package server;

import java.sql.*;
import java.util.List;

public class AuthService {
    private static Connection connection;
    private static Statement statement;

    public static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) {
        String sql = String.format("select nickname from main where login = '%s' and password = '%s'", login, pass);
        try {
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void fillBlackList(List<String> blackList, String nickname){
        String sql = String.format("SELECT nickIgnore FROM blackList WHERE nickname = '%s'", nickname);

        try {
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()){
                blackList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static boolean addUserInTableBlackList(String nickName, String nickIgnore){
        // проверка на наличе юзера в таблице main
        String sql = String.format("SELECT *FROM main WHERE nickname = '%s'", nickIgnore);

        try {
            ResultSet rs = statement.executeQuery(sql);
            if( !rs.next() ){
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // добавление юзера blackList таблицу
        sql = String.format("INSERT INTO blackList VALUES ('%s','%s')",nickName, nickIgnore);

        try {
            if ( statement.executeUpdate(sql) > 0 ) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean extractUserInTableBlackList(String nickName, String nickIgnore){
        // проверка на наличе юзера в таблице main
        String sql = String.format("SELECT *FROM main WHERE nickname = '%s'", nickIgnore);

        try {
            ResultSet rs = statement.executeQuery(sql);
            if( !rs.next() ){
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // удаляем юзера из blackList таблицы
        sql = String.format("DELETE FROM blackList WHERE nickname = '%s' AND nickIgnore = '%s'",nickName, nickIgnore);

        try {
            if ( statement.executeUpdate(sql) > 0 ) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
