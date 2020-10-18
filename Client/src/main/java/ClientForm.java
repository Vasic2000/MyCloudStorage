import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ClientForm implements Initializable {
    private final String path = "client/src/main/resources/client_storage";
    private String relativePath = "";
    private String clientFile, serverFile;

    private DataInputStream cis;
    private DataOutputStream cos;

    @FXML
    private ListView<String> listClient;

    @FXML
    public void handleMouseClickClient() {
        clientFile = listClient.getSelectionModel().getSelectedItem();

        if (clientFile.equals("...")) {
            relativePath = navigateUp(relativePath);
            refreshClientList();
        } else {
            File current = new File(path + relativePath + "/" + clientFile);
            if (current.isDirectory()) {
                System.out.println(clientFile + " is a directory");
                relativePath = relativePath + "/" + clientFile;
                refreshClientList();
            } else
                System.out.println(clientFile + " is a file");
        }
    }

    private String navigateUp(String relativePath) {
        int index = relativePath.lastIndexOf("/");
        return relativePath.substring(0, index);
    }

    @FXML
    private ListView<String> listServer;

    @FXML
    public void handleMouseClickServer() throws IOException {
        serverFile = listServer.getSelectionModel().getSelectedItem();
        if (serverFile.equals("...")) {
            serverNavigateOut();
            refreshServerList();
        } else {
            if (isServerDirectory(serverFile)) {
                serverNavigateIn();
                cos.writeUTF(serverFile);
                System.out.println(serverFile + " is a directory");
                refreshServerList();
            }
            else
                System.out.println(serverFile + " is a file");
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            cis = new DataInputStream(socket.getInputStream());
            cos = new DataOutputStream(socket.getOutputStream());

            refreshClientList();
            refreshServerList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serverNavigateIn() {
        try {
            cos.writeUTF("_navigateIn");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serverNavigateOut() {
        try {
            cos.writeUTF("_navigateOut");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isServerDirectory(String file) {
        System.out.println("Спрашиваю сервер кто такой " + file);
        try {
            cos.writeUTF("_whoIsFile");
            cos.writeUTF(file);
            return cis.readBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void upload() {
        if (!clientFile.isEmpty()) {
            System.out.println(clientFile);
            try {
                cos.writeUTF(clientFile);
                File current = new File(path + relativePath + "/" + clientFile);
                cos.writeLong(current.length());
                FileInputStream is = new FileInputStream(current);
                int tmp;
                byte[] buffer = new byte[8192];
                while ((tmp = is.read(buffer)) != -1) {
                    cos.write(buffer, 0, tmp);
                }
                cos.flush();
                is.close();
                Thread.sleep(150); //Костыль. Плохое решение команды и файлы в один поток. Сделано, чтобы следующая команда не цеплялась к файлу, не нашёл , видимо из-за многопоточности.
                refreshServerList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void download() {
        if (!serverFile.isEmpty()) {
            System.out.println("Прошу у сервера " + serverFile);
            try {
                cos.writeUTF("_downLoad");
                cos.writeUTF(serverFile);
                File dFile = new File(path + relativePath + "/"+ serverFile);
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
                    refreshClientList();
                } else {
                    System.out.println("Такой уже есть");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshClientList() {
        File clientFile = new File(path + relativePath);
        String[] files = clientFile.list();
        listClient.getItems().clear();
        if (files != null) {
            if(!relativePath.equals(""))
                listClient.getItems().add("...");
            for (String name : files) {
                listClient.getItems().add(name);
            }
        }
        listClient.refresh();
    }

    private void refreshServerList() throws IOException {
        List<String> files = getServerFiles();
        listServer.getItems().clear();
        if (files != null) {
            for (String name : files) {
                listServer.getItems().add(name);
            }
        }
        listServer.refresh();
    }

    private List<String> getServerFiles() throws IOException {
        List<String> files = new ArrayList();
        cos.writeUTF("_getFilesList?");
        int listSize = cis.readInt();
        for (int i = 0; i < listSize; i++) {
            files.add(cis.readUTF());
        }
        return files;
    }

    public void deleteItem() {
        String delFile = listClient.getSelectionModel().getSelectedItem();
        System.out.println(delFile + " will be deleted!");
        File file = new File(path + "/" + delFile);
        if (file.delete()) {
            System.out.println(delFile + " файл был удален");
        } else System.out.println("Файл " + delFile + " не был найден");
        refreshClientList();
    }

    @FXML
    private Button closeButton;

    @FXML
    public void closeButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        try {
            cos.writeUTF("_/end");
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.close();
    }
}
