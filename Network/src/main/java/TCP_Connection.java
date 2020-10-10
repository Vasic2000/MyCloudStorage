import java.io.*;
import java.net.Socket;

public class TCP_Connection {

    private final Socket socket;
    private final Thread rxThread;
    private final TCP_Connection tcp_eventsListener;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    //    Где сервер собирает файлы
    private static final String path = "server/src/main/resources/";

    public TCP_Connection(final TCP_Connection tcp_eventsListener, Socket socket) throws IOException {

        this.socket = socket;
        this.tcp_eventsListener = tcp_eventsListener;
        
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        rxThread = new Thread(new Runnable() {
            public void run() {
                tcp_eventsListener.onConnectionReady(TCP_Connection.this);
                while(true) {
                    try {
//                1. Жду имя и проверяю, нет ли уже такого
                        String fileName = dis.readUTF();

                        if (fileName.equals("_getFilesList?")) {
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
                        } else if (fileName.equals("_downLoad")) {
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
                        } else {
                            System.out.println("Save file: " + fileName);
                            File file = new File("server/src/main/resources/" + fileName);
                            if (!file.exists()) {
                                file.createNewFile();
                                FileOutputStream os = new FileOutputStream(file);
                                //                2. Получаю размер
                                long fileLength = dis.readLong();
                                System.out.println("Wait: " + fileLength + " bytes");
//                3. Читаю
                                byte[] buffer = new byte[8192];
                                for (int i = 0; i < (fileLength + 8191) / 8192; i++) {
                                    int cnt = dis.read(buffer);
                                    os.write(buffer, 0, cnt);
                                }
                                System.out.println("File successfully uploaded!");
                                os.close();
                            } else {
                                System.out.println("Такой уже есть");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        rxThread.start();
    }

    private void onConnectionReady(TCP_Connection tcp_connection) {
    }
}
