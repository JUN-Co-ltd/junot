// CHECKSTYLE:OFF
package test.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectController {
    private static DBConnectController dbCon = new DBConnectController();

    private static final String DB_CONNECT_URI = "jdbc:mysql://localhost:3306/junot?useUnicode=true&amp;characterEncoding=utf8mb4";
    private static final String DB_CONNECT_USER = "root";
    private static final String DB_CONNECT_PWD = "";

    private Connection conn = null;

    private DBConnectController() {

    }

    public static DBConnectController getInstance() {
        return dbCon;
    }

    public Connection getConnect() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection(DB_CONNECT_URI, DB_CONNECT_USER, DB_CONNECT_PWD);
        }
        return conn;
    }

}
//CHECKSTYLE:ON
