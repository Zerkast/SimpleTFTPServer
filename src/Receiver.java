import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Receiver {
    private final DatagramSocket socket;
    private InternaR r;
    private IDataConsumer consumer;
    
    public Receiver(DatagramSocket socket, int bufferLength) {
            this.socket=socket;
            r=new InternaR(socket,bufferLength);        
    }
    
    public void setConsumer(IDataConsumer consumer){
        this.consumer=consumer;
        r.start();
    }

    
    class InternaR extends Thread{
        DatagramSocket socket;
        DatagramPacket packetIn;
        int bufferLength;


        InternaR(DatagramSocket socket,int bufferLength) {
            this.socket=socket;
            this.bufferLength = bufferLength;
        }
        
        @Override
        public void run(){
            try {
                byte [] buffer=new byte[bufferLength];
                packetIn=new DatagramPacket(buffer, bufferLength);
                while(packetIn.getLength()!=0){
                    packetIn=new DatagramPacket(buffer, bufferLength);
                    socket.receive(packetIn);
                    consumer.consumeData(packetIn.getData(),packetIn.getLength(), packetIn.getAddress(),packetIn.getPort());
                }
            } catch (SocketException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        
    }
}
