package edu.univ.erp;

import edu.univ.erp.ui.auth.LoginWindow;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatDarkLaf;
public class Main {

    public static void main(String[] args) {

        // --- 2. ADD THIS LINE ---
        // This sets the Look and Feel for the entire app.
        FlatDarkLaf.setup();

        // (Original code)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            }
        });
    }
}