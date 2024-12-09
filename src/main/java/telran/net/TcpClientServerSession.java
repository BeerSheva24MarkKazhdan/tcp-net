package telran.net;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpClientServerSession implements Runnable{
    Protocol protocol;
    Socket socket;
    private final ServerControl serverControl;
    private static final int IDLE_TIMEOUT = 5000;
    private final AtomicInteger failedResponses;
    public TcpClientServerSession(Protocol protocol, Socket socket, ServerControl serverControl) {
        this.protocol = protocol;
        this.socket = socket;
        this.serverControl = serverControl;
        this.failedResponses = new AtomicInteger(0);
    }
    @Override
    public void run() {
        try {
            socket.setSoTimeout(IDLE_TIMEOUT);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintStream writer = new PrintStream(socket.getOutputStream())) {

                String request;
                while ((request = reader.readLine()) != null) {
                    if (serverControl.isShutdownInitiated()) {
                        System.out.println("Server is shutting down. Closing session.");
                        break;
                    }

                    String response = protocol.getResponseWithJSON(request);

                    writer.println(response);

                    if (response.contains("ResponseCode.NOT_OK")) {
                        if (failedResponses.incrementAndGet() > serverControl.getMaxFailedResponses()) {
                            System.out.println("Too many failed responses. Closing session.");
                            break;
                        }
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timeout. Closing session: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception in session: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to close socket: " + e.getMessage());
            }
        }
    }
}