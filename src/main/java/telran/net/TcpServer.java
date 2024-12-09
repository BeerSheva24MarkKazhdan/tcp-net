package telran.net;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Runnable{
    Protocol protocol;
    int port;
    private final ExecutorService executorService;
    private final ServerControl serverControl;
    private volatile boolean shutdownInitiated;

    public TcpServer(Protocol protocol, int port, int maxThreads) {
        this.protocol = protocol;
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(maxThreads);
        this.serverControl = new ServerControl();
        this.shutdownInitiated = false;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(5000);
            System.out.println("Server is listening on the port " + port);

            while (!shutdownInitiated) {
                try {
                    Socket socket = serverSocket.accept();
                    TcpClientServerSession session = new TcpClientServerSession(protocol, socket, serverControl);
                    executorService.submit(session);
                } catch (SocketTimeoutException e) {
                    if (shutdownInitiated) {
                        System.out.println("Server shutdown in progress...");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } finally {
            shutdownNow();
        }
    }

    public void shutdown() {
        System.out.println("Shutdown initiated...");
        this.shutdownInitiated = true;
        serverControl.initiateShutdown();
    }

    private void shutdownNow() {
        executorService.shutdownNow(); // Прерываем все задачи
        System.out.println("All tasks interrupted.");
    }
}