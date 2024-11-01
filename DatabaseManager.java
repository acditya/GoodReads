import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/GoodReads";
    private static final String USER = "GoodReads"; // Can also use 'root'
    private static final String PASSWORD = "GoodReads";
    private static Connection connection = null;

    static {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public ResultSet executeQuery(String query, Object... parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement.executeQuery();
    }

    public int executeUpdate(String query, Object... parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement.executeUpdate();
    }

    public static void main(String[] args) {
        try {
            Connection conn = DatabaseManager.getConnection();
            if (conn != null) {
                System.out.println("Connection is valid: " + conn.isValid(2));
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.println("Error during connection validation: " + e.getMessage());
        }
    }
}