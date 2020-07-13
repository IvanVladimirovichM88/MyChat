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
//            String name = AuthService.getNickByLoginAndPass("login1","pass1");
//            System.out.println(name);
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

    public void broadcastMsg(String msg) {

        ClientHandler destination = this.destinationPrivateMsg(msg);
        ClientHandler source = this.sourcePrivateMsg(msg);

        if (destination != null){

            String msgForSend = msg.substring(destination.getNick().length() + source.getNick().length() + 3);

            destination.sendMsg(source.getNick() + "> " + msgForSend);
            source.sendMsg(">" + destination.getNick() + "<" + msgForSend);
        }else {
            for (ClientHandler o : clients) {
                o.sendMsg(msg);
            }
        }
    }

    private ClientHandler destinationPrivateMsg(String msg){
        String[] tokens = msg.split(" ");

        if (tokens.length > 2) {
            for (ClientHandler o : clients) {
                if ( tokens[1].equals("/"+o.getNick() ) ){
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
