import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    //    Где сервер собирает файлы
    private static final String path = "server/src/main/resources/";

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(8189)) {
            System.out.println("Server started");
            Socket socket = server.accept();
            System.out.print("Connection accepted.");

//            Server input stream
            DataInputStream sis = new DataInputStream(socket.getInputStream());
//            Server output stream
            DataOutputStream sos = new DataOutputStream(socket.getOutputStream());

//            Пока сокет существует
            while (true) {
                if (socket.isClosed()) break;
//                1. Жду имя и проверяю, нет ли уже такого
                String fileName = sis.readUTF();

                if (fileName.equals("_getFilesList?")) {
                    File dir = new File(path);
                    String[] files = dir.list();
                    if (files != null) {
                        sos.writeInt(files.length);
                        for (String file : files) {
                            sos.writeUTF(file);
                        }
                    } else {
                        sos.writeInt(0);
                    }
                    sos.flush();
                } else if (fileName.equals("_downLoad")) {
                    String dFile = sis.readUTF();
                    System.out.println("Отдаю file: " + dFile);
                    File file = new File("Server/src/main/resources/" + dFile);
                    if (!file.exists()) {
//                      Сюда попасть не должен никогда, но пусть будет для отладки
                        System.out.println("У server нет " + dFile + " файла :(");
                    }
                    else {
                        try {
                            sos.writeLong(dFile.length());
                            FileInputStream is = new FileInputStream(file);
                            int tmp;
                            byte [] buffer = new byte[8192];
                            while ((tmp = is.read(buffer)) != -1) {
                            sos.write(buffer, 0, tmp);
                        }
                    } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    System.out.println("Save file: " + fileName);
                    File file = new File("server/src/main/resources/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        System.out.println("Такой уже есть");
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    //                2. Получаю размер
                    long fileLength = sis.readLong();
                    System.out.println("Wait: " + fileLength + " bytes");
//                3. Читаю
                    byte[] buffer = new byte[8192];
                    for (int i = 0; i < (fileLength + 8191) / 8192; i++) {
                        int cnt = sis.read(buffer);
                        os.write(buffer, 0, cnt);
                    }
                    System.out.println("File successfully uploaded!");
                    os.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
