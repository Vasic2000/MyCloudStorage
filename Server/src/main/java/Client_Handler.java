import java.io.*;
import java.net.Socket;

public class Client_Handler {

    private final Socket socket;
    private final Thread rxThread;
    private DataInputStream dis;
    private DataOutputStream dos;
    //    Где сервер собирает файлы
    private static final String path = "server/src/main/resources/";

    public Client_Handler(final Socket socket) {

        this.socket = socket;

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());

                    while (!rxThread.isInterrupted()) {
//                1. Жду имя и проверяю, нет ли уже такого
                        String fileName = dis.readUTF();

                        switch (fileName) {

                            case "_whoIsFile" :
                                String boolFile = dis.readUTF();
                                File bFile = new File("Server/src/main/resources/" + boolFile);
                                Boolean bbb = bFile.isDirectory();
                                dos.writeBoolean(bbb);
                                break;

                            case "_getFilesList?":
                                File dir = new File(path);
                                String[] files = dir.list();
                                if (files != null) {
                                    dos.writeInt(files.length);
                                    for (String file : files) {
                                        dos.writeUTF(file);
                                    }
                                } else {
                                    dos.writeInt(0);
                                }
                                dos.flush();
                                break;

                            case "_downLoad":
                                String dFile = dis.readUTF();
                                System.out.println("Отдаю file: " + dFile);
                                File file = new File("Server/src/main/resources/" + dFile);
                                if (!file.exists()) {
//                      Сюда попасть не должен никогда, но пусть будет для отладки
                                    System.out.println("У server нет " + dFile + " файла :(");
                                } else {
                                    try {
                                        dos.writeLong(dFile.length());
                                        FileInputStream is = new FileInputStream(file);
                                        int tmp;
                                        byte[] buffer = new byte[8192];
                                        while ((tmp = is.read(buffer)) != -1) {
                                            dos.write(buffer, 0, tmp);
                                        }
                                        is.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;

                            case "_/end":
                                disconnect();
                                break;

                            default:

                                System.out.println("Save file: " + fileName);
                                File saveFile = new File("server/src/main/resources/" + fileName);
                                if (!saveFile.exists()) {
                                    saveFile.createNewFile();
                                    FileOutputStream os = new FileOutputStream(saveFile);
                                    //                2. Получаю размер
                                    long fileLength = dis.readLong();
                                    System.out.println("Wait: " + fileLength + " bytes");
//                3. Читаю
                                    byte[] buffer = new byte[8192];
                                    for (int i = 0; i < (fileLength + 8191) / 8192; i++) {
                                        int cnt = dis.read(buffer);
                                        os.write(buffer, 0, cnt);
                                    }
                                    System.out.println("File successfully uploaded!" + buffer.toString());
                                    os.close();
                                } else {
                                    System.out.println("Такой уже есть");
                                }
                                break;
                        }
                    }
                } catch (IOException e) {
                    disconnect();
                }
            }
        });
        rxThread.start();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException io) {
            System.out.println(io);
        }
        rxThread.interrupt();
    }
}
