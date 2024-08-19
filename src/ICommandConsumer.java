

import java.net.InetAddress;

public interface ICommandConsumer {
    void fileReadRequest(String fileName, InetAddress clientAddress, int clientPort);
    void fileWriteRequest(String filename, InetAddress clientAddress, int clientPort);
    void ack(int sequenceNumber, InetAddress clientAddress, int clientPort);
    void data(int sequenceNumber, byte[] data, InetAddress clientAddress, int clientPort);
    void listRequest(InetAddress clientAddress, int clientPort);
}
