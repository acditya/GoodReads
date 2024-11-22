import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class MemberUI extends JFrame {
    private JTextField searchField;
    private JList<String> bookList;
    private DefaultListModel<String> bookListModel;
    private ArrayList<String> cart;
    private Connection conn;
    private Member member;

    public MemberUI(Member member_in) {
        // Set Current Connection and Member
        conn = DatabaseManager.getConnection();
        member = member_in;

        setTitle("Member Dashboard");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cart = new ArrayList<>();
        initializeDatabaseConnection();

        JPanel panel = new JPanel(new BorderLayout());

        // Search bar and book display
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        bookListModel = new DefaultListModel<>();
        bookList = new JList<>(bookListModel);
        JScrollPane bookScrollPane = new JScrollPane(bookList);

        searchButton.addActionListener(e -> searchBooks());

        // Add to Cart button
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> addToCart());

        // Checkout button
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());

        // Transactions and borrowed books view button
        JButton viewTransactionsButton = new JButton("View Transactions");
        viewTransactionsButton.addActionListener(e -> viewTransactions());

        // Update email and password button
        JButton updateProfileButton = new JButton("Update Profile");
        updateProfileButton.addActionListener(e -> updateProfile());

        // Organize the layout
        panel.add(new JLabel("Welcome Member"), BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(bookScrollPane, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel();
        actionsPanel.add(addToCartButton);
        actionsPanel.add(checkoutButton);
        actionsPanel.add(viewTransactionsButton);
        actionsPanel.add(updateProfileButton);

        panel.add(actionsPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void initializeDatabaseConnection() {
        try {
            // Database credentials
            String url = "jdbc:mysql://localhost:3306/GoodReads";
            String membername = "root"; // Can also use 'root'
            String password = "GoodReads";

            // Establish the connection
            conn = DriverManager.getConnection(url, membername, password);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBooks() {
        String searchQuery = searchField.getText().toLowerCase();
        bookListModel.clear();
        
        String sql = "SELECT * FROM Books WHERE LOWER(Title) LIKE ? OR LOWER(Author) LIKE ? OR LOWER(Genre) LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchQuery + "%");
            stmt.setString(2, "%" + searchQuery + "%");
            stmt.setString(3, "%" + searchQuery + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bookDetails = rs.getString("Title") + " by " + rs.getString("Author") +
                        " - Genre: " + rs.getString("Genre");
                bookListModel.addElement(bookDetails);
            }

            if (bookListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No books found for the search query.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        String selectedBook = bookList.getSelectedValue();
        if (selectedBook != null) {
            cart.add(selectedBook);
            JOptionPane.showMessageDialog(this, "Book added to cart.");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to add to cart.");
        }
    }

    private void checkout() {
        if (!cart.isEmpty()) {
            try {
                for (String bookDetails : cart) {
                    // Example SQL query to update book status in the database
                    String bookTitle = bookDetails.split(" by ")[0];
                    String sql = "UPDATE Books SET CopiesAvailable = CopiesAvailable - 1 WHERE Title = ? AND CopiesAvailable > 0";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, bookTitle);
                        int rowsAffected = stmt.executeUpdate();

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "Checkout successful for " + bookTitle);
                        } else {
                            JOptionPane.showMessageDialog(this, "No copies available for " + bookTitle);
                        }
                    }
                }
                cart.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
        }
    }

    private void viewTransactions() {
        StringBuilder transactions = new StringBuilder("Your Transactions:\n");
        String sql = "SELECT Books.Title, Transactions.BorrowDate, Transactions.Status " +
                     "FROM Transactions JOIN Books ON Transactions.ISBN = Books.ISBN " +
                     "WHERE Transactions.MemberID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); // Replace with actual MemberID
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.append(rs.getString("Title")).append(" - ")
                            .append(rs.getString("BorrowDate")).append(" - ")
                            .append(rs.getString("Status")).append("\n");
            }

            JOptionPane.showMessageDialog(this, transactions.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProfile() {
        String newEmail = JOptionPane.showInputDialog(this, "Enter new email:");
        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
        String hashedPassword = hashPassword(newPassword);

        if (newEmail != null && hashedPassword != null) {
            String sql = "UPDATE Members SET Email = ? WHERE MemberID = ?";
            String sqlPassword = "UPDATE MemberPasswords SET Password = ? WHERE MemberID = ?";

            try (PreparedStatement stmtEmail = conn.prepareStatement(sql);
                 PreparedStatement stmtPassword = conn.prepareStatement(sqlPassword)) {

                stmtEmail.setString(1, newEmail);
                stmtEmail.setInt(2, 1); // Replace with actual MemberID
                stmtEmail.executeUpdate();

                stmtPassword.setString(1, hashedPassword);
                stmtPassword.setInt(2, 1); // Replace with actual MemberID
                stmtPassword.executeUpdate();

                JOptionPane.showMessageDialog(this, "Profile updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing hashing algorithm", e);
        }
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         MemberUI memberUI = new MemberUI(conn,);
    //         memberUI.setVisible(true);
    //     });
    // }
}
