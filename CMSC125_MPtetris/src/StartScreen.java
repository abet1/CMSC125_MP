import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * The start screen window that appears before the main game.
 * Handles the initial UI and game launch.
 */
public class StartScreen extends JFrame {
    // Window dimensions
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private CosmicEffects cosmicEffects;
    private Timer effectsTimer;
    private float titleGlow = 0;
    private boolean glowIncreasing = true;

    /**
     * Constructor sets up the start screen window and its components
     */
    public StartScreen() {
        setTitle("Tetris Effect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize cosmic effects
        cosmicEffects = new CosmicEffects(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Create main panel with custom painting
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw cosmic background
                cosmicEffects.draw(g2d);

                // Draw title with glow effect
                drawTitle(g2d);

                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(100, 20, 50, 20));

        // Create content panel for buttons
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(150, 0, 0, 0));

        // Create buttons
        JButton startButton = UITheme.createStyledButton("Start Game");
        JButton helpButton = UITheme.createStyledButton("How to Play");
        JButton exitButton = UITheme.createStyledButton("Exit");

        // Center align buttons
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add button actions
        startButton.addActionListener(e -> startGame());
        helpButton.addActionListener(e -> showHelp());
        exitButton.addActionListener(e -> System.exit(0));

        // Add vertical spacing between buttons
        contentPanel.add(startButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(helpButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(exitButton);

        mainPanel.add(contentPanel);

        // Add the main panel to the frame
        add(mainPanel);

        // Start animation timer
        effectsTimer = new Timer(16, e -> {
            cosmicEffects.update();
            updateTitleGlow();
            mainPanel.repaint();
        });
        effectsTimer.start();

        // Add key bindings
        addKeyBindings(mainPanel);
    }

    private void addKeyBindings(JPanel panel) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startGame");
        panel.getActionMap().put("startGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    private void drawTitle(Graphics2D g2d) {
        // Set up the title text
        String title = "TETRIS EFFECT";
        g2d.setFont(UITheme.TITLE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int x = (WINDOW_WIDTH - titleWidth) / 2;
        int y = 150;

        // Draw outer glow
        float alpha = 0.5f + (float)(Math.sin(titleGlow) + 1) * 0.25f;
        for (int i = 20; i > 0; i--) {
            Color glowColor = new Color(
                UITheme.ACCENT_PRIMARY.getRed(),
                UITheme.ACCENT_PRIMARY.getGreen(),
                UITheme.ACCENT_PRIMARY.getBlue(),
                (int)(alpha * 255 / i)
            );
            g2d.setColor(glowColor);
            g2d.drawString(title, x + i/2, y + i/2);
            g2d.drawString(title, x - i/2, y - i/2);
        }

        // Draw main text with gradient
        GradientPaint gradient = new GradientPaint(
            x, y - fm.getAscent(),
            UITheme.ACCENT_PRIMARY,
            x + titleWidth, y,
            UITheme.ACCENT_SECONDARY
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, x, y);

        // Draw subtitle
        String subtitle = "A Cosmic Journey";
        g2d.setFont(UITheme.SUBTITLE_FONT);
        fm = g2d.getFontMetrics();
        int subtitleWidth = fm.stringWidth(subtitle);
        x = (WINDOW_WIDTH - subtitleWidth) / 2;
        y += 40;

        g2d.setColor(UITheme.TEXT_SECONDARY);
        g2d.drawString(subtitle, x, y);
    }

    private void updateTitleGlow() {
        if (glowIncreasing) {
            titleGlow += 0.05f;
            if (titleGlow >= Math.PI) glowIncreasing = false;
        } else {
            titleGlow -= 0.05f;
            if (titleGlow <= 0) glowIncreasing = true;
        }
    }

    private void startGame() {
        cleanup();
        GameModeScreen modeScreen = new GameModeScreen();
        modeScreen.setVisible(true);
        dispose();
    }

    private void showHelp() {
        JDialog helpDialog = new JDialog(this, "How to Play", true);
        helpDialog.setSize(400, 500);
        helpDialog.setLocationRelativeTo(this);

        JPanel helpPanel = UITheme.createStyledPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add help content
        String[] helpContent = {
            "Controls:",
            "← → : Move piece left/right",
            "↑ : Rotate piece",
            "↓ : Soft drop",
            "Space : Hard drop",
            "C : Hold piece",
            "P : Pause game",
            "",
            "Scoring:",
            "1 line: 100 × level",
            "2 lines: 300 × level",
            "3 lines: 500 × level",
            "4 lines: 800 × level"
        };

        for (String text : helpContent) {
            JLabel label = UITheme.createStyledLabel(text, UITheme.TEXT_FONT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            helpPanel.add(label);
            helpPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JButton closeButton = UITheme.createStyledButton("Close");
        closeButton.addActionListener(e -> helpDialog.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        helpPanel.add(closeButton);

        helpDialog.add(helpPanel);
        helpDialog.setVisible(true);
    }

    private void cleanup() {
        if (effectsTimer != null) {
            effectsTimer.stop();
        }
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