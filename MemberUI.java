import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class MemberUI extends JFrame {
    private JTextField searchField;
    private JList<String> bookList;
    private JTable userInfoTable;
    private DefaultListModel<String> bookListModel;
    private ArrayList<String> cart;
    private Connection conn;
    private Member member;

    public MemberUI(Member member_in) {
        // Set Current Connection and Member
        conn = DatabaseManager.getConnection();
        member = member_in;

        setTitle("Member Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cart = new ArrayList<>();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Book Browsing Tab
        JPanel bookBrowsingPanel = new JPanel(new BorderLayout());
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

        JPanel actionsPanel = new JPanel();
        actionsPanel.add(addToCartButton);
        actionsPanel.add(checkoutButton);

        bookBrowsingPanel.add(searchPanel, BorderLayout.NORTH);
        bookBrowsingPanel.add(bookScrollPane, BorderLayout.CENTER);
        bookBrowsingPanel.add(actionsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Book Browsing", bookBrowsingPanel);

        // Borrowed Book Management Tab
        JPanel borrowedBooksPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> borrowedBooksListModel = new DefaultListModel<>();
        JList<String> borrowedBooksList = new JList<>(borrowedBooksListModel);
        JScrollPane borrowedBooksScrollPane = new JScrollPane(borrowedBooksList);

        // Return Book button
        JButton returnBookButton = new JButton("Return Book");
        returnBookButton.addActionListener(e -> returnBook(borrowedBooksList, borrowedBooksListModel));

        borrowedBooksPanel.add(borrowedBooksScrollPane, BorderLayout.CENTER);
        borrowedBooksPanel.add(returnBookButton, BorderLayout.SOUTH);

        tabbedPane.addTab("Borrowed Books", borrowedBooksPanel);

        // User Management Panel Tab
        JPanel userManagementPanel = new JPanel(new GridLayout(0, 2));
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(e -> changePassword());

        JButton changeAddressButton = new JButton("Change Address");
        changeAddressButton.addActionListener(e -> changeAddress());

        JButton changeEmailButton = new JButton("Change Email");
        changeEmailButton.addActionListener(e -> changeEmail());

        userManagementPanel.add(changePasswordButton);
        userManagementPanel.add(changeAddressButton);
        userManagementPanel.add(changeEmailButton);

        tabbedPane.addTab("User Management", userManagementPanel);

        // User Info Table
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Field", "Value"};
        Object[][] data = getUserInfo();
        userInfoTable = new JTable(data, columnNames){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane userInfoScrollPane = new JScrollPane(userInfoTable);

        userInfoPanel.add(userInfoScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("User Info", userInfoPanel);

        // Add Logout Button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            this.dispose();
            new LoginUI().setVisible(true);
        });
        add(logoutButton, BorderLayout.SOUTH);

        add(tabbedPane);
        }

        private Object[][] getUserInfo() {
        String query = "SELECT Name, Email, Phone, Address, MembershipDate FROM Members WHERE MemberID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, member.getMemberID());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            Object[][] data = {
                {"Name", rs.getString("Name")},
                {"Email", rs.getString("Email")},
                {"Phone", rs.getString("Phone")},
                {"Address", rs.getString("Address")},
                {"Membership Date", rs.getString("MembershipDate")}
            };
            return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[0][];
        }

        private void searchBooks() {
        String searchQuery = searchField.getText().toLowerCase();
        bookListModel.clear();

        String query = "SELECT * FROM Books WHERE LOWER(Title) LIKE ? OR LOWER(Author) LIKE ? OR LOWER(Genre) LIKE ? AND ISBN NOT IN (SELECT ISBN FROM Transactions WHERE MemberID = ? AND Status = 'Borrowed')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchQuery + "%");
            stmt.setString(2, "%" + searchQuery + "%");
            stmt.setString(3, "%" + searchQuery + "%");
            stmt.setInt(4, member.getMemberID());

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
            cart.clear(); // Clear the cart to ensure only one book at a time
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
                    String query = "UPDATE Books SET CopiesAvailable = CopiesAvailable - 1 WHERE Title = ? AND CopiesAvailable > 0";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
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

    private void returnBook(JList<String> borrowedBooksList, DefaultListModel<String> borrowedBooksListModel) {
        String selectedBook = borrowedBooksList.getSelectedValue();
        if (selectedBook != null) {
            try {
                String bookTitle = selectedBook.split(" by ")[0];
                String query = "UPDATE Books SET CopiesAvailable = CopiesAvailable + 1 WHERE Title = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, bookTitle);
                    stmt.executeUpdate();
                }

                String deleteTransactionSql = "DELETE FROM Transactions WHERE ISBN = (SELECT ISBN FROM Books WHERE Title = ?) AND MemberID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteTransactionSql)) {
                    stmt.setString(1, bookTitle);
                    stmt.setInt(2, member.getMemberID());
                    stmt.executeUpdate();
                }

                borrowedBooksListModel.removeElement(selectedBook);
                JOptionPane.showMessageDialog(this, "Book returned successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to return.");
        }
    }

    private void changePassword() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(this, passwordField, "Enter new password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        String newPassword = null;
        if (option == JOptionPane.OK_OPTION) {
            newPassword = new String(passwordField.getPassword());
        }

        if (newPassword != null && !newPassword.equals("")) {
            String hashedPassword = hashPassword(newPassword);
            String query = "UPDATE MemberPasswords SET Password = ? WHERE MemberID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, hashedPassword);
                stmt.setInt(2, member.getMemberID());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Password updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid password format. Please enter a valid, non-empty password.");
        }
    }

    private void changeAddress() {
        String newAddress = JOptionPane.showInputDialog(this, "Enter new address:");

        if (newAddress != null && !newAddress.equals("")) {
            String query = "UPDATE Members SET Address = ? WHERE MemberID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newAddress);
                stmt.setInt(2, member.getMemberID());
                stmt.executeUpdate();
                
                // Update the User Info Table to Reflect the Address Change
                Object[][] newData = getUserInfo();
                for (int i = 0; i < newData.length; i++) {
                    for (int j = 0; j < newData[i].length; j++) {
                        userInfoTable.setValueAt(newData[i][j], i, j);
                    }
                }

                JOptionPane.showMessageDialog(this, "Address updated successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid address format. Please enter a valid address.");
        }
    }

    private void changeEmail() {
        String newEmail = JOptionPane.showInputDialog(this, "Enter new email:");

        if (newEmail != null && !newEmail.equals("")) {
            if (newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                String query = "UPDATE Members SET Email = ? WHERE MemberID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newEmail);
                    stmt.setInt(2, member.getMemberID());
                    stmt.executeUpdate();

                    // Update the User Info Table to Reflect the Email Change
                    Object[][] newData = getUserInfo();
                    for (int i = 0; i < newData.length; i++) {
                        for (int j = 0; j < newData[i].length; j++) {
                            userInfoTable.setValueAt(newData[i][j], i, j);
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Email updated successfully.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email format. Please enter a valid email.");
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
    //         Member member = new Member(1, "Jane Doe"); // Example member
    //         new MemberUI(member).setVisible(true);
    //     });
    // }
}