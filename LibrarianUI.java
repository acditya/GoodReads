import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LibrarianUI extends JFrame {
    private Connection conn;
    private Librarian librarian;

    public LibrarianUI(Librarian librarian_in) {
        // Set Current Connection and Librarian
        conn = DatabaseManager.getConnection();
        librarian = librarian_in;

        setTitle("Librarian Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Book Management Tab
        JPanel bookManagementPanel = new JPanel(new BorderLayout());
        bookManagementPanel.add(new JLabel("Book Management"), BorderLayout.NORTH);

        // Book Table
        String[] bookColumns = {"ID", "Title", "Author", "Total Count", "Available", "Borrowed"};
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
        JComboBox<String> bookSearchComboBox = new JComboBox<>(bookColumns);
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
                try {
                    String query = "SELECT * FROM Books WHERE " + searchAttribute + " LIKE ?";
                    DatabaseManager dbManager = new DatabaseManager();
                    ResultSet rs = dbManager.executeQuery(query, "%" + searchTerm + "%");

                    // Clear existing data
                    DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
                    model.setRowCount(0);

                    // Populate table with search results
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("ISBN"),
                            rs.getString("Title"),
                            rs.getString("Author"),
                            rs.getString("Genre"),
                            rs.getString("Publisher"),
                            rs.getInt("YearPublished"),
                            rs.getInt("CopiesAvailable")
                        });
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
                panel.add(new JLabel("Copies Available:"));
                panel.add(copiesField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Add Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String query = "INSERT INTO Books (Title, Author, Genre, Publisher, YearPublished, CopiesAvailable) VALUES (?, ?, ?, ?, ?, ?)";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(query, titleField.getText(), authorField.getText(), genreField.getText(), publisherField.getText(), Integer.parseInt(yearField.getText()), Integer.parseInt(copiesField.getText()));
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

                String ISBN = bookTable.getValueAt(selectedRow, 0).toString();
                JTextField titleField = new JTextField(bookTable.getValueAt(selectedRow, 1).toString(), 20);
                JTextField authorField = new JTextField(bookTable.getValueAt(selectedRow, 2).toString(), 20);
                JTextField genreField = new JTextField(bookTable.getValueAt(selectedRow, 3).toString(), 20);
                JTextField publisherField = new JTextField(bookTable.getValueAt(selectedRow, 4).toString(), 20);
                JTextField yearField = new JTextField(bookTable.getValueAt(selectedRow, 5).toString(), 4);
                JTextField copiesField = new JTextField(bookTable.getValueAt(selectedRow, 6).toString(), 4);

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
                panel.add(new JLabel("Copies Available:"));
                panel.add(copiesField);

                int result = JOptionPane.showConfirmDialog(null, panel, "Edit Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String query = "UPDATE Books SET Title = ?, Author = ?, Genre = ?, Publisher = ?, YearPublished = ?, CopiesAvailable = ? WHERE ISBN = ?";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(query, titleField.getText(), authorField.getText(), genreField.getText(), publisherField.getText(), Integer.parseInt(yearField.getText()), Integer.parseInt(copiesField.getText()), Integer.parseInt(ISBN));
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Book updated successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error updating book: " + ex.getMessage());
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
                        int userChoice = JOptionPane.showOptionDialog(
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
    
                        if (userChoice == 0) { // "Yes" selected
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
                        } else if (userChoice == 2) { // "Details" selected
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
    

        // User Management Tab
        JPanel userManagementPanel = new JPanel(new BorderLayout());
        userManagementPanel.add(new JLabel("User Management"), BorderLayout.NORTH);


        // User Table
        String[] userColumns = {"ID", "Name", "Email", "Phone", "Address", "Membership Date", "Password"};
        DefaultTableModel userTableModel = new DefaultTableModel(userColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable userTable = new JTable(userTableModel);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userManagementPanel.add(userScrollPane, BorderLayout.CENTER);

        // User Management Buttons
        JPanel userButtonPanel = new JPanel();
        JButton addUserButton = new JButton("Add User");
        JButton editUserButton = new JButton("Edit User");
        JButton deleteUserButton = new JButton("Delete User");
        userButtonPanel.add(addUserButton); // Add the new button here
        userButtonPanel.add(editUserButton);
        userButtonPanel.add(deleteUserButton); // Add the new button here
        userManagementPanel.add(userButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("User Management", userManagementPanel);

        add(tabbedPane);

        // Search Bar for User Management
        JPanel userSearchPanel = new JPanel();
        JTextField userSearchField = new JTextField(20);
        JComboBox<String> userSearchComboBox = new JComboBox<>(userColumns);
        JButton userSearchButton = new JButton("Search");
        userSearchPanel.add(new JLabel("Search by:"));
        userSearchPanel.add(userSearchComboBox);
        userSearchPanel.add(userSearchField);
        userSearchPanel.add(userSearchButton);
        userManagementPanel.add(userSearchPanel, BorderLayout.NORTH);

        // User Search Button Action Listener
        userSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = userSearchField.getText();
                String searchAttribute = (String) userSearchComboBox.getSelectedItem();
                // Implement search functionality for users
                JOptionPane.showMessageDialog(LibrarianUI.this, "Search User functionality to be implemented.");
            }
        });


        // Add User Button Action Listener
        addUserButton.addActionListener(new ActionListener() {
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

                int result = JOptionPane.showConfirmDialog(null, panel, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String insertUserQuery = "INSERT INTO Members (Name, Email, Phone, Address) VALUES (?, ?, ?, ?)";
                        String insertPasswordQuery = "INSERT INTO MemberPasswords (MemberID, Password) VALUES (LAST_INSERT_ID(), ?)";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(insertUserQuery, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText());
                        dbManager.executeUpdate(insertPasswordQuery, passwordField.getText());
                        JOptionPane.showMessageDialog(LibrarianUI.this, "User added successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error adding user: " + ex.getMessage());
                    }
                }

                // Update User data after changes pushed
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    Object[][] userData = dbManager.getUserData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        userTableModel.setRowCount(0);

                        for (Object[] row : userData) {
                            userTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching user data: " + ex.getMessage());
                    });
                }
            }
        });


        // Delete User Button Action Listener
        deleteUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a user to delete.");
                    return;
                }
        
                String userId = userTable.getValueAt(selectedRow, 0).toString();
                int result = JOptionPane.showConfirmDialog(
                    LibrarianUI.this,
                    "Are you sure you want to delete this user?",
                    "Delete User",
                    JOptionPane.YES_NO_OPTION
                );
        
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        // Step 1: Delete the user's password record
                        String deletePasswordQuery = "DELETE FROM MemberPasswords WHERE MemberID = ?";
                        
                        // Step 2: Delete the user from the Members table
                        String deleteUserQuery = "DELETE FROM Members WHERE MemberID = ?";
                        
                        // Initialize DatabaseManager
                        DatabaseManager dbManager = new DatabaseManager();
                        
                        // Execute deletion in the correct order
                        dbManager.executeUpdate(deletePasswordQuery, Integer.parseInt(userId)); // Delete from MemberPasswords
                        dbManager.executeUpdate(deleteUserQuery, Integer.parseInt(userId));    // Delete from Members
                        
                        // Notify the user of successful deletion
                        JOptionPane.showMessageDialog(LibrarianUI.this, "User deleted successfully.");
                    } catch (SQLException ex) {
                        // Handle any SQL exceptions
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error deleting user: " + ex.getMessage());
                    }
        
                    // Update User data after changes pushed
                    try {
                        DatabaseManager dbManager = new DatabaseManager();
                        Object[][] userData = dbManager.getUserData();
        
                        // Clear existing data and update the table
                        SwingUtilities.invokeLater(() -> {
                            userTableModel.setRowCount(0);
                            for (Object[] row : userData) {
                                userTableModel.addRow(row);
                            }
                        });
                    } catch (SQLException ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching user data: " + ex.getMessage());
                        });
                    }
                }
            }
        });


        
        // Edit User Button Action Listener
        editUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement edit user functionality
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(LibrarianUI.this, "Please select a user to edit.");
                    return;
                }

                String userId = userTable.getValueAt(selectedRow, 0).toString();
                JTextField nameField = new JTextField(userTable.getValueAt(selectedRow, 1).toString(), 20);
                JTextField emailField = new JTextField(userTable.getValueAt(selectedRow, 2).toString(), 20);
                JTextField phoneField = new JTextField(userTable.getValueAt(selectedRow, 3).toString(), 20);
                JTextField addressField = new JTextField(userTable.getValueAt(selectedRow, 4).toString(), 20);
                JTextField passwordField = new JTextField(userTable.getValueAt(selectedRow, 6).toString(), 20);

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

                int result = JOptionPane.showConfirmDialog(null, panel, "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String updateUserQuery = "UPDATE Members SET Name = ?, Email = ?, Phone = ?, Address = ? WHERE MemberID = ?";
                        String updatePasswordQuery = "UPDATE MemberPasswords SET Password = ? WHERE MemberID = ?";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(updateUserQuery, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText(), Integer.parseInt(userId));
                        dbManager.executeUpdate(updatePasswordQuery, passwordField.getText(), Integer.parseInt(userId));
                        JOptionPane.showMessageDialog(LibrarianUI.this, "User updated successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error updating user: " + ex.getMessage());
                    }
                }

                // Update User data after changes pushed
                try {
                    DatabaseManager dbManager = new DatabaseManager();
                    Object[][] userData = dbManager.getUserData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        userTableModel.setRowCount(0);

                        for (Object[] row : userData) {
                            userTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching user data: " + ex.getMessage());
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
                    Object[][] userData = dbManager.getUserData();

                    // Clear existing data
                    SwingUtilities.invokeLater(() -> {
                        userTableModel.setRowCount(0);

                        for (Object[] row : userData) {
                            userTableModel.addRow(row);
                        }
                    });
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error fetching user data: " + ex.getMessage());
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
