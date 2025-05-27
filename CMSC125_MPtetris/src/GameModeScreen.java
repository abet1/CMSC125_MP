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
    private boolean isTransitioning = false;
    private SoundManager soundManager;

    public GameModeScreen() {
        setTitle("Tetris Effect - Select Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize sound manager
        soundManager = new SoundManager(false);
        soundManager.playMenuMusic();

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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(70, 20, 40, 20));

        // Create mode selection panel
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setOpaque(false);
        modePanel.setBorder(BorderFactory.createEmptyBorder(160, 0, 0, 0));

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
        int y = 130;

        float t = (float)((Math.sin(selectionGlow) + 1) / 2.0);
        Color animatedGlow = blend(UITheme.ACCENT_SECONDARY, UITheme.ACCENT_PRIMARY, t);

        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.drawString(title, x + 5, y + 7);

        float alpha = 0.22f + (float)(Math.sin(selectionGlow) + 1) * 0.10f;
        for (int i = 16; i > 0; i--) {
            Color glowColor = new Color(
                animatedGlow.getRed(),
                animatedGlow.getGreen(),
                animatedGlow.getBlue(),
                (int)(alpha * 255 / i)
            );
            g2d.setColor(glowColor);
            g2d.drawString(title, x + i/2, y + i/2);
            g2d.drawString(title, x - i/2, y - i/2);
        }

        GradientPaint gradient = new GradientPaint(
            x, y - fm.getAscent(),
            UITheme.ACCENT_SECONDARY,
            x + titleWidth, y,
            UITheme.ACCENT_PRIMARY
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, x, y);

        String subtitle = "Choose Your Cosmic Path";
        g2d.setFont(UITheme.SUBTITLE_FONT);
        fm = g2d.getFontMetrics();
        float spacing = 2.5f;
        int subtitleWidth = measureStringWithSpacing(g2d, subtitle, spacing);
        int subtitleX = (WINDOW_WIDTH - subtitleWidth) / 2;
        int subtitleY = y + 70;

        int pillPadX = 38, pillPadY = 18;
        int pillWidth = subtitleWidth + pillPadX;
        int pillHeight = fm.getHeight() + pillPadY;
        int pillX = subtitleX - pillPadX/2;
        int pillY = subtitleY - fm.getAscent() - pillPadY/2;
        g2d.setColor(new Color(120, 80, 180, 70));
        g2d.fillRoundRect(pillX-8, pillY-6, pillWidth+16, pillHeight+12, pillHeight+12, pillHeight+12);

        g2d.setColor(new Color(30, 30, 40, 180));
        g2d.fillRoundRect(pillX, pillY, pillWidth, pillHeight, pillHeight, pillHeight);

        g2d.setColor(UITheme.TEXT_SECONDARY);
        drawStringWithSpacing(g2d, subtitle, subtitleX, subtitleY, spacing);
    }

    private Color blend(Color c1, Color c2, float ratio) {
        int r = (int)(c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    private int drawStringWithSpacing(Graphics2D g2d, String text, int x, int y, float spacing) {
        int startX = x;
        for (char c : text.toCharArray()) {
            String s = String.valueOf(c);
            g2d.drawString(s, x, y);
            x += g2d.getFontMetrics().charWidth(c) + spacing;
        }
        return x - startX;
    }

    private int measureStringWithSpacing(Graphics2D g2d, String text, float spacing) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += g2d.getFontMetrics().charWidth(c) + spacing;
        }
        return width;
    }

    private void updateSelectionGlow() {
        if (glowIncreasing) {
            selectionGlow += 0.03f;
            if (selectionGlow >= Math.PI) glowIncreasing = false;
        } else {
            selectionGlow -= 0.03f;
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
        if (isTransitioning) return;
        isTransitioning = true;
        cleanup();
        dispose();
        SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
            game.startGame();
        });
    }

    private void startTwoPlayer() {
        if (isTransitioning) return;
        isTransitioning = true;
        cleanup();
        dispose();
        SwingUtilities.invokeLater(() -> {
            TwoPlayerTetrisGame game = new TwoPlayerTetrisGame();
            game.setVisible(true);
            game.startGame();
        });
    }

    private void returnToMenu() {
        if (isTransitioning) return;
        isTransitioning = true;
        cleanup();
        dispose();
        SwingUtilities.invokeLater(() -> {
            new StartScreen().setVisible(true);
        });
    }

    private void cleanup() {
        if (effectsTimer != null) {
            effectsTimer.stop();
            effectsTimer = null;
        }
        if (cosmicEffects != null) {
            cosmicEffects.cleanup();
            cosmicEffects = null;
        }
        if (soundManager != null) {
            soundManager.stopMenuMusic();
            soundManager.cleanup();
        }
        System.gc();
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModeScreen screen = new GameModeScreen();
            screen.setVisible(true);
        });
    }
} 