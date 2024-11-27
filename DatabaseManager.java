import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/GoodReads";
    private static final String USER = "root"; // Can also use 'root'
    private static final String PASSWORD = "GoodReads";
    private static Connection connection = null;

    static {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            JOptionPane.showMessageDialog(null, "Connected to the database.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to connect to the database.");
        }
    }

    public static Connection getConnection() {
        // Check if the connection is valid before returning
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            } else {
                // Re-establish the connection if it's closed
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Reconnected to the database.");
                return connection;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to connect to the database.");
            return null;
        }
    }

    public ResultSet executeQuery(String query, Object... parameters) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeQuery();
        }
    }

    public int executeUpdate(String query, Object... parameters) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        }
    }

    public Object[][] getBookData() throws SQLException {
        String query = "SELECT ISBN, Title, Author, Genre, Publisher, YearPublished, TotalCopies, CopiesAvailable, " +
                       "(TotalCopies - CopiesAvailable) AS Borrowed, InformationUpdateTime FROM Books";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getInt("ISBN"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Genre"),
                    rs.getString("Publisher"),
                    rs.getInt("YearPublished"),
                    rs.getInt("TotalCopies"),
                    rs.getInt("CopiesAvailable"),
                    rs.getInt("Borrowed"), // Fetch the calculated Borrowed value
                    rs.getTimestamp("InformationUpdateTime")
                });
            }
            return data.toArray(new Object[0][]);
        }
    }

    public Object[][] getMemberData() throws SQLException {
        String query = "SELECT Members.MemberID, Members.Name, Members.Address, Members.Phone, Members.Email, Members.MembershipDate, MemberPasswords.Password, Members.Authorized, Members.Deleted, Members.InformationUpdateTime " +
                       "FROM Members " +
                       "JOIN MemberPasswords ON Members.MemberID = MemberPasswords.MemberID ";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getInt("MemberID"),
                    rs.getString("Name"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("Address"),
                    rs.getTimestamp("MembershipDate"),
                    rs.getInt("Authorized"),
                    rs.getInt("Deleted"),
                    rs.getTimestamp("InformationUpdateTime")
                });
            }
            return data.toArray(new Object[0][]);
        }
    }


    // public static void main(String[] args) {
    //     try {
    //         Connection conn = DatabaseManager.getConnection();
    //         if (conn != null) {
    //             JOptionPane.showMessageDialog(null, "Connection is valid.");
    //         } else {
    //             JOptionPane.showMessageDialog(null, "Connection is null.");
    //         }
    //     } catch (SQLException e) {
    //         JOptionPane.showMessageDialog(null, "An error occurred while trying to connect to the database.");
    //     }
    // }
}
