import javax.swing.*;

public class LibrarianUI extends JFrame {
    public LibrarianUI() {
        setTitle("Librarian Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Welcome Librarian"));
        
        // Additional librarian functionalities (e.g., book management, transactions) go here

        add(panel);
    }
}