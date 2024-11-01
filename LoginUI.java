import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleSelector;

    public LoginUI() {
        setTitle("GoodReads Library Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        userIdField = new JTextField(20);
        passwordField = new JPasswordField(20);
        roleSelector = new JComboBox<>(new String[]{"Librarian", "Member"});
        
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("User ID:"));
        panel.add(userIdField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleSelector);
        panel.add(loginButton);

        loginButton.addActionListener(new LoginAction());

        add(panel);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userId = userIdField.getText();
            String password = new String(passwordField.getPassword());
            String role = roleSelector.getSelectedItem().toString().toLowerCase();

            AuthService authService = new AuthService();
            User user = authService.authenticate(userId, password, role);

            if (user != null) {
                JOptionPane.showMessageDialog(LoginUI.this, "Welcome, " + user.getName());
                if (role.equals("librarian")) {
                    new LibrarianUI().setVisible(true);
                } else {
                    new MemberUI().setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginUI.this, "Invalid credentials.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
