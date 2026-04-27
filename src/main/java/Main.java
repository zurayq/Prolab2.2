import ui.MainFrame;

import javax.swing.*;

/**
 * Main is the application entry point.
 *
 * It starts the Swing UI on the Event Dispatch Thread (EDT),
 * which is the correct and thread-safe way to launch Swing apps.
 */
public class Main {

    public static void main(String[] args) {
        // Use system look and feel so the app looks native on the student's OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default Swing look and feel — not critical
            System.out.println("Could not set system look and feel: " + e.getMessage());
        }

        // All Swing UI must be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
