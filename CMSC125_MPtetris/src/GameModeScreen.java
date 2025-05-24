import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Screen for selecting game mode (1 player or 2 players)
 */
public class GameModeScreen extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private CosmicEffects cosmicEffects;
    private Timer effectsTimer;
    private float selectionGlow = 0;
    private boolean glowIncreasing = true;

    public GameModeScreen() {
        setTitle("Tetris Effect - Select Mode");
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

                // Draw title
                drawTitle(g2d);

                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(100, 20, 50, 20));

        // Create mode selection panel
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setOpaque(false);
        modePanel.setBorder(BorderFactory.createEmptyBorder(150, 0, 0, 0));

        // Create mode buttons with descriptions
        addModeButton(modePanel, "Single Player", "Classic Tetris experience with cosmic visuals",
            e -> startSinglePlayer());
        addModeButton(modePanel, "Two Players", "Compete with a friend in split-screen mode",
            e -> startTwoPlayer());
        addModeButton(modePanel, "Back to Menu", "Return to the main menu",
            e -> returnToMenu());

        mainPanel.add(modePanel);
        add(mainPanel);

        // Start animation timer
        effectsTimer = new Timer(16, e -> {
            cosmicEffects.update();
            updateSelectionGlow();
            mainPanel.repaint();
        });
        effectsTimer.start();

        // Add key bindings
        addKeyBindings(mainPanel);
    }

    private void addModeButton(JPanel panel, String title, String description, ActionListener action) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton button = UITheme.createStyledButton(title);
        button.addActionListener(action);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = UITheme.createStyledLabel(description, UITheme.TEXT_FONT);
        descLabel.setForeground(UITheme.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(button);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(descLabel);

        panel.add(buttonPanel);
    }

    private void drawTitle(Graphics2D g2d) {
        String title = "SELECT MODE";
        g2d.setFont(UITheme.TITLE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int x = (WINDOW_WIDTH - titleWidth) / 2;
        int y = 150;

        // Draw outer glow
        float alpha = 0.5f + (float)(Math.sin(selectionGlow) + 1) * 0.25f;
        for (int i = 15; i > 0; i--) {
            Color glowColor = new Color(
                UITheme.ACCENT_SECONDARY.getRed(),
                UITheme.ACCENT_SECONDARY.getGreen(),
                UITheme.ACCENT_SECONDARY.getBlue(),
                (int)(alpha * 255 / i)
            );
            g2d.setColor(glowColor);
            g2d.drawString(title, x + i/2, y + i/2);
            g2d.drawString(title, x - i/2, y - i/2);
        }

        // Draw main text with gradient
        GradientPaint gradient = new GradientPaint(
            x, y - fm.getAscent(),
            UITheme.ACCENT_SECONDARY,
            x + titleWidth, y,
            UITheme.ACCENT_PRIMARY
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, x, y);
    }

    private void updateSelectionGlow() {
        if (glowIncreasing) {
            selectionGlow += 0.05f;
            if (selectionGlow >= Math.PI) glowIncreasing = false;
        } else {
            selectionGlow -= 0.05f;
            if (selectionGlow <= 0) glowIncreasing = true;
        }
    }

    private void addKeyBindings(JPanel panel) {
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "singlePlayer");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "twoPlayer");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToMenu");

        actionMap.put("singlePlayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSinglePlayer();
            }
        });

        actionMap.put("twoPlayer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTwoPlayer();
            }
        });

        actionMap.put("returnToMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnToMenu();
            }
        });

        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    private void startSinglePlayer() {
        cleanup();
        TetrisGame game = new TetrisGame();
        game.setVisible(true);
        game.startGame();
        dispose();
    }

    private void startTwoPlayer() {
        cleanup();
        TwoPlayerTetrisGame game = new TwoPlayerTetrisGame();
        game.setVisible(true);
        game.startGame();
        dispose();
    }

    private void returnToMenu() {
        cleanup();
        StartScreen startScreen = new StartScreen();
        startScreen.setVisible(true);
        dispose();
    }

    private void cleanup() {
        if (effectsTimer != null) {
            effectsTimer.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModeScreen screen = new GameModeScreen();
            screen.setVisible(true);
        });
    }
} 