import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MemberUI extends JFrame {
    public MemberUI() {
        setTitle("Member Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Welcome Member"));
        
        // Additional member functionalities (e.g., search books) go here

        add(panel);
    }
}