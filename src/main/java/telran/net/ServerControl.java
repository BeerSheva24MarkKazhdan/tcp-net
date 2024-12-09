package telran.net;

public class ServerControl {
    private volatile boolean shutdownInitiated = false;
    private final int maxFailedResponses;
    private final int maxRequestsPerSecond;

    public ServerControl() {
        this.maxFailedResponses = 10;
        this.maxRequestsPerSecond = 100;
    }

    public boolean isShutdownInitiated() {
        return shutdownInitiated;
    }

    public void initiateShutdown() {
        shutdownInitiated = true;
    }

    public int getMaxFailedResponses() {
        return maxFailedResponses;
    }

    public int getMaxRequestsPerSecond() {
        return maxRequestsPerSecond;
    }
}
