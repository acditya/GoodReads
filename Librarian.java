
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class Librarian extends User {
    public Librarian(int id, String name) {
        super(id, name);
    }

    public static void AddBooks(Book book){
        String insertSQL = "INSERT INTO Books (Title, Author, Genre, Publisher, YearPublished, CopiesAvailable) VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection connection = DatabaseManager.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)){

            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setString(3, book.getGenre());
            preparedStatement.setString(4, book.getPublisher());
            preparedStatement.setInt(5, book.getYearPublished());
            preparedStatement.setInt(6, book.getCopiesAvailable());

            preparedStatement.executeUpdate();
            }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to add a book.");
            }
     }
     
    public void RemoveBooks(Book book){
        
     }
       public void updateBookInfo(Book book, String title, String author, String genre, String publisher, int yearPublished, int copiesAvailable) {
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenre(genre);
            book.setPublisher(publisher);
            book.setYearPublished(yearPublished);
            book.setCopiesAvailable(copiesAvailable);
     }

     public boolean addMember(Member member) {
        String query = "INSERT INTO Members (Name, Address, Phone, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getAddress());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getEmail());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if insertion is successful
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to add a member.");
            return false;
        }
    }
    
    public boolean modifyMember(Member member) {
        String query = "UPDATE Members SET Name = ?, Address = ?, Phone = ?, Email = ? WHERE MemberID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getAddress());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getEmail());
            stmt.setInt(5, member.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if update is successful
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to modify a member.");
            return false;
        }
    }
    
    public boolean deleteMember(int memberId) {
        String query = "DELETE FROM Members WHERE MemberID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, memberId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if deletion is successful
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "An error occurred while trying to delete a member.");
            return false;
        }
    }
    
     
     @Override
    public String getRole() {
        return "Librarian";
    }
}
