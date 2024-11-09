import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.sql.*;
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
        Object[][] bookData = {}; // Replace with actual data
        JTable bookTable = new JTable(bookData, bookColumns);
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
                int result = JOptionPane.showConfirmDialog(LibrarianUI.this, "Are you sure you want to delete this book?", "Delete Book", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        String query = "DELETE FROM Books WHERE ISBN = ?";
                        DatabaseManager dbManager = new DatabaseManager();
                        dbManager.executeUpdate(query, Integer.parseInt(ISBN));
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Book deleted successfully.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LibrarianUI.this, "Error deleting book: " + ex.getMessage());
                    }
                }
            }
        });

        // User Management Tab
        JPanel userManagementPanel = new JPanel(new BorderLayout());
        userManagementPanel.add(new JLabel("User Management"), BorderLayout.NORTH);

        // User Table
        String[] userColumns = {"ID", "Name", "Email", "Phone", "Address", "Membership Date"};
        Object[][] userData = {}; // Replace with actual data
        JTable userTable = new JTable(userData, userColumns);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userManagementPanel.add(userScrollPane, BorderLayout.CENTER);

        // User Management Buttons
        JPanel userButtonPanel = new JPanel();
        JButton editUserButton = new JButton("Edit User");
        userButtonPanel.add(editUserButton);
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

        
        // Edit User Button Action Listener
        editUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement edit user functionality
                JOptionPane.showMessageDialog(LibrarianUI.this, "Edit User functionality to be implemented.");
            }
        });
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(new Runnable() {
    //         @Override
    //         public void run() {
    //             new LibrarianUI().setVisible(true);
    //         }
    //     });
    // }
}
