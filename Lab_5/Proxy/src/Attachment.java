import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class Attachment {
    private SocksState state = SocksState.METHOD_SENDING;

    private ByteBuffer inputData;
    private ByteBuffer outputData;

    private SelectionKey peerKey;
    private int peerPort;

    private boolean inputShutdown = false;
    private boolean outputShutdown = false;

    public boolean isOutputShutdown() {
        return outputShutdown;
    }

    public void setOutputShutdown(boolean outputShutdown) {
        this.outputShutdown = outputShutdown;
    }

    public boolean isInputShutdown() {
        return inputShutdown;
    }

    public void setInputShutdown(boolean inputShutdown) {
        this.inputShutdown = inputShutdown;
    }

    public SocksState getState() {
        return state;
    }

    public void setState(SocksState state) {
        this.state = state;
    }

    public ByteBuffer getInputData() {
        return inputData;
    }

    public void setInputData(ByteBuffer inputData) {
        this.inputData = inputData;
    }

    public ByteBuffer getOutputData() {
        return outputData;
    }

    public void setOutputData(ByteBuffer outputData) {
        this.outputData = outputData;
    }

    public SelectionKey getPeerKey() {
        return peerKey;
    }

    public void setPeerKey(SelectionKey peerKey) {
        this.peerKey = peerKey;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }
}
