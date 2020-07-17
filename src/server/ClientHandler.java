package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String nick;
    private List<String> blackList;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        //цикл инициализации
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if ( (newNick != null) & ( !server.isNickUse(newNick) ) ) {
                                    nick = newNick;
                                    sendMsg("/authok " + nick);
                                    server.subscribe(ClientHandler.this);
                                    //заполнение черного списка
                                    AuthService.fillBlackList(blackList, nick);
                                    break;
                                } else {
                                    sendMsg("Неверный логин/пароль или ник уже испльзуется");
                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if( str.startsWith("/") ){
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverClosed");
                                    break;
                                } else if (str.startsWith("/blacklist+")) {
                                    String[] tokens = str.split(" ");
                                    addUserInBlackList(tokens[1]);
                                }else if (str.startsWith("/blacklist-")) {
                                    String[] tokens = str.split(" ");
                                    extractUserInBlackList(tokens[1]);
                                }else if (str.startsWith("/w")){
                                    server.translateMsgUnicast(nick + ": " + str);
                                }
                            }else {
                                server.translateMsgBroadcast(nick + ": " + str);
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick(){
        return nick;
    }

    public void addUserInBlackList(String blackUser){
        // проверка на существование такого узера в списке
        for (String o : this.blackList) {
            if (o.equals(blackUser)){
                sendMsg("user c ником - " + blackUser+ " уже находится в черном списке");
                return;
            }
        }
        // добавление юзерара в bleckList таблицу если он есть в таблице main иначе
        if ( AuthService.addUserInTableBlackList(this.nick, blackUser) ){
            blackList.add(blackUser);
        }else{
            sendMsg("user с ником - " + blackUser + " незарегитрирован в чате");
        }
    }

    public boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }

    public void extractUserInBlackList(String whiteUser){
        // проверка на существование такого узера в списке
        boolean isUserInBlackList = false;
        for (String o : this.blackList) {
            if (o.equals(whiteUser)){
                isUserInBlackList = true;
                break;
            }
        }
        if (!isUserInBlackList) {
            sendMsg("user c ником - " + whiteUser + " отсутствует в черном списке");
            return;
        }

        // удаление юзерара из blackList таблицу если он есть в таблице main иначе
        if ( AuthService.extractUserInTableBlackList(this.nick, whiteUser) ){
            blackList.remove(whiteUser);
        }else{
            sendMsg("user с ником - " + whiteUser + " незарегитрирован в чате");
        }
    }


}
