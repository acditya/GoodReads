import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public User authenticate(String id, String password, String role) {
        String table = role.equals("librarian") ? "StaffPasswords" : "MemberPasswords";
        String idColumn = role.equals("librarian") ? "StaffID" : "MemberID";
        String passwordQuery = "SELECT * FROM " + table + " WHERE " + idColumn + " = ? AND Password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(passwordQuery)) {

            stmt.setString(1, id);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
        
            if (rs.next()) {
                if (role.equals("librarian")) {
                    String infoQuery = "SELECT * FROM Staff WHERE StaffID=" + rs.getInt(idColumn);

                    try(PreparedStatement stmt2 = conn.prepareStatement(infoQuery);
                        ResultSet rs2 = stmt2.executeQuery()) {
                        rs2.next();
                        return new Librarian(rs2.getInt("StaffID"), rs2.getString("Name"));
                    }
                } else {
                    String infoQuery = "SELECT * FROM Members WHERE MemberID=" + rs.getInt(idColumn);

                    try(PreparedStatement stmt2 = conn.prepareStatement(infoQuery);
                        ResultSet rs2 = stmt2.executeQuery()) {
                        rs2.next();
                        return new Member(rs2.getInt("MemberID"), rs2.getString("Name"), rs2.getString("Address"), rs2.getString("Phone"), rs2.getString("Email"), rs2.getString("MembershipDate"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
