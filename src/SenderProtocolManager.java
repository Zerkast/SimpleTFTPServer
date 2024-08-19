import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class SenderProtocolManager {
    private Sender sender;
    private Map<Integer, String> errors;    

    public SenderProtocolManager(Sender sender) {
        this.sender = sender;
        errors = new HashMap<>();
        errors.put(1, "File not found\0");
        errors.put(2, "Access violation\0");
        errors.put(3, "Disk full or allocation exceeded\0");
        errors.put(4, "Illegal TFTP operation\0");
        errors.put(5, "Unknown transfer ID\0");
        errors.put(6, "File already exists\0");
        //ci sarebbe un errore "no such user", tuttavia non mi pare proprio che il protocollo preveda un sistema di autenticazione...
    }

    public void sendError (int errorCode, InetAddress ipAddress, int port) {
        byte[] errorMessage = errors.get(errorCode).getBytes();
        byte[] message = new byte[4 + errorMessage.length];
        message[0] = 0;
        message[1] = 5; 
        message[2] = 0;
        message[3] = (byte)errorCode;
        for (int i = 0; i < errorMessage.length; i++) message[4+i] = errorMessage[i];
        sender.send(message, ipAddress, port);
    }

    public void sendData(int sequenceNumber, byte[] data, InetAddress ipAddress, int port) {
        byte[] message = new byte[4 + data.length];
        message[0] = 0;
        message[1] = 3; 
        message[2] = (byte) (sequenceNumber >> 8);
        message[3] = (byte) (sequenceNumber);
        for (int i = 0; i < data.length; i++) {
            message[4+i] = data[i];
        }
        sender.send(message, ipAddress, port);
    }

    public void sendListResponse(int sequenceNumber, String filename, int filesize ,InetAddress ipAddress, int port) {
        byte[] filenameInBytes = (filename + "\0").getBytes(); 
        byte[] message = new byte[filenameInBytes.length + 8];
        System.out.println(filesize);
        message[0] = 0;
        message[1] = 7;
        message[2] = (byte) (sequenceNumber >> 8);
        message[3] = (byte) (sequenceNumber);
        for (int i = 7; i >= 4; i--) {
            message[i] = (byte)(filesize & 0xFF);
            filesize>>=8;
        }
        for (int i = 0; i < filenameInBytes.length; i++) {
            message[8+i] = filenameInBytes[i];
        }
        sender.send(message, ipAddress, port);
        /**
         * OPCODE 2b, # BLOCK 2b, FILESIZE in bytes 4b, FILENAME n b, \0 1b  
         * la fine viene segnalata con un nome vuoto e dimensione pari a 0
         */
    }

    public void sendAck(int sequenceNumber, InetAddress ipAddress, int port) {
        byte[] message = new byte[4];
        message[0] = 0;
        message[1] = 4;
        message[2] = (byte) (sequenceNumber >> 8);
        message[3] = (byte) (sequenceNumber);
        sender.send(message, ipAddress, port);
    }
}