/**
 * Main class that launches the Tetris application.
 */
public class TetrisApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            StartScreen startScreen = new StartScreen();
            startScreen.setVisible(true);
        });
    }
}