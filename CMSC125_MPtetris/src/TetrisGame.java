import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The main Tetris game class that handles the game window, UI components,
 * and game state. This class demonstrates multi-threading by separating game
 * logic and rendering into different threads.
 */
public class TetrisGame extends JFrame implements TetrisGameInterface {
    // Game constants
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int PREVIEW_SIZE = 4;

    // Game components
    private GameBoard gameBoard;
    private GameThread gameThread;
    private PreviewPanel nextPiecePanel;
    private PreviewPanel holdPiecePanel;
    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel linesLabel;

    // Game state
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;

    // Thread-safe variables for concurrency control
    private final ReentrantLock gameLock = new ReentrantLock();
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isGameOver = new AtomicBoolean(false);
    private SoundManager soundManager;

    /**
     * Constructor sets up the game window, components, and initializes the UI.
     */
    public TetrisGame() {
        setTitle("Multi-threaded Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Initialize sound manager for single player
        soundManager = new SoundManager(false);

        // Set up the main game panels
        setupGameComponents();

        // Set up keyboard controls
        setupKeyBindings();

        pack();
        setLocationRelativeTo(null);

        // Initialize the game thread
        gameThread = new GameThread();
    }

    /**
     * Sets up the game board, preview panels, and score display
     */
    private void setupGameComponents() {
        // Main game panel
        gameBoard = new GameBoard(this, BOARD_WIDTH, BOARD_HEIGHT, BLOCK_SIZE);
        gameBoard.setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));

        // Create styled side panel
        JPanel sidePanel = UITheme.createStyledPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidePanel.setPreferredSize(new Dimension(200, BOARD_HEIGHT * BLOCK_SIZE));

        // Next piece section
        JLabel nextPieceLabel = UITheme.createStyledLabel("NEXT PIECE", UITheme.SUBTITLE_FONT);
        nextPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextPiecePanel = new PreviewPanel(PREVIEW_SIZE, BLOCK_SIZE);
        nextPiecePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hold piece section
        JLabel holdPieceLabel = UITheme.createStyledLabel("HOLD PIECE", UITheme.SUBTITLE_FONT);
        holdPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        holdPiecePanel = new PreviewPanel(PREVIEW_SIZE, BLOCK_SIZE);
        holdPiecePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats panel with gradient background
        JPanel statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 100),
                    getWidth(), getHeight(), new Color(0, 0, 0, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(180, 100));

        // Create styled stat labels
        scoreLabel = UITheme.createStyledLabel("SCORE: 0", UITheme.TEXT_FONT);
        levelLabel = UITheme.createStyledLabel("LEVEL: 1", UITheme.TEXT_FONT);
        linesLabel = UITheme.createStyledLabel("LINES: 0", UITheme.TEXT_FONT);

        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(scoreLabel);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(levelLabel);
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(linesLabel);

        // Controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setOpaque(false);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel controlsTitle = UITheme.createStyledLabel("CONTROLS", UITheme.SUBTITLE_FONT);
        controlsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(controlsTitle);
        controlsPanel.add(Box.createVerticalStrut(10));

        String[] controls = {
            "← → : Move",
            "↑ : Rotate",
            "↓ : Soft Drop",
            "Space : Hard Drop",
            "C : Hold Piece",
            "P : Pause"
        };

        for (String control : controls) {
            JLabel controlLabel = UITheme.createStyledLabel(control, UITheme.TEXT_FONT);
            controlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            controlsPanel.add(controlLabel);
            controlsPanel.add(Box.createVerticalStrut(5));
        }

        // Add components to side panel with spacing
        sidePanel.add(nextPieceLabel);
        sidePanel.add(Box.createVerticalStrut(5));
        sidePanel.add(nextPiecePanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(holdPieceLabel);
        sidePanel.add(Box.createVerticalStrut(5));
        sidePanel.add(holdPiecePanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(statsPanel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(controlsPanel);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(UITheme.BACKGROUND_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(gameBoard, BorderLayout.CENTER);
        mainPanel.add(sidePanel, BorderLayout.EAST);

        add(mainPanel);
    }

    /**
     * Sets up keyboard controls for the game using key bindings
     */
    private void setupKeyBindings() {
        gameBoard.setFocusable(true);
        InputMap inputMap = gameBoard.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = gameBoard.getActionMap();

        // Movement controls
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "rotate");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "hardDrop");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "holdPiece");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");

        // Add menu return key binding
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "returnToMenu");
        actionMap.put("returnToMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused.get() || isGameOver.get()) {
                    returnToMenu();
                }
            }
        });

        // Add restart key binding
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        actionMap.put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isGameOver.get()) {
                    restartGame();
                }
            }
        });

        // Define actions for key bindings
        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        gameBoard.moveCurrentPieceLeft();
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        gameBoard.moveCurrentPieceRight();
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        if (!gameBoard.moveCurrentPieceDown()) {
                            gameBoard.placePiece();
                        }
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        gameBoard.rotateCurrentPiece();
                        soundManager.playRotateSound();
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("hardDrop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        gameBoard.hardDrop();
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("holdPiece", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        gameBoard.holdCurrentPiece();
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
        });

        actionMap.put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }

    /**
     * Toggles the pause state of the game
     */
    private void togglePause() {
        isPaused.set(!isPaused.get());
        if (isPaused.get()) {
            gameBoard.showPauseMessage();
            soundManager.stopBackgroundMusic();
        } else {
            soundManager.playBackgroundMusic();
        }
    }

    /**
     * Updates the score based on the number of lines cleared
     * @param clearedLines Number of lines cleared in one move
     */
    public void updateScore(int clearedLines) {
        if (clearedLines > 0) {
            // Score calculation based on level and lines cleared
            int points = 0;
            switch (clearedLines) {
                case 1: points = 100 * level; break;
                case 2: points = 300 * level; break;
                case 3: points = 500 * level; break;
                case 4: points = 800 * level; break;
            }

            score += points;
            linesCleared += clearedLines;

            // Level up every 10 lines
            int newLevel = (linesCleared / 10) + 1;
            if (newLevel > level) {
                level = newLevel;
                soundManager.playLevelUpSound();
                soundManager.updateLevel(level);
            }

            // Update UI
            updateScoreLabels();
            
            // Play line clear sound
            soundManager.playLineClearSound();
        }
    }

    /**
     * Updates the score display in the UI
     */
    private void updateScoreLabels() {
        scoreLabel.setText("SCORE: " + score);
        levelLabel.setText("LEVEL: " + level);
        linesLabel.setText("LINES: " + linesCleared);

        // Update label colors based on level
        Color levelColor = Color.getHSBColor((level * 0.1f) % 1.0f, 0.8f, 1.0f);
        levelLabel.setForeground(levelColor);
    }

    /**
     * Updates the next piece preview panel
     * @param piece The next tetromino to display
     */
    public void updateNextPiecePanel(Tetromino piece) {
        nextPiecePanel.updatePreview(piece);
    }

    /**
     * Updates the hold piece preview panel
     * @param piece The held tetromino to display
     */
    public void updateHoldPiecePanel(Tetromino piece) {
        holdPiecePanel.updatePreview(piece);
    }

    /**
     * Starts the game thread
     */
    public void startGame() {
        gameThread.start();
        soundManager.playBackgroundMusic();
    }

    /**
     * Ends the game and shows game over message
     */
    public void gameOver() {
        isGameOver.set(true);
        gameBoard.showGameOverMessage();
        soundManager.playGameOverSound();
        soundManager.stopBackgroundMusic();
    }

    /**
     * Gets the current game level which affects speed
     * @return The current level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Checks if the game is paused
     * @return True if the game is paused
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /**
     * Checks if the game is over
     * @return True if the game is over
     */
    public boolean isGameOver() {
        return isGameOver.get();
    }

    /**
     * Gets the lock used for thread synchronization
     * @return The ReentrantLock instance
     */
    public ReentrantLock getGameLock() {
        return gameLock;
    }

    /**
     * Restarts the game
     */
    private void restartGame() {
        // Reset game state
        score = 0;
        level = 1;
        linesCleared = 0;
        isGameOver.set(false);
        isPaused.set(false);
        
        // Update score display
        updateScoreLabels();
        
        // Reset the game board
        gameBoard.cleanup();
        gameBoard.resetBoard();
        gameBoard.initializeGame();
        
        // Clear any messages
        gameBoard.clearMessage();
        
        // Restart the game thread
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
        gameThread = new GameThread();
        gameThread.start();
        
        // Restart background music
        soundManager.stopBackgroundMusic();
        soundManager.playBackgroundMusic();
    }

    /**
     * Returns to the start menu
     */
    private void returnToMenu() {
        if (gameThread != null) {
            gameThread.interrupt();
        }
        soundManager.cleanup();
        gameBoard.cleanup();
        new StartScreen().setVisible(true);
        dispose();
    }

    /**
     * Game thread that handles the main game loop running on a separate thread
     */
    private class GameThread extends Thread {
        public GameThread() {
            super("Tetris-GameThread");
        }

        @Override
        public void run() {
            gameBoard.initializeGame();

            while (!isGameOver.get()) {
                if (!isPaused.get()) {
                    gameLock.lock();
                    try {
                        // Move piece down
                        if (!gameBoard.moveCurrentPieceDown()) {
                            gameBoard.placePiece();
                        }
                    } finally {
                        gameLock.unlock();
                    }
                }

                // Sleep based on current level (game speeds up with higher levels)
                try {
                    int delay = Math.max(100, 1000 - (level - 1) * 50);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void playPieceDropSound() {
        soundManager.playPieceDropSound();
    }
}