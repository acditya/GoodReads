import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.sql.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.mail.PasswordAuthentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LibrarianUI extends JFrame {
    private Connection conn;
    private Librarian librarian;

    private void sendEmail(String recipientEmail, String subject, String message) {
        final String username = "root";
        final String password = "GoodReads";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            javax.mail.Transport.send(mimeMessage);
            JOptionPane.showMessageDialog(LibrarianUI.this, "Email sent successfully.");
        } catch (javax.mail.MessagingException e) {
            JOptionPane.showMessageDialog(LibrarianUI.this, "Error sending email: " + e.getMessage());
        }
    }

    public LibrarianUI(Librarian librarian_in) {
        // Set Current Connection and Librarian
        conn = DatabaseManager.getConnection();
        librarian = librarian_in;

        setTitle("Librarian Dashboard");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginUI().setVisible(true);
            }
        });
        add(logoutButton, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Book Management Tab
        JPanel bookManagementPanel = new JPanel(new BorderLayout());
        bookManagementPanel.add(new JLabel("Book Management"), BorderLayout.NORTH);

        // Book Table
        String[] bookColumns = {"ISBN", "Title", "Author", "Genre", "Publisher", "YearPublished", "TotalCopies", "CopiesAvailable", "Borrowed", "InformationUpdateTime"};
        DefaultTableModel bookTableModel = new DefaultTableModel(bookColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookTable = new JTable(bookTableModel);
        JScrollPane bookScrollPane = new JScrollPane(bookTable);
        bookManagementPanel.add(bookScrollPane, BorderLayout.CENTER);

        // Book Management Buttons
        JPanel bookButtonPanel = new JPanel();
        JButton addBookButton = new JButton("Add Book");
        JButton editBookButton = new JButton("Edit Book");
        JButton deleteBookButton = new JButton("Delete Book");
        bookButtonPanel.add(addBookButton);
        bookButtonPanel.add(editBookButton);
        bookButtonPanel.add(deleteBookButton);
        bookManagementPanel.add(bookButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Book Management", bookManagementPanel);

        // Search Bar for Book Management
        JPanel bookSearchPanel = new JPanel();
        JTextField bookSearchField = new JTextField(20);
        String[] searchColumns = {"ISBN", "Title", "Author", "Genre", "Publisher", "YearPublished", "TotalCopies", "CopiesAvailable"};
        JComboBox<String> bookSearchComboBox = new JComboBox<>(searchColumns);

        JButton bookSearchButton = new JButton("Search");
        bookSearchPanel.add(new JLabel("Search by:"));
        bookSearchPanel.add(bookSearchComboBox);
        bookSearchPanel.add(bookSearchField);
        bookSearchPanel.add(bookSearchButton);
        bookManagementPanel.add(bookSearchPanel, BorderLayout.NORTH);
        
        // Book Search Button Action Listener
        bookSearchButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchTerm = bookSearchField.getText();
            String searchAttribute = (String) bookSearchComboBox.getSelectedItem();
            String query = "SELECT * FROM Books WHERE " + searchAttribute + " LIKE ?";

            try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, "%" + searchTerm + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    // Clear existing data
                    DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
                    model.setRowCount(0);

                    if (!rs.isBeforeFirst()) { // Check if the ResultSet is empty
                        JOptionPane.showMessageDialog(LibrarianUI.this, "No books found with matching search conditions.");
                        return;
                    }

                    // Populate table with search results
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("ISBN"),
                            rs.getString("Title"),
                            rs.getString("Author"),
                            rs.getString("Genre"),
                            rs.getString("Publisher"),
                            rs.getInt("YearPublished"),
                            rs.getInt("TotalCopies"),
                            rs.getInt("CopiesAvailable"),
                            rs.getInt("TotalCopies") - rs.getInt("CopiesAvailable"), // Borrowed
                            rs.getTimestamp("InformationUpdateTime")
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LibrarianUI.this, "Error searching books: " + ex.getMessage());
            }
        }
        }); 

        // Add Book Button Action Listener
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField titleField = new JTextField(20);
                JTextField authorField = new JTextField(20);
                JTextField genreField = new JTextField(20);
                JTextField publisherField = new JTextField(20);
                JTextField yearField = new JTextField(4);
                JTextField copiesField = new JTextField(4);
        
                JPanel panel = new JPanel(new GridLayout(0, 2));
                panel.add(new JLabel("Title:"));
                panel.add(titleField);
                panel.add(new JLabel("Author:"));
                panel.add(authorField);
                panel.add(new JLabel("Genre:"));
                panel.add(genreField);
                panel.add(new JLabel("Publisher:"));
                panel.add(publisherField);
                panel.add(new JLabel("Year Published:"));
                panel.add(yearField);
                panel.add(new JLabel("Total Copies:"));
                panel.add(copiesField);
        
                int result = JOptionPane.showConfirmDialog(null, panel, "Add Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String query = "INSERT INTO Books (Title, Author, Genre, Publisher, YearPublished, TotalCopies, CopiesAvailable) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(query, 
                            titleField.getText(), 
                            authorField.getText(), 
                            genreField.getText(), 
                            publisherField.getText(), 
                            Integer.parseInt(yearField.getText()), 
                            Integer.parseInt(copiesField.getText()), 
                            Integer.parseInt(copiesField.getText())); // CopiesAvailable = TotalCopies initially
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Book added successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error adding book: " + ex.getMessage());
                    }
                }
        
                // Update Book Table After Changes
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    Object[][] bookData = dbManager.getBookData();
        
                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        bookTableModel.setRowCount(0);
        
                        for (Object[] row : bookData) {
                            bookTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching book data: " + ex.getMessage());
                    });
                }
            }
        });

        // Edit Book Button Action Listener
        editBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a book to edit.");
                    return;
                }
        
                // Fetch cell values with null handling
                Object isbnObject = bookTable.getValueAt(selectedRow, 0);
                Object titleObject = bookTable.getValueAt(selectedRow, 1);
                Object authorObject = bookTable.getValueAt(selectedRow, 2);
                Object genreObject = bookTable.getValueAt(selectedRow, 3);
                Object publisherObject = bookTable.getValueAt(selectedRow, 4);
                Object yearPublishedObject = bookTable.getValueAt(selectedRow, 5);
                Object totalCountObject = bookTable.getValueAt(selectedRow, 6);
                Object availableObject = bookTable.getValueAt(selectedRow, 7);
        
                // Default to empty strings or zeros for null values
                String isbn = (isbnObject != null) ? isbnObject.toString() : "0";
                String title = (titleObject != null) ? titleObject.toString() : "";
                String author = (authorObject != null) ? authorObject.toString() : "";
                String genre = (genreObject != null) ? genreObject.toString() : "";
                String publisher = (publisherObject != null) ? publisherObject.toString() : "";
                String yearPublished = (yearPublishedObject != null) ? yearPublishedObject.toString() : "0";
                String totalCount = (totalCountObject != null) ? totalCountObject.toString() : "0";
                String available = (availableObject != null) ? availableObject.toString() : "0";
        
                // Create text fields pre-filled with existing data
                JTextField isbnField = new JTextField(isbn, 20);
                JTextField titleField = new JTextField(title, 20);
                JTextField authorField = new JTextField(author, 20);
                JTextField genreField = new JTextField(genre, 20);
                JTextField publisherField = new JTextField(publisher, 20);
                JTextField yearPublishedField = new JTextField(yearPublished, 4);
                JTextField totalCountField = new JTextField(totalCount, 4);
                JTextField availableField = new JTextField(available, 4);
        
                // Create a panel to display input fields
                JPanel panel = new JPanel(new GridLayout(0, 2));
                panel.add(new JLabel("ISBN:"));
                panel.add(isbnField);
                panel.add(new JLabel("Title:"));
                panel.add(titleField);
                panel.add(new JLabel("Author:"));
                panel.add(authorField);
                panel.add(new JLabel("Genre:"));
                panel.add(genreField);
                panel.add(new JLabel("Publisher:"));
                panel.add(publisherField);
                panel.add(new JLabel("Year Published:"));
                panel.add(yearPublishedField);
                panel.add(new JLabel("Total Copies:"));
                panel.add(totalCountField);
                panel.add(new JLabel("Available Copies:"));
                panel.add(availableField);
        
                int result = JOptionPane.showConfirmDialog(null, panel, "Edit Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int newTotalCopies = Integer.parseInt(totalCountField.getText());
                        int newCopiesAvailable = Integer.parseInt(availableField.getText());
        
                        // Validation: TotalCopies cannot be less than CopiesAvailable
                        if (newTotalCopies < newCopiesAvailable) {
                            JOptionPane.showMessageDialog(LibrarianUI.this, "Total Copies cannot be less than Available Copies.");
                            return;
                        }
        
                        // Update the book record in the database
                        String updateQuery = "UPDATE Books SET Title = ?, Author = ?, Genre = ?, Publisher = ?, YearPublished = ?, TotalCopies = ?, CopiesAvailable = ? WHERE ISBN = ?";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(
                            updateQuery,
                            titleField.getText(),
                            authorField.getText(),
                            genreField.getText(),
                            publisherField.getText(),
                            Integer.parseInt(yearPublishedField.getText()),
                            newTotalCopies,
                            newCopiesAvailable,
                            Integer.parseInt(isbnField.getText())
                        );
        
                        JOptionPane.showMessageDialog(null, "Book updated successfully!");
        
                        // Refresh the table using DatabaseManager.getBookData
                        Object[][] bookData = dbManager.getBookData();
                        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
                        model.setRowCount(0); // Clear existing data
                        for (Object[] row : bookData) {
                            model.addRow(row);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage());
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Error updating book: " + ex.getMessage());
                    }
                }
            }
        });
        


    
       // Delete Book Button Action Listener
       deleteBookButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a book to delete.");
                return;
            }
    
            String ISBN = bookTable.getValueAt(selectedRow, 0).toString();
            int result = JOptionPane.showConfirmDialog(
                LibrarianUI.this,
                "Are you sure you want to delete this book?",
                "Delete Book",
                JOptionPane.YES_NO_OPTION
            );
    
            if (result == JOptionPane.YES_OPTION) {
                String query = null;
                try {
                    // Attempt to delete the book
                    query = "DELETE FROM Books WHERE ISBN = ?";
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.executeUpdate(query, ISBN);
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Book deleted successfully.");
                } catch (SQLException ex) {
                    // Check if the error is due to a foreign key constraint
                    boolean continueLoop = true;
                    while (continueLoop) {
                        Object[] options = {"Yes", "No", "Details"};
                        int memberChoice = JOptionPane.showOptionDialog(
                            LibrarianUI.this,
                            "Cannot delete this book because it has associated transactions.\n" +
                            "Do you want to forcefully delete it?",
                            "Foreign Key Constraint",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[2] // Default focus on "Details"
                        );
    
                        if (memberChoice == 0) { // "Yes" selected
                            try {
                                DatabaseManager dbManager = new DatabaseManager();
                                String cascadeQuery = "DELETE FROM Transactions WHERE ISBN = ?";
                                dbManager.executeUpdate(cascadeQuery, ISBN); // Delete associated transactions
                                dbManager.executeUpdate(query, ISBN); // Delete the book
                                JOptionPane.showMessageDialog(LibrarianUI.this, "Book and its associated transactions deleted successfully.");
                                continueLoop = false; // Exit the loop after successful deletion
                            } catch (SQLException forceEx) {
                                JOptionPane.showMessageDialog(LibrarianUI.this, "Error performing forced deletion: " + forceEx.getMessage());
                                continueLoop = false; // Exit the loop as deletion failed
                            }
                        } else if (memberChoice == 2) { // "Details" selected
                            JOptionPane.showMessageDialog(
                                LibrarianUI.this,
                                "Error Details:\n" + ex.getMessage(),
                                "Error Details",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            // Loop continues; dialog is re-displayed
                        } else { // "No" selected
                            continueLoop = false; // Exit the loop
                        }
                    }
                }
            }
        }
    });
    

        // Member Management Tab
        JPanel memberManagementPanel = new JPanel(new BorderLayout());
        memberManagementPanel.add(new JLabel("Member Management"), BorderLayout.NORTH);


        // Member Table
        String[] memberColumns = {"ID", "Name", "Email", "Phone", "Address", "Membership Date", "Password", "Authorized", "Deleted", "InformationUpdateTime"};
        DefaultTableModel memberTableModel = new DefaultTableModel(memberColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        JTable memberTable = new JTable(memberTableModel);
        memberTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering, for index-based column references that we make
        memberTable.setDefaultRenderer(Object.class, new MemberTableCellRenderer());
        JScrollPane memberScrollPane = new JScrollPane(memberTable);
        memberManagementPanel.add(memberScrollPane, BorderLayout.CENTER);

        // Member Management Buttons
        JPanel memberButtonPanel = new JPanel();
        JButton addMemberButton = new JButton("Add Member");
        JButton approveMemberButton = new JButton("Approve Member");
        JButton editMemberButton = new JButton("Edit Member");
        JButton deleteMemberButton = new JButton("Delete Member");
        memberButtonPanel.add(addMemberButton);
        memberButtonPanel.add(approveMemberButton);
        memberButtonPanel.add(editMemberButton);
        memberButtonPanel.add(deleteMemberButton); 
        memberManagementPanel.add(memberButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Member Management", memberManagementPanel);

        add(tabbedPane);

        // Search Bar for Member Management
        JPanel memberSearchPanel = new JPanel();
        JTextField memberSearchField = new JTextField(20);
        JComboBox<String> memberSearchComboBox = new JComboBox<>(memberColumns);
        JButton memberSearchButton = new JButton("Search");
        memberSearchPanel.add(new JLabel("Search by:"));
        memberSearchPanel.add(memberSearchComboBox);
        memberSearchPanel.add(memberSearchField);
        memberSearchPanel.add(memberSearchButton);
        memberManagementPanel.add(memberSearchPanel, BorderLayout.NORTH);

        // To reset search, just empty search bar and press search
        // Member Search Button Action Listener
        memberSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = memberSearchField.getText();
                String searchAttribute = (String) memberSearchComboBox.getSelectedItem();
                if(searchAttribute.equals("ID")){
                    searchAttribute = "MemberID";
                } else if (searchAttribute.equals("Membership Date")){
                    searchAttribute = "MembershipDate";
                }
                String searchTable = "Members";
                if(searchAttribute.equals("Password")){
                    searchTable = "MemberPasswords";
                }
                String query = "SELECT * FROM Members JOIN MemberPasswords ON Members.MemberID = MemberPasswords.MemberID WHERE " + searchTable + "." + searchAttribute + " LIKE ? ";
                
                try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(query)) {
                    
                    stmt.setString(1, "%" + searchTerm + "%");
                    try (ResultSet rs = stmt.executeQuery()) {
                        // Clear existing data
                        DefaultTableModel model = (DefaultTableModel) memberTable.getModel();
                        model.setRowCount(0);

                        if (!rs.isBeforeFirst()) { // Check if the ResultSet is empty
                            JOptionPane.showMessageDialog(LibrarianUI.this, "No members found with matching search conditions.");
                            return;
                        }

                        // Populate table with search results
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getInt("MemberID"),
                                rs.getString("Name"),
                                rs.getString("Email"),
                                rs.getString("Phone"),
                                rs.getString("Address"),
                                rs.getTimestamp("MembershipDate"),
                                rs.getString("Password"),
                                rs.getInt("Authorized"),
                                rs.getInt("Deleted"),
                                rs.getTimestamp("InformationUpdateTime"),
                            });
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Error searching members: " + ex.getMessage());
                }
            }
        });

        // Add Member Button Action Listener
        addMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField nameField = new JTextField(20);
                JTextField emailField = new JTextField(20);
                JTextField phoneField = new JTextField(20);
                JTextField addressField = new JTextField(20);
                JTextField passwordField = new JTextField(20);

                JPanel panel = new JPanel(new GridLayout(0, 2));
                panel.add(new JLabel("Name:"));
                panel.add(nameField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);
                panel.add(new JLabel("Phone:"));
                panel.add(phoneField);
                panel.add(new JLabel("Address:"));
                panel.add(addressField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Add Member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String insertMemberQuery = "INSERT INTO Members (Name, Email, Phone, Address) VALUES (?, ?, ?, ?)";
                        String insertPasswordQuery = "INSERT INTO MemberPasswords (MemberID, Password) VALUES (LAST_INSERT_ID(), ?)";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(insertMemberQuery, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText());
                        dbManager.executeUpdate(insertPasswordQuery, hashPassword(passwordField.getText()));
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Member added successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error adding member: " + ex.getMessage());
                    }
                }

                // Update Member data after changes pushed
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    Object[][] memberData = dbManager.getMemberData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        memberTableModel.setRowCount(0);

                        for (Object[] row : memberData) {
                            memberTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching member data: " + ex.getMessage());
                    });
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

        });


// Approve User Button Action Listener
approveMemberButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = memberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a member to approve.");
            return;
        }

        // Get MemberID as a String and parse it to Integer
        String memberIdString = memberTable.getValueAt(selectedRow, 0).toString();
        int memberId;
        try {
            memberId = Integer.parseInt(memberIdString);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(LibrarianUI.this, "Invalid MemberID: " + ex.getMessage());
            return;
        }

        // Get Authorized value as a String and parse it to Integer
        String authorizedString = memberTable.getValueAt(selectedRow, 7).toString();
        int authorized;
        try {
            authorized = Integer.parseInt(authorizedString);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(LibrarianUI.this, "Invalid Authorized value: " + ex.getMessage());
            return;
        }

        if (authorized == 1) {
            JOptionPane.showMessageDialog(LibrarianUI.this, "This member is already authorized.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(LibrarianUI.this, "Are you sure you want to approve this member?", "Approve Member", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                String approveUserQuery = "UPDATE Members SET Authorized = 1 WHERE MemberID = ?";
                DatabaseManager dbManager = new DatabaseManager();
                dbManager.executeUpdate(approveUserQuery, memberId);
                JOptionPane.showMessageDialog(LibrarianUI.this, "User approved successfully.");

                // Send email to the user with their details
                String recipientEmail = memberTable.getValueAt(selectedRow, 2).toString(); // Assuming email is at index 2
                String subject = "Library Membership Approved";
                String message = "Dear " + memberTable.getValueAt(selectedRow, 1).toString() + ",\n\n" +
                                 "Your library membership has been approved. You can now log in using the following details:\n" +
                                 "Email: " + recipientEmail + "\n" +
                                 "Password: [Hidden for security]\n\n" +
                                 "Best regards,\nLibrary Team";

                sendEmail(recipientEmail, subject, message);

                // Update Member data after changes
                Object[][] memberData = dbManager.getMemberData();
                SwingUtilities.invokeLater(() -> {
                    memberTableModel.setRowCount(0);
                    for (Object[] row : memberData) {
                        memberTableModel.addRow(row);
                    }
                });

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LibrarianUI.this, "Error approving member: " + ex.getMessage());
            }
        }
    }
});



        // Delete Member Button Action Listener
        deleteMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a member to delete.");
                    return;
                }
        
                String memberId = memberTable.getValueAt(selectedRow, 0).toString();
                int result = JOptionPane.showConfirmDialog(
                    LibrarianUI.this,
                    "Are you sure you want to delete this member?",
                    "Delete Member",
                    JOptionPane.YES_NO_OPTION
                );
        
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        // Step 1: Delete the member's password record
                        String deletePasswordQuery = "DELETE FROM MemberPasswords WHERE MemberID = ?";
                        
                        // Step 2: Delete the member from the Members table
                        String deleteMemberQuery = "DELETE FROM Members WHERE MemberID = ?";
                        
                        // Initialize DatabaseManager
                        DatabaseManager dbManager = new DatabaseManager();
                        
                        // Execute deletion in the correct order
                        dbManager.executeUpdate(deletePasswordQuery, Integer.parseInt(memberId)); // Delete from MemberPasswords
                        dbManager.executeUpdate(deleteMemberQuery, Integer.parseInt(memberId));    // Delete from Members
                        
                        // Notify the member of successful deletion
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Member deleted successfully.");
                    } catch (SQLException ex) {
                        // Handle any SQL exceptions
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error deleting member: " + ex.getMessage());
                    }
        
                    // Update Member data after changes pushed
                    try {
                        DatabaseManager dbManager = new DatabaseManager();
                        Object[][] memberData = dbManager.getMemberData();
        
                        // Clear existing data and update the table
                        SwingUtilities.invokeLater(() -> {
                            memberTableModel.setRowCount(0);
                            for (Object[] row : memberData) {
                                memberTableModel.addRow(row);
                            }
                        });
                    } catch (SQLException ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching member data: " + ex.getMessage());
                        });
                    }
                }
            }
        });


        
        // Edit Member Button Action Listener
        editMemberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement edit member functionality
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a member to edit.");
                    return;
                }

                String memberId = memberTable.getValueAt(selectedRow, 0).toString();
                JTextField nameField = new JTextField(memberTable.getValueAt(selectedRow, 1).toString(), 20);
                JTextField emailField = new JTextField(memberTable.getValueAt(selectedRow, 2).toString(), 20);
                JTextField phoneField = new JTextField(memberTable.getValueAt(selectedRow, 3).toString(), 20);
                JTextField addressField = new JTextField(memberTable.getValueAt(selectedRow, 4).toString(), 20);
                JTextField passwordField = new JTextField(memberTable.getValueAt(selectedRow, 6).toString(), 20);

                JPanel panel = new JPanel(new GridLayout(0, 2));
                panel.add(new JLabel("Name:"));
                panel.add(nameField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);
                panel.add(new JLabel("Phone:"));
                panel.add(phoneField);
                panel.add(new JLabel("Address:"));
                panel.add(addressField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Edit Member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String updateMemberQuery = "UPDATE Members SET Name = ?, Email = ?, Phone = ?, Address = ? WHERE MemberID = ?";
                        String updatePasswordQuery = "UPDATE MemberPasswords SET Password = ? WHERE MemberID = ?";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(updateMemberQuery, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText(), Integer.parseInt(memberId));
                        dbManager.executeUpdate(updatePasswordQuery, passwordField.getText(), Integer.parseInt(memberId));
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Member updated successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error updating member: " + ex.getMessage());
                    }
                }

                // Update Member data after changes pushed
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    Object[][] memberData = dbManager.getMemberData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        memberTableModel.setRowCount(0);

                        for (Object[] row : memberData) {
                            memberTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching member data: " + ex.getMessage());
                    });
            }
        }
    });


        // Set up a timer to refresh the book data periodically
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DatabaseManager dbManager = new DatabaseManager();

                try {
                    Object[][] bookData = dbManager.getBookData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        bookTableModel.setRowCount(0);

                        for (Object[] row : bookData) {
                            bookTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching book data: " + ex.getMessage());
                    });
                }

                try {
                    Object[][] memberData = dbManager.getMemberData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        memberTableModel.setRowCount(0);

                        for (Object[] row : memberData) {
                            memberTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching member data: " + ex.getMessage());
                    });
            }
        }
        
    }, 0, 30000); // Refresh every 30 seconds


    

    
    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(new Runnable() {
    //         @Override
    //         public void run() {
    //             new LibrarianUI().setVisible(true);
    //         }
    //     });
    // }
    }
}
