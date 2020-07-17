package client.sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
public class Controller {

    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox vBoxChat;
    @FXML
    TextField textField;
    @FXML
    Button smButton;
    @FXML
    HBox bottomPanel;


    @FXML
    HBox upperPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    TextArea textArea;


    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    String nick;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    private boolean isAuthorized;

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            textArea.setVisible(true);
            textArea.setManaged(true);
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            textArea.setVisible(false);
            textArea.setManaged(false);
            scrollPane.setVisible(true);
            scrollPane.setManaged(true);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Main.getStage().setTitle("CHAT -> "+ nick);
                }
            });
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true){
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                nick = str.split(" ",2)[1];
                                setAuthorized(true);
                                break;
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.equalsIgnoreCase("/serverClosed")) {
                                break;
                            }else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        addMessageOnBox(str + "\n");
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMessageOnBox(String str){
        String[] msg = str.split(" ",2);

        Label label;
        VBox vBox = new VBox();

        if (msg[0].equals(nick + ":")){
            vBox.setAlignment(Pos.TOP_RIGHT);
            label = new Label(msg[1]);
        }else{
            vBox.setAlignment(Pos.TOP_LEFT);
            label = new Label(str);
        }

        vBox.getChildren().add(label);
        vBoxChat.getChildren().add(vBox);
        scrollPane.vvalueProperty().bind(vBoxChat.heightProperty());

        textField.clear();
        textField.requestFocus();

    }
}
