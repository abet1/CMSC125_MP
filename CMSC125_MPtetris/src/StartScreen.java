import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;

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
    private float fadeInAlpha = 0f;
    private float promptAnim = 0f;
    private boolean promptIncreasing = true;
    private SoundManager soundManager;

    /**
     * Constructor sets up the start screen window and its components
     */
    public StartScreen() {
        setTitle("Tetris Effect");
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

                Composite oldComp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeInAlpha));

                cosmicEffects.draw(g2d);

                drawTitle(g2d);

                g2d.setComposite(oldComp);
                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(70, 20, 40, 20));

        // Create content panel for buttons
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(160, 0, 0, 0));

        // Create buttons
        JButton startButton = UITheme.createStyledButton("Start Game");
        JButton helpButton = UITheme.createStyledButton("How to Play");
        JButton highScoreButton = UITheme.createStyledButton("High Scores");
        JButton exitButton = UITheme.createStyledButton("Exit");

        // Center align buttons
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add button actions
        startButton.addActionListener(e -> startGame());
        helpButton.addActionListener(e -> showHelp());
        highScoreButton.addActionListener(e -> showHighScores());
        exitButton.addActionListener(e -> System.exit(0));

        // Add vertical spacing between buttons
        contentPanel.add(startButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(helpButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(highScoreButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(exitButton);

        mainPanel.add(contentPanel);

        // Add the main panel to the frame
        add(mainPanel);

        // Start animation timer
        effectsTimer = new Timer(16, e -> {
            cosmicEffects.update();
            updateTitleGlow();
            if (fadeInAlpha < 1f) {
                fadeInAlpha += 0.12f;
                if (fadeInAlpha > 1f) fadeInAlpha = 1f;
            }
            // Animate prompt fade in/out
            if (promptIncreasing) {
                promptAnim += 0.04f;
                if (promptAnim > 1f) { promptAnim = 1f; promptIncreasing = false; }
            } else {
                promptAnim -= 0.04f;
                if (promptAnim < 0.2f) { promptAnim = 0.2f; promptIncreasing = true; }
            }
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
        int y = 130;

        float t = (float)((Math.sin(titleGlow) + 1) / 2.0);
        Color animatedGlow = blend(UITheme.ACCENT_PRIMARY, UITheme.ACCENT_SECONDARY, t);

        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.drawString(title, x + 5, y + 7);

        float alpha = 0.22f + (float)(Math.sin(titleGlow) + 1) * 0.10f;
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
            UITheme.ACCENT_PRIMARY,
            x + titleWidth, y,
            UITheme.ACCENT_SECONDARY
        );
        g2d.setPaint(gradient);
        g2d.drawString(title, x, y);

        String subtitle = "A Cosmic Journey";
        g2d.setFont(UITheme.SUBTITLE_FONT);
        fm = g2d.getFontMetrics();
        float spacing = 2.5f;
        int subtitleWidth = measureStringWithSpacing(g2d, subtitle, spacing);
        int pillPadX = 38, pillPadY = 18;
        int pillWidth = subtitleWidth + pillPadX;
        int pillHeight = fm.getHeight() + pillPadY;
        int subtitleY = y + 70;
        int pillX = (WINDOW_WIDTH - pillWidth) / 2;
        int pillY = subtitleY - fm.getAscent() - pillPadY/2;
        int textX = pillX + pillPadX/2;

        g2d.setColor(new Color(120, 80, 180, 70));
        g2d.fillRoundRect(pillX-8, pillY-6, pillWidth+16, pillHeight+12, pillHeight+12, pillHeight+12);

        g2d.setColor(new Color(30, 30, 40, 180));
        g2d.fillRoundRect(pillX, pillY, pillWidth, pillHeight, pillHeight, pillHeight);

        g2d.setColor(UITheme.TEXT_SECONDARY);
        drawStringWithSpacing(g2d, subtitle, textX, subtitleY, spacing);
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

    private void updateTitleGlow() {
        if (glowIncreasing) {
            titleGlow += 0.03f;
            if (titleGlow >= Math.PI) glowIncreasing = false;
        } else {
            titleGlow -= 0.03f;
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

    private void showHighScores() {
        JDialog highScoreDialog = new JDialog(this, "High Scores", true);
        highScoreDialog.setSize(400, 500);
        highScoreDialog.setLocationRelativeTo(this);

        JPanel highScorePanel = UITheme.createStyledPanel();
        highScorePanel.setLayout(new BoxLayout(highScorePanel, BoxLayout.Y_AXIS));
        highScorePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title
        JLabel titleLabel = UITheme.createStyledLabel("HIGH SCORES", UITheme.SUBTITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScorePanel.add(titleLabel);
        highScorePanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Get and display scores
        HighScoreManager highScoreManager = new HighScoreManager();
        List<HighScoreManager.ScoreEntry> scores = highScoreManager.getHighScores();

        if (scores.isEmpty()) {
            JLabel noScoresLabel = UITheme.createStyledLabel("No high scores yet!", UITheme.TEXT_FONT);
            noScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            highScorePanel.add(noScoresLabel);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                HighScoreManager.ScoreEntry entry = scores.get(i);
                String scoreText = String.format("%d. %s - %d pts", 
                    i + 1, entry.getUsername(), entry.getScore());
                String statsText = String.format("   Level %d | %d lines", 
                    entry.getLevel(), entry.getLines());
                
                JLabel scoreLabel = UITheme.createStyledLabel(scoreText, UITheme.TEXT_FONT);
                JLabel statsLabel = UITheme.createStyledLabel(statsText, UITheme.TEXT_FONT);
                
                scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                highScorePanel.add(scoreLabel);
                highScorePanel.add(statsLabel);
                highScorePanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        JButton closeButton = UITheme.createStyledButton("Close");
        closeButton.addActionListener(e -> highScoreDialog.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScorePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        highScorePanel.add(closeButton);

        highScoreDialog.add(highScorePanel);
        highScoreDialog.setVisible(true);
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

    /**
     * Main method to launch the start screen
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartScreen startScreen = new StartScreen();
            startScreen.setVisible(true);
        });
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
} 