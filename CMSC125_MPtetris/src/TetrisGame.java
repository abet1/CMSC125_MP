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

        // Side panel for score, next piece, etc.
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(44, 62, 80));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Next piece preview
        JLabel nextPieceLabel = new JLabel("Next Piece:");
        nextPieceLabel.setForeground(Color.WHITE);
        nextPiecePanel = new PreviewPanel(PREVIEW_SIZE, BLOCK_SIZE);
        nextPiecePanel.setPreferredSize(new Dimension(PREVIEW_SIZE * BLOCK_SIZE, PREVIEW_SIZE * BLOCK_SIZE));
        nextPiecePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Hold piece panel
        JLabel holdPieceLabel = new JLabel("Hold Piece:");
        holdPieceLabel.setForeground(Color.WHITE);
        holdPiecePanel = new PreviewPanel(PREVIEW_SIZE, BLOCK_SIZE);
        holdPiecePanel.setPreferredSize(new Dimension(PREVIEW_SIZE * BLOCK_SIZE, PREVIEW_SIZE * BLOCK_SIZE));
        holdPiecePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Score display
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.WHITE);
        linesLabel = new JLabel("Lines: 0");
        linesLabel.setForeground(Color.WHITE);

        // Add components to side panel
        sidePanel.add(nextPieceLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(nextPiecePanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(holdPieceLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(holdPiecePanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(scoreLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(levelLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(linesLabel);

        // Add controls info
        addControlsInfo(sidePanel);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.add(gameBoard, BorderLayout.CENTER);
        mainPanel.add(sidePanel, BorderLayout.EAST);

        add(mainPanel);
    }

    /**
     * Adds control instructions to the side panel
     */
    private void addControlsInfo(JPanel sidePanel) {
        String[] controls = {
                "Controls:",
                "← → : Move",
                "↑ : Rotate",
                "↓ : Soft Drop",
                "Space : Hard Drop",
                "C : Hold Piece",
                "P : Pause"
        };

        for (String control : controls) {
            JLabel label = new JLabel(control);
            label.setForeground(Color.WHITE);
            sidePanel.add(label);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
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
            level = (linesCleared / 10) + 1;

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
        scoreLabel.setText("Score: " + score);
        levelLabel.setText("Level: " + level);
        linesLabel.setText("Lines: " + linesCleared);
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
        soundManager.cleanup();
        dispose(); // Close the game window
        StartScreen startScreen = new StartScreen();
        startScreen.setVisible(true);
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