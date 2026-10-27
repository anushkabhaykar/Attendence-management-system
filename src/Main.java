import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import util.DBConnection;
import util.UIUtils;
import view.LoginFrame;

/**
 * Main application launcher for Face Recognition Attendance Management System.
 * Location: src/Main.java
 */
public class Main {

    public static void main(String[] args) {
        // Set Look and Feel
        setupModernLookAndFeel();

        // Launch Application on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Check Database Connectivity
            boolean dbConnected = DBConnection.testConnection();
            if (!dbConnected) {
                String dbInfo = DBConnection.getDisplayInfo();
                System.err.println("[Main] Warning: Could not connect to database (" + dbInfo + ").");
                String helpMsg = DBConnection.isSqlite()
                        ? "Could not initialize SQLite database (" + DBConnection.getDbName() + ")!\n\n"
                        + "Please ensure write permissions exist in the project directory."
                        : "Could not connect to MySQL database (" + DBConnection.getDbName() + ")!\n\n"
                        + "Please ensure:\n"
                        + "1. XAMPP Control Panel is open and MySQL is started.\n"
                        + "2. 'schema.sql' has been imported.";

                int choice = JOptionPane.showConfirmDialog(null,
                        helpMsg + "\n\nDo you still wish to launch the application interface?",
                        "Database Notice",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice != JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return;
                }
            } else {
                System.out.println("[Main] Database connection verified successfully (" + DBConnection.getDisplayInfo() + ").");
            }

            // Display Login Frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void setupModernLookAndFeel() {
        try {
            // Try FlatLaf for modern clean aesthetics
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
        } catch (Throwable t) {
            // Fallback to System Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }
}
