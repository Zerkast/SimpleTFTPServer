

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReceiverProtocolManager implements IDataConsumer{

    ICommandConsumer commandConsumer;

    public ReceiverProtocolManager(ICommandConsumer commandConsumer) {
        this.commandConsumer = commandConsumer;
    }

    @Override
    public void consumeData(byte[] arg0, int arg1, InetAddress arg2, int arg3) {
        System.out.println((((arg0[2]&0xFF)<<8) | arg0[3])&0xFF);
        System.out.println(arg0[0]+arg0[1]);
        switch (((arg0[0]&0xFF)<<8) + arg0[1]) { //il casting a int non dovrebbe servire, ma non si sa mai
            case 1:
                commandConsumer.fileReadRequest(stringReader(arg0, 2), arg2, arg3);
                break;
            case 2:
                commandConsumer.fileWriteRequest(stringReader(arg0, 2), arg2, arg3);
                break;
            case 3:
                commandConsumer.data((((arg0[2]&0xFF)<<8) | arg0[3]&0xFF),Arrays.copyOfRange(arg0, 4, arg0.length), arg2, arg3);
                break;
            case 4: 
                commandConsumer.ack((((arg0[2]&0xFF)<<8) | arg0[3]&0xFF),arg2, arg3);
                break;
            case 6:
                commandConsumer.listRequest(arg2, arg3);
                break;
        }
    }

    private String stringReader(byte[] byteArray, int beginIndex) {
        int end = beginIndex;
        for (; byteArray[end]!=0 && end < byteArray.length; end++);
        return new String(byteArray, StandardCharsets.UTF_8).substring(2, end);
    }
    private int byteArrayToInt(byte[] data) {
        int value = 0;
        for (byte b : data) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
