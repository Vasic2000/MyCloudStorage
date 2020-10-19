import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() throws IOException {
        ServerSocket server = new ServerSocket(8189);
        Socket socket;
        System.out.println("Сервер запущен!");

        while (true) {
            socket = server.accept();
            System.out.println("Клиент " + socket.toString() + " подключился");
            new Client_Handler(socket);
        }
    }
}
