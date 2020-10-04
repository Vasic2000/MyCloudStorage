import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ClientForm implements Initializable {
    public ListView<String> listView;
    private String path = "client/src/main/resources/client_storage";
    private DataInputStream cis;
    private DataOutputStream cos;
    private boolean upLoad = false;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            cis = new DataInputStream(socket.getInputStream());
            cos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(ActionEvent actionEvent) {
        if(!upLoad) upload(actionEvent);
        else download(actionEvent);
    }

    public void upload(ActionEvent actionEvent) {
        String file = listView.getSelectionModel().getSelectedItem();
        System.out.println(file);
        try {
            cos.writeUTF(file);
            File current = new File(path + "/" + file);
            cos.writeLong(current.length());
            FileInputStream is = new FileInputStream(current);
            int tmp;
            byte [] buffer = new byte[8192];
            while ((tmp = is.read(buffer)) != -1) {
                cos.write(buffer, 0, tmp);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void download(ActionEvent actionEvent) {
        String file = listView.getSelectionModel().getSelectedItem();
        System.out.println("Прошу у сервера " + file);
        try {
            cos.writeUTF("_downLoad");
            cos.writeUTF(file);

            File dFile = new File("client/src/main/resources/client_storage/" + file);
            if (!dFile.exists()) {
                dFile.createNewFile();
                FileOutputStream os = new FileOutputStream(dFile);
                //                2. Получаю размер
                long fileLength = cis.readLong();
                System.out.println("Wait: " + fileLength + " bytes");
//                3. Читаю
                byte[] buffer = new byte[8192];
                for (int i = 0; i < (fileLength + 8191) / 8192; i++) {
                    int cnt = cis.read(buffer);
                    os.write(buffer, 0, cnt);
                }
                System.out.println("Downloaded!");
                os.close();
            } else {
                System.out.println("Такой уже есть");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshList() {
        File file = new File(path);
        String[] files = file.list();
        listView.getItems().clear();
        if (files != null) {
            for (String name : files) {
                listView.getItems().add(name);
            }
        }
    }

    private List<String> getServerFiles() throws IOException {
        upLoad = true;
        List<String> files = new ArrayList();
        cos.writeUTF("_getFilesList?");
        cos.flush();
        int listSize = cis.readInt();
        for (int i = 0; i < listSize; i++) {
            files.add(cis.readUTF());
        }
        return files;
    }

    public void refreshList(ActionEvent actionEvent) {

        refreshList();
    }

    public void clientList(ActionEvent actionEvent ) {
        upLoad = false;
        refreshList();
    }

    public void serverList(ActionEvent actionEvent) {
        try {
            listView.getItems().clear();
            listView.getItems().addAll(getServerFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(ActionEvent actionEvent) {
        String delFile = listView.getSelectionModel().getSelectedItem();
        System.out.println(delFile + " will be deleted!");
        File file = new File(path + "/" + delFile);
        if (file.delete()) {
            System.out.println(delFile + " файл был удален");
        } else System.out.println("Файл " + delFile + " не был найден");
        refreshList();
    }
}
