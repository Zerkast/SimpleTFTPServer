
import java.net.InetAddress;

public interface IDataConsumer {
    void consumeData(byte [] data, int dataLegth, InetAddress address, int port);
}
