import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    //    Список клиентов
    Vector<Client_Handler> clients;

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() throws IOException {
        this.clients = new Vector<>();
        ServerSocket server = new ServerSocket(8189);
        Socket socket;
        System.out.println("Сервер запущен!");

        while (true) {
            socket = server.accept();
            System.out.println("Клиент " + socket.toString() + " подключился");
            clients.add(new Client_Handler(socket));
        }
    }
}
