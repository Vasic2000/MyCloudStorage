import java.io.*;
import java.net.Socket;

public class Client_Handler {

    private final Socket socket;
    private final Thread rxThread;
    private DataInputStream dis;
    private DataOutputStream dos;
//    Где сервер собирает файлы
    private String path = "server/src/main/resources";
//    Дополнительный путь, подпапки
    private String relativePath = "";

    public Client_Handler(final Socket socket) {

        this.socket = socket;

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());

                    while (!rxThread.isInterrupted()) {
//                1. Жду команду
                        String fileName = dis.readUTF();

                        switch (fileName) {

                            case "_navigateIn" :
                                String dirName = dis.readUTF();
                                relativePath = relativePath + "/" + dirName;
                                break;

                            case "_navigateOut" :
                                relativePath = navigateUp(relativePath);
                                break;

                            case "_whoIsFile" :
                                String boolFile = dis.readUTF();
                                File bFile = new File(path + relativePath + "/" + boolFile);
                                dos.writeBoolean(bFile.isDirectory());
                                break;

                            case "_getFilesList?":
                                File dir = new File(path + relativePath);
                                String[] files = dir.list();
                                if (files != null) {
                                    if (!relativePath.equals("")) {
                                        dos.writeInt(files.length + 1);
                                        dos.writeUTF("...");
                                        for (String file : files) {
                                            dos.writeUTF(file);
                                        }
                                    } else {
                                        dos.writeInt(files.length);
                                        for (String file : files) {
                                            dos.writeUTF(file);
                                        }
                                    }
                                } else {
                                    dos.writeInt(0);
                                }
                                break;

                            case "_downLoad":
                                String dFile = dis.readUTF();
                                System.out.println("Отдаю file: " + dFile);
                                File file = new File(path + relativePath + "/" + dFile);
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
                                        dos.flush();
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
                                File saveFile = new File(path + relativePath + "/" + fileName);
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
                                    System.out.println(saveFile + " Загружен!");
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

    private String navigateUp(String relativePath) {
        int index = relativePath.lastIndexOf("/");
        return relativePath.substring(0, index);
    }

    public void disconnect() {
        try {
            System.out.println("Всё, клиент " + socket.toString() + " отключился");
            socket.close();
        } catch (IOException io) {
            System.out.println(io);
        }
        rxThread.interrupt();
    }
}
