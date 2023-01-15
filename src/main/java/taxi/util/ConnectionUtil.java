package taxi.util;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtil {
    private static final String URL = "YOUR_URL";
    private static final String USERNAME = "YOUR_USERNAME";
    private static final String PASSWORD = "YOUR_PASSWORD";

    public static Connection getConnection() {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL(URL);
        mysqlDataSource.setPassword(PASSWORD);
        mysqlDataSource.setUser(USERNAME);
        try {
            return mysqlDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Can't create connection to DB ", e);
        }
    }
}
