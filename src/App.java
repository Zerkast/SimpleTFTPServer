import java.net.DatagramSocket;

public class App {
    public static void main(String[] args) throws Exception {
        DatagramSocket server = new DatagramSocket(10000);
        Receiver rcv = new Receiver(server, 1024);
        Sender snd = new Sender(server);
        SenderProtocolManager spm = new SenderProtocolManager(snd);
        Server srv = new Server(spm, "./data/");
        ReceiverProtocolManager receiveManager = new ReceiverProtocolManager(srv);
        rcv.setConsumer(receiveManager);
    }
}
