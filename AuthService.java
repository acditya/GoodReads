import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class AuthService {
    protected Librarian authenticateLibrarian(String id, String password) {
        String passwordQuery = "SELECT * FROM StaffPasswords WHERE StaffID = ? AND Password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(passwordQuery)) {

            stmt.setString(1, id);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String infoQuery = "SELECT * FROM Staff WHERE StaffID = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(infoQuery)) {
                    stmt2.setInt(1, rs.getInt("StaffID"));
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        return new Librarian(rs2.getInt("StaffID"), rs2.getString("Name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Member authenticateMember(String id, String password) {
        String passwordQuery = "SELECT * FROM MemberPasswords WHERE MemberID = ? AND Password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(passwordQuery)) {

            stmt.setString(1, id);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String infoQuery = "SELECT * FROM Members WHERE MemberID = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(infoQuery)) {
                    stmt2.setInt(1, rs.getInt("MemberID"));
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        if (rs2.getBoolean("Deleted")) {
                            JOptionPane.showMessageDialog(null, "This account has been deleted, please contact a librarian to restore it.");
                            return null;
                        }
                        if (!rs2.getBoolean("Authorized")) {
                            JOptionPane.showMessageDialog(null, "This account has not been authorized yet, please wait for authorization.");
                            return null;
                        }
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