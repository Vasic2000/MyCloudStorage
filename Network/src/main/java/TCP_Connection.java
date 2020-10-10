import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCP_Connection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCP_Connection tcp_eventsListener;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    public TCP_Connection(TCP_Connection tcp_eventsListener, Socket socket) throws IOException {
        this.socket = socket;
        this.tcp_eventsListener = tcp_eventsListener;
        
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        rxThread = new Thread(new Runnable() {
            public void run() {

            }
        });
        rxThread.start();
    }
}
