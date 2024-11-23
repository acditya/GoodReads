import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class MemberUI extends JFrame {
    private JTextField searchField;
    private JList<String> bookList;
    private JTable userInfoTable;
    private DefaultListModel<String> bookListModel;
    private ArrayList<String> cart;
    private Connection conn;
    private Member member;
    private DefaultListModel<String> cartListModel;
    private JList<String> cartList;
    String[] filters = {"Title", "Author", "Genre", "Publisher", "YearPublished"};
    JComboBox<String> filterComboBox = new JComboBox<>(filters);
    private Timer refreshTimer;
    private String lastSearchQuery = ""; // Stores the last search text
    private String lastSearchFilter = "Title"; // Stores the last selected filter (default to "Title")



    public MemberUI(Member member_in) {
        // Set Current Connection and Member
        conn = DatabaseManager.getConnection();
        member = member_in;

        setTitle("Member Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cart = new ArrayList<>();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Initialize cartListModel
        cartListModel = new DefaultListModel<>();
        cartList = new JList<>(cartListModel);

        // Book Browsing tab
        bookListModel = new DefaultListModel<>();
        bookList = new JList<>(bookListModel);
        setCustomListRenderer(); // Apply custom renderer for LOW STOCK

        JPanel bookBrowsingPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        // Add a JComboBox for filter options
        JLabel filterLabel = new JLabel("Filter by:");
        searchPanel.add(filterLabel);
        searchPanel.add(filterComboBox);


        // Refresh books on search
        searchButton.addActionListener(e -> searchBooks());

        JScrollPane bookScrollPane = new JScrollPane(bookList);

        // Add to Cart button
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> addToCart());

        // Cart Display Panel
        JScrollPane cartScrollPane = new JScrollPane(cartList);
        cartScrollPane.setPreferredSize(new Dimension(200, 200));
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JLabel("Cart:"), BorderLayout.NORTH);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Checkout button
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());

        JPanel actionsPanel = new JPanel();
        actionsPanel.add(addToCartButton);
        actionsPanel.add(checkoutButton);

        // Split pane to include book list and cart display
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(bookScrollPane);
        splitPane.setRightComponent(cartPanel);
        splitPane.setResizeWeight(0.7);

        bookBrowsingPanel.add(searchPanel, BorderLayout.NORTH);
        bookBrowsingPanel.add(splitPane, BorderLayout.CENTER);
        bookBrowsingPanel.add(actionsPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Book Browsing", bookBrowsingPanel);

        // Borrowed Book Management Tab
        JPanel borrowedBooksPanel = new JPanel(new BorderLayout());
        DefaultListModel<String> borrowedBooksListModel = new DefaultListModel<>();
        JList<String> borrowedBooksList = new JList<>(borrowedBooksListModel);
        JScrollPane borrowedBooksScrollPane = new JScrollPane(borrowedBooksList);

        // Fetch Borrowed Books
        fetchBorrowedBooks(borrowedBooksListModel);

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
        userInfoTable = new JTable(data, columnNames) {
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

        // Fetch available books on startup
        fetchAvailableBooks();

        // Initialize a single Timer for semi-real-time updates
        int refreshInterval = 5000; // Refresh every 5 seconds
        refreshTimer = new Timer(refreshInterval, e -> {
            fetchAvailableBooks(); // Refresh Book Browsing
            fetchBorrowedBooks(borrowedBooksListModel); // Refresh Borrowed Books
        });
        refreshTimer.start();

        // Ensure the timer stops when the UI is closed
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });
        }

    private void setCustomListRenderer() {
        bookList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String bookDetails = value.toString();

                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else if (bookDetails.contains("LOW STOCK")) {
                    label.setForeground(Color.RED); // Set red for LOW STOCK
                } else {
                    label.setForeground(Color.BLACK); // Default text color
                }

                return label;
            }
        });
    }

    private void addToCart() {
        String selectedBook = bookList.getSelectedValue();
        if (selectedBook != null) {
            if (!cart.contains(selectedBook)) {
                cart.add(selectedBook);
                cartListModel.addElement(selectedBook); // Update the cart list model
                JOptionPane.showMessageDialog(this, "Book added to cart.");
            } else {
                JOptionPane.showMessageDialog(this, "This book is already in your cart.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to add to cart.");
        }
    }

    private void fetchAvailableBooks() {
        bookListModel.clear();
    
        String columnName = switch (lastSearchFilter.toLowerCase()) {
            case "title" -> "Title";
            case "author" -> "Author";
            case "genre" -> "Genre";
            case "publisher" -> "Publisher";
            case "yearpublished" -> "YearPublished"; // Handle YearPublished
            default -> "Title"; // Default to Title if no match
        };
    
        String query = "SELECT Title, Author, Genre, YearPublished, CopiesAvailable FROM Books " +
                "WHERE LOWER(" + columnName + ") LIKE ? AND CopiesAvailable > 0";
    
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + lastSearchQuery + "%"); // Use LIKE for all filters, including YearPublished
    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String genre = rs.getString("Genre");
                int yearPublished = rs.getInt("YearPublished");
                int copiesAvailable = rs.getInt("CopiesAvailable");
    
                String bookDetails = title + " by " + author + " - Genre: " + genre + " - Year: " + yearPublished;
                if (copiesAvailable < 2) {
                    bookDetails += " (LOW STOCK)";
                }
                bookListModel.addElement(bookDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while fetching available books.");
        }
    }
    
    
    
    private void searchBooks() {
        lastSearchQuery = searchField.getText().toLowerCase();
        lastSearchFilter = filterComboBox.getSelectedItem().toString(); // Save selected filter
    
        bookListModel.clear();
    
        String columnName = switch (lastSearchFilter.toLowerCase()) {
            case "title" -> "Title";
            case "author" -> "Author";
            case "genre" -> "Genre";
            case "publisher" -> "Publisher";
            case "yearpublished" -> "YearPublished"; // Add YearPublished filter
            default -> "Title"; // Default to Title if no match
        };
    
        String query = "SELECT Title, Author, Genre, YearPublished, CopiesAvailable FROM Books " +
                "WHERE LOWER(" + columnName + ") LIKE ? AND CopiesAvailable > 0";
    
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + lastSearchQuery + "%"); // Use LIKE for all filters, including YearPublished
    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String genre = rs.getString("Genre");
                int yearPublished = rs.getInt("YearPublished");
                int copiesAvailable = rs.getInt("CopiesAvailable");
    
                String bookDetails = title + " by " + author + " - Genre: " + genre + " - Year: " + yearPublished;
                if (copiesAvailable < 2) {
                    bookDetails += " (LOW STOCK)";
                }
                bookListModel.addElement(bookDetails);
            }
    
            if (bookListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No books found for the search query.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while searching for books.");
        }
    }
    
    
    
    

    private Object[][] getUserInfo() {
        String query = "SELECT Name, Email, Phone, Address, MembershipDate FROM Members WHERE MemberID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, member.getMemberID());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Object[][]{
                        {"Name", rs.getString("Name")},
                        {"Email", rs.getString("Email")},
                        {"Phone", rs.getString("Phone")},
                        {"Address", rs.getString("Address")},
                        {"Membership Date", rs.getString("MembershipDate")}
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[0][];
    }

   private void fetchBorrowedBooks(DefaultListModel<String> borrowedBooksListModel) {
    String query = "SELECT b.Title, MAX(t.BorrowDate) AS LastBorrowDate " +
            "FROM Transactions t " +
            "JOIN Books b ON t.ISBN = b.ISBN " +
            "WHERE t.MemberID = ? AND t.Status = 'Borrowed' " +
            "GROUP BY b.Title";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, member.getMemberID());
        ResultSet rs = stmt.executeQuery();

        ArrayList<String> newList = new ArrayList<>();
        while (rs.next()) {
            String bookDetails = rs.getString("Title") + " - Borrowed on: " + rs.getDate("LastBorrowDate");
            newList.add(bookDetails);
        }

        // Update the list only if there are changes
        if (!newList.equals(Collections.list(borrowedBooksListModel.elements()))) {
            borrowedBooksListModel.clear();
            for (String book : newList) {
                borrowedBooksListModel.addElement(book);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "An error occurred while fetching borrowed books.");
    }
}


    private void returnBook(JList<String> borrowedBooksList, DefaultListModel<String> borrowedBooksListModel) {
        String selectedBook = borrowedBooksList.getSelectedValue();
        if (selectedBook != null) {
            try {
                String bookTitle = selectedBook.split(" - ")[0];

                String isbnQuery = "SELECT ISBN FROM Books WHERE Title = ?";
                int isbn = -1;
                try (PreparedStatement stmt = conn.prepareStatement(isbnQuery)) {
                    stmt.setString(1, bookTitle);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        isbn = rs.getInt("ISBN");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: Book not found in the database.");
                        return;
                    }
                }

                String updateTransactionQuery = "UPDATE Transactions SET Status = 'Returned', ReturnDate = ? " +
                        "WHERE MemberID = ? AND ISBN = ? AND Status = 'Borrowed'";
                try (PreparedStatement stmt = conn.prepareStatement(updateTransactionQuery)) {
                    stmt.setDate(1, new java.sql.Date(System.currentTimeMillis())); // Current date as ReturnDate
                    stmt.setInt(2, member.getMemberID());
                    stmt.setInt(3, isbn);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Book returned successfully: " + bookTitle);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to return the book: " + bookTitle);
                        return;
                    }
                }

                String updateBookQuery = "UPDATE Books SET CopiesAvailable = CopiesAvailable + 1 WHERE ISBN = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateBookQuery)) {
                    stmt.setInt(1, isbn);
                    stmt.executeUpdate();
                }

                fetchBorrowedBooks(borrowedBooksListModel);

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while returning the book.");
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
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing hashing algorithm", e);
        }
    }

    private void checkout() {
        if (!cart.isEmpty()) {
            try {
                for (String bookDetails : cart) {
                    // Extract book title
                    String bookTitle = bookDetails.split(" by ")[0];
    
                    // Query to find the ISBN of the selected book
                    String isbnQuery = "SELECT ISBN FROM Books WHERE Title = ?";
                    int isbn = -1;
                    try (PreparedStatement stmt = conn.prepareStatement(isbnQuery)) {
                        stmt.setString(1, bookTitle);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            isbn = rs.getInt("ISBN");
                        } else {
                            JOptionPane.showMessageDialog(this, "Book not found in database: " + bookTitle);
                            continue; // Skip this book if ISBN not found
                        }
                    }
    
                    // Check if the member already borrowed the same book and has not returned it
                    String checkTransactionQuery = "SELECT * FROM Transactions WHERE MemberID = ? AND ISBN = ? AND Status = 'Borrowed'";
                    try (PreparedStatement stmt = conn.prepareStatement(checkTransactionQuery)) {
                        stmt.setInt(1, member.getMemberID());
                        stmt.setInt(2, isbn);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this, "You have already borrowed this book: " + bookTitle);
                            continue; // Skip this book if already borrowed
                        }
                    }
    
                    // Create a new transaction in the Transactions table
                    String transactionQuery = "INSERT INTO Transactions (MemberID, ISBN, BorrowDate, Status) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                        stmt.setInt(1, member.getMemberID());
                        stmt.setInt(2, isbn);
                        stmt.setDate(3, new java.sql.Date(System.currentTimeMillis())); // Current date as BorrowDate
                        stmt.setString(4, "Borrowed"); // Set status as "Borrowed"
                        stmt.executeUpdate();
                    }
    
                    // Update book availability in the Books table
                    String updateBookQuery = "UPDATE Books SET CopiesAvailable = CopiesAvailable - 1 WHERE ISBN = ? AND CopiesAvailable > 0";
                    try (PreparedStatement stmt = conn.prepareStatement(updateBookQuery)) {
                        stmt.setInt(1, isbn);
                        int rowsAffected = stmt.executeUpdate();
    
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "Checkout successful for " + bookTitle);
                        } else {
                            JOptionPane.showMessageDialog(this, "No copies available for " + bookTitle);
                        }
                    }
                }
    
                // Clear the cart after successful checkout
                cart.clear();
                cartListModel.clear();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred during checkout. Please try again.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
        }
    }
    
}




    
    

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         Member member = new Member(1, "Jane Doe"); // Example member
    //         new MemberUI(member).setVisible(true);
    //     });
    // }
