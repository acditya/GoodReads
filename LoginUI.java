import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class LoginUI extends JFrame {
    private JTextField memberIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleSelector;
    private Member member;
    private Librarian librarian;

    public LoginUI() {
        setTitle("GoodReads Library Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on screen

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Login"));
        panel.setBackground(new Color(240, 248, 255)); // Light blue background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Member ID Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Member ID:"), gbc);

        gbc.gridx = 1;
        memberIdField = new JTextField(20);
        panel.add(memberIdField, gbc);

        // Password Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Role Selector
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        roleSelector = new JComboBox<>(new String[]{"Librarian", "Member"});
        panel.add(roleSelector, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(240, 248, 255)); // Same as the panel background

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.setBackground(new Color(30, 144, 255)); // Dodger blue
        loginButton.setForeground(Color.WHITE);

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setPreferredSize(new Dimension(100, 30));
        signUpButton.setBackground(new Color(60, 179, 113)); // Medium sea green
        signUpButton.setForeground(Color.WHITE);

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        panel.add(buttonPanel, gbc);

        add(panel);

        // Button Listeners
        loginButton.addActionListener(new LoginAction());
        signUpButton.addActionListener(new SignUpAction());
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String memberId = memberIdField.getText();
            String password = new String(passwordField.getPassword());
            String role = roleSelector.getSelectedItem().toString().toLowerCase();
            boolean isAuthenticated = false;

            // Hash the password before sending it to AuthService
            String hashedPassword = hashPassword(password);

            AuthService authService = new AuthService();
            if (role.equals("librarian")) {
                LoginUI.this.librarian = authService.authenticateLibrarian(memberId, hashedPassword);
                isAuthenticated = LoginUI.this.librarian != null;
            } else { // role is member
                LoginUI.this.member = authService.authenticateMember(memberId, hashedPassword);
                isAuthenticated = LoginUI.this.member != null;
            }

            try {
                if (isAuthenticated) {
                    if (role.equals("librarian")) {
                        JOptionPane.showMessageDialog(LoginUI.this, "Welcome, " + LoginUI.this.librarian.getName());
                        new LibrarianUI(LoginUI.this.librarian).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginUI.this, "Welcome, " + LoginUI.this.member.getName());
                        new MemberUI(LoginUI.this.member).setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginUI.this, "Unable To Login.");
                }
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(LoginUI.this, "An error has occurred: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(LoginUI.this, "Error hashing password: " + e.getMessage());
            }

            return null;
        }
    }

    private class SignUpAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField nameField = new JTextField(20);
            JTextField emailField = new JTextField(20);
            JTextField phoneField = new JTextField(20);
            JTextField addressField = new JTextField(20);
            JPasswordField passwordField = new JPasswordField(20);

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
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

            int result = JOptionPane.showConfirmDialog(null, panel, "Sign Up", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);



            if (result == JOptionPane.OK_OPTION) {
                try {
                    String insertMemberQuery = "INSERT INTO Members (Name, Email, Phone, Address, Authorized) VALUES (?, ?, ?, ?, 0)";
                    String insertPasswordQuery = "INSERT INTO MemberPasswords (MemberID, Password) VALUES (LAST_INSERT_ID(), ?)";
                    DatabaseManager dbManager = new DatabaseManager();

                    String email = emailField.getText();
                    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        throw new IllegalArgumentException("Invalid email format.");
                    }

                    dbManager.executeUpdate(insertMemberQuery, nameField.getText(), email, phoneField.getText(), addressField.getText());
                    dbManager.executeUpdate(insertPasswordQuery, hashPassword(new String(passwordField.getPassword())));
                    JOptionPane.showMessageDialog(LoginUI.this, "Sign up successful. Please wait for approval.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LoginUI.this, "Error signing up: " + ex.getMessage());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(LoginUI.this, ex.getMessage());
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
            } catch (Exception e) {
                JOptionPane.showMessageDialog(LoginUI.this, "Error Hashing Password: " + e.getMessage());
            }

            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
