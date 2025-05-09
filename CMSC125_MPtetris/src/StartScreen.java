import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * The start screen window that appears before the main game.
 * Handles the initial UI and game launch.
 */
public class StartScreen extends JFrame {
    // Window dimensions
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;

    /**
     * Constructor sets up the start screen window and its components
     */
    public StartScreen() {
        setTitle("Tetris - Start Screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel setup with dark theme
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

        // Game title
        JLabel titleLabel = new JLabel("TETRIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Start button with custom styling
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.PLAIN, 20));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(200, 50));
        startButton.setBackground(new Color(52, 152, 219));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);

        // Create action for starting game
        Action startAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TetrisGame game = new TetrisGame();
                game.setVisible(true);
                game.startGame();
                dispose(); // Close start screen
            }
        };

        // Add action listener to start button
        startButton.addActionListener(startAction);

        // Add key binding for Enter key
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startGame");
        mainPanel.getActionMap().put("startGame", startAction);

        // Add components to main panel
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(startButton);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        
        // Make sure the panel can receive focus for key bindings
        mainPanel.setFocusable(true);
        mainPanel.requestFocusInWindow();
    }

    /**
     * Main method to launch the start screen
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen startScreen = new StartScreen();
            startScreen.setVisible(true);
        });
    }
} 