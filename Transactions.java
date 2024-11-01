import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transactions{

    private int MemberId;
    private int BookID;
    private int transactionId;
    private String status;
    private String BorrowDate;
    private String ReturnDate;
    
    public Transactions(int memberId, int bookId, String status) {

        this.MemberId = memberId;
        this.BookID = bookId;
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

    public int getBookId() {
        return BookID;
    }

    public void setBookId(int bookId) {
        this.BookID = bookId;
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
            String sql = "INSERT INTO Transactions (MemberID, BookID, BorrowDate, Status) VALUES (?, ?, CURRENT_DATE, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.MemberId);
            stmt.setInt(2, this.BookID);
            stmt.setString(3, this.status);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the transaction was successfully created
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if an error occurred
        }
    }

}