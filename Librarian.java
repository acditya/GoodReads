
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            e.printStackTrace();
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
     
     @Override
    public String getRole() {
        return "Librarian";
    }
}
