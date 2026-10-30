import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentGUI gui = new StudentGUI();
            new StudentController(gui);
            gui.setVisible(true);
        });
    }
}
