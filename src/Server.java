

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

public class Server implements ICommandConsumer{
    private SenderProtocolManager spm;
    private Map<String, SessionBehaviour> connectedClients; //forse devo aggiungere anche l'opcode alla stringa con l'ip e la porta usata come chiave
    private Timer timer;
    private String dataDirectory;

    public Server(SenderProtocolManager spm, String dataDirectory) {
        this.spm = spm;
        timer = new Timer();
        connectedClients = new HashMap<>();
        this.dataDirectory = dataDirectory;
    }
    @Override
    public void fileReadRequest(String fileName, InetAddress clientAddress, int clientPort) {
        File file = new File(dataDirectory+fileName);
        if (!file.isFile()) {
            spm.sendError(1, clientAddress, clientPort);
            return;           
        }
        if (!file.canRead()) {
            spm.sendError(2, clientAddress, clientPort);
            return;
        }
        connectedClients.put(clientAddress.getHostAddress() + clientPort, new FileSessionData(fileName));
        readDataFromFile((int)1, fileName, clientAddress, clientPort, 1);
    }

    @Override
    public void fileWriteRequest(String filename, InetAddress clientAddress, int clientPort) {
        File file = new File(dataDirectory+filename);
        if (file.exists()) spm.sendError(6, clientAddress, clientPort);
        else {
            connectedClients.put(clientAddress.getHostAddress() + clientPort, new FileSessionData(filename));
            spm.sendAck(0, clientAddress, clientPort);
        }
    }

    @Override
    public void ack(int sequenceNumber, InetAddress clientAddress, int clientPort) {
        SessionBehaviour currentSession = connectedClients.get(clientAddress.getHostAddress() + clientPort);
        if (currentSession!=null) {
            if (!currentSession.isLastPiece()) {
                currentSession.increaseSequenceNumber();
                if (currentSession.getDataType()==DataTypes.FILE) readDataFromFile((int)(sequenceNumber+1), currentSession.getFilename(), clientAddress, clientPort, 1);
                else sendListResponse(currentSession, clientAddress, clientPort, clientPort);
            } else {
                connectedClients.remove(clientAddress.getHostAddress() + clientPort);
            }
        }
    }

    @Override
    public void data(int sequenceNumber, byte[] data, InetAddress clientAddress, int clientPort) {
        SessionBehaviour currentSession = connectedClients.get(clientAddress.getHostAddress() + clientPort);
        if (sequenceNumber<currentSession.getSequenceNumber()) {
            spm.sendAck(sequenceNumber, clientAddress, clientPort);
        }
        RandomAccessFile stream;
        try {
            stream = new RandomAccessFile(dataDirectory+currentSession.getFilename(), "rw");
                stream.seek((sequenceNumber-1)*512);
                stream.write(data,0,512);
                if (data.length<512) { 
                    sequenceNumber = 0;
                }
            spm.sendAck(currentSession.getSequenceNumber(), clientAddress, clientPort);
            currentSession.increaseSequenceNumber();
            if (data.length<512) { //TODO: VERIFICA CHE FUNZIONI
                connectedClients.remove(clientAddress.getHostAddress() + clientPort);
            }
            stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void listRequest(InetAddress clientAddress, int clientPort) {
        ArrayDeque<String> filenames = new ArrayDeque<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataDirectory))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    filenames.add(path.getFileName().toString());
                }
            }
            filenames.add("");
            connectedClients.put(clientAddress.getHostAddress() + clientPort, new FileListSessionData(filenames));
            SessionBehaviour session = connectedClients.get(clientAddress.getHostAddress() + clientPort);
            sendListResponse(session, clientAddress, clientPort, clientPort);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readDataFromFile(int packageNumber, String filename, InetAddress ipAddress, int port, int attemptNumber) {
        if (attemptNumber==5) {
            System.out.println("Lost connection to " + ipAddress.getHostAddress() + ":" + port);
            connectedClients.remove(ipAddress.getHostAddress()+port);
        } else {
            byte[] buffer = new byte[512]; 
            File file = new File(dataDirectory+filename);
            RandomAccessFile stream;
            try {
                stream = new RandomAccessFile(file, "r");
                stream.seek((packageNumber-1)*512);
                int bytesNum = stream.read(buffer, 0, 512);
                if (bytesNum<512) {
                    connectedClients.get(ipAddress.getHostAddress() + port).setIsLastPiece();
                }
                spm.sendData(packageNumber, buffer, ipAddress, port);
                TimerTask tt = new TimerTask() {
                    int lastPackageNumber = packageNumber;
                    @Override
                    public void run() {
                        if (connectedClients.get(ipAddress.getHostAddress()+port)!=null) {
                            if (connectedClients.get(ipAddress.getHostAddress()+port).getSequenceNumber()==lastPackageNumber) {
                                readDataFromFile(lastPackageNumber, filename, ipAddress, port, attemptNumber+1);
                            }
                        }
                    }  
                };
                timer.schedule(tt, 1000);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendListResponse(SessionBehaviour currentSession, InetAddress clientAddress, int clientPort, int attemptNumber) {
        try {
            int currentSequenceNumber = currentSession.getSequenceNumber();
            int filesize = 0;
            if (currentSession.getFilename()!="") {
                filesize = (int)Files.size(Paths.get(dataDirectory+currentSession.getFilename()));
            }
            spm.sendListResponse(currentSequenceNumber, currentSession.getFilename(), filesize, clientAddress, clientPort);
            TimerTask ttl = new TimerTask() {
                int lastPackageNumber = currentSequenceNumber;
                @Override
                public void run() {
                    if (connectedClients.get(clientAddress.getHostAddress()+clientPort)!=null) {
                        if (connectedClients.get(clientAddress.getHostAddress()+clientPort).getSequenceNumber()==lastPackageNumber) {
                            sendListResponse(currentSession, clientAddress, clientPort, attemptNumber+1);
                        }
                    }
                }
            };
            // timer.schedule(ttl, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
