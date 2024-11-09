import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleSelector;
    private Member member;
    private Librarian librarian;

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
            boolean isAuthenticated = false;

            AuthService authService = new AuthService();
            if (role.equals("librarian")) {
                LoginUI.this.librarian =  authService.authenticateLibrarian(userId, password);
                isAuthenticated = LoginUI.this.librarian != null;
            } else { // role is member
                LoginUI.this.member = authService.authenticateMember(userId, password);
                isAuthenticated = LoginUI.this.member != null;
            }

            // User user = authService.authenticate(userId, password, role);
            try{
                if (isAuthenticated) {
                    if (role.equals("librarian")) {
                        System.out.println("Welcome, " + LoginUI.this.librarian + " " + LoginUI.this.librarian.getName());
                        JOptionPane.showMessageDialog(LoginUI.this, "Welcome, " + LoginUI.this.librarian.getName());
                        new LibrarianUI(LoginUI.this.librarian).setVisible(true);
                    } else {
                        System.out.println("Welcome, " + LoginUI.this.member + " " + LoginUI.this.member.getName());
                        JOptionPane.showMessageDialog(LoginUI.this, "Welcome, " + LoginUI.this.member.getName());
                        new MemberUI(LoginUI.this.member).setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginUI.this, "Invalid Credentials.");
                }
            } catch(Exception e1){
                JOptionPane.showMessageDialog(LoginUI.this, "An error has occured"+ e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}