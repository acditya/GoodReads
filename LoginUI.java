import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.awt.GridLayout;
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

        JPanel panel = new JPanel();
        memberIdField = new JTextField(20);
        passwordField = new JPasswordField(20);
        roleSelector = new JComboBox<>(new String[]{"Librarian", "Member"});
        
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");

        panel.add(new JLabel("Member ID:"));
        panel.add(memberIdField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleSelector);
        panel.add(loginButton);
        panel.add(signUpButton);

        loginButton.addActionListener(new LoginAction());
        signUpButton.addActionListener(new SignUpAction());

        add(panel);
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
                    JOptionPane.showMessageDialog(LoginUI.this, "Invalid Credentials.");
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
                throw new RuntimeException("Error initializing hashing algorithm", e);
            }
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

            int result = JOptionPane.showConfirmDialog(null, panel, "Sign Up", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String insertMemberQuery = "INSERT INTO Members (Name, Email, Phone, Address, Authorized) VALUES (?, ?, ?, ?, 0)";
                    String insertPasswordQuery = "INSERT INTO MemberPasswords (MemberID, Password) VALUES (LAST_INSERT_ID(), ?)";
                    DatabaseManager dbManager = new DatabaseManager();
                    dbManager.executeUpdate(insertMemberQuery, nameField.getText(), emailField.getText(), phoneField.getText(), addressField.getText());
                    dbManager.executeUpdate(insertPasswordQuery, hashPassword(new String(passwordField.getPassword())));
                    JOptionPane.showMessageDialog(LoginUI.this, "Sign up successful. Please wait for approval.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LoginUI.this, "Error signing up: " + ex.getMessage());
                }
            }
        }

            // Hashing method to hash the password using SHA-256
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}