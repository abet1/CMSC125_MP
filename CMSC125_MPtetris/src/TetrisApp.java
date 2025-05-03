/**
 * Main class that launches the Tetris application.
 */
public class TetrisApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
            game.startGame();
        });
    }
}