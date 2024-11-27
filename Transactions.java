import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class Transactions{

    private int MemberId;
    private int ISBN;
    private int transactionId;
    private String status;
    private String BorrowDate;
    private String ReturnDate;
    
    public Transactions(int memberId, int ISBN, String status) {

        this.MemberId = memberId;
        this.ISBN = ISBN;
        this.status = status;
    }
        // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getMemberId() {
        return MemberId;
    }

    public void setMemberId(int memberId) {
        this.MemberId = memberId;
    }

    public int getISBN() {
        return ISBN;
    }

    public void setISBN(int ISBN) {
        this.ISBN = ISBN;
    }

    public String getBorrowDate() {
        return BorrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.BorrowDate = borrowDate;
    }

    public String getReturnDate() {
        return ReturnDate;
    }

    public void setReturnDate(String returnDate) {
        this.ReturnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
        // Method to create a new transaction in the database
    public boolean createTransaction() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // SQL query to insert a new transaction
            String query = "INSERT INTO Transactions (MemberID, ISBN, BorrowDate, Status) VALUES (?, ?, CURRENT_DATE, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, this.MemberId);
            stmt.setInt(2, this.ISBN);
            stmt.setString(3, this.status);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the transaction was successfully created
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to create a transaction.");
            return false; // Return false if an error occurred
        }
    }

}