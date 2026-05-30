import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    // postgresql connection settings
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        try {
            Properties props = new Properties();
            props.load(DatabaseManager.class.getResourceAsStream("resources/db.properties"));
            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database config", e);
        }
    }

    /**
     * getConnection function returns a connection to the DB
     * @return a connection to the DB
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}