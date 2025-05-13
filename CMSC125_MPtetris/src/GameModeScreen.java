import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Screen for selecting game mode (1 player or 2 players)
 */
public class GameModeScreen extends JFrame {
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;

    public GameModeScreen() {
        setTitle("Tetris - Select Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel setup
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

        JLabel titleLabel = new JLabel("Select Mode");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton singlePlayerBtn = createButton("1 Player");
        JButton twoPlayerBtn = createButton("2 Players");

        Action singlePlayerAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSinglePlayer();
            }
        };

        Action twoPlayerAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTwoPlayer();
            }
        };

        singlePlayerBtn.addActionListener(singlePlayerAction);
        twoPlayerBtn.addActionListener(twoPlayerAction);

        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "singlePlayer");
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "twoPlayer");
        mainPanel.getActionMap().put("singlePlayer", singlePlayerAction);
        mainPanel.getActionMap().put("twoPlayer", twoPlayerAction);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(singlePlayerBtn);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(twoPlayerBtn);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        mainPanel.setFocusable(true);
        mainPanel.requestFocusInWindow();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 20));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void startSinglePlayer() {
        TetrisGame game = new TetrisGame();
        game.setVisible(true);
        game.startGame();
        dispose();
    }

    private void startTwoPlayer() {
        TwoPlayerTetrisGame game = new TwoPlayerTetrisGame();
        game.setVisible(true);
        game.startGame();
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModeScreen screen = new GameModeScreen();
            screen.setVisible(true);
        });
    }
} 