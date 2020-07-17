package server;

import javax.naming.InsufficientResourcesException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");
            clients = new Vector<>();

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void translateMsgUnicast(String msg) {

        ClientHandler destination = this.destinationPrivateMsg(msg);
        ClientHandler source = this.sourcePrivateMsg(msg);

        if (destination != null){
            String[] msgForSend = msg.split(" ", 4);

            if (msgForSend.length == 4){
                if (!destination.checkBlackList(source.getNick())) {
                    destination.sendMsg(source.getNick() + "> " + msgForSend[3]);
                    source.sendMsg(source.getNick() + ": "+">" + destination.getNick() + "< " + msgForSend[3]);
                }else{
                    source.sendMsg(source.getNick() + ": "+">" + destination.getNick() + "< " + msgForSend[3]);
                }
            }
        }
    }

    public void translateMsgBroadcast(String msg){
        {
            ClientHandler source = this.sourcePrivateMsg(msg);

            for (ClientHandler o : clients) {
                if ( !o.checkBlackList(source.getNick()) ) {
                    o.sendMsg(msg);
                }
            }
        }
    }

    private ClientHandler destinationPrivateMsg(String msg){
        String[] tokens = msg.split(" ",4); // "srcUser: /w dstUser message"

        if (tokens.length > 3) {
            for (ClientHandler o : clients) {
                if ( tokens[2].equals( o.getNick() ) ){
                    return o;
                }
            }
        }
        return null;
    }

    private ClientHandler sourcePrivateMsg(String msg) {
        String[] str = msg.split(" ");
        for (ClientHandler o : clients) {
            if ( str[0].equals(o.getNick()+":") ){
                return o;
            }
        }
        return null;
    }

    public boolean isNickUse(String nick){
        for(ClientHandler o : clients){
            if (nick.equals(o.getNick())){
                return true;
            }
        }
        return false;
    }

}
