import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Two-player Tetris game implementation with separate game instances
 * and controls for each player
 */
public class TwoPlayerTetrisGame extends JFrame implements TetrisGameInterface {
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 600;

    private GameBoard player1Board;
    private GameBoard player2Board;
    private GameThread player1Thread;
    private GameThread player2Thread;
    private PreviewPanel player1NextPanel;
    private PreviewPanel player2NextPanel;
    private PreviewPanel player1HoldPanel;
    private PreviewPanel player2HoldPanel;
    private JLabel player1ScoreLabel;
    private JLabel player2ScoreLabel;
    private JLabel player1LevelLabel;
    private JLabel player2LevelLabel;
    private JLabel player1LinesLabel;
    private JLabel player2LinesLabel;

    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isGameOver = new AtomicBoolean(false);
    private final ReentrantLock gameLock = new ReentrantLock();
    private SoundManager soundManager;

    public TwoPlayerTetrisGame() {
        setTitle("Two Player Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        soundManager = new SoundManager(true);
        setupGameComponents();
        setupKeyBindings();
        pack();
        setLocationRelativeTo(null);
    }

    private void setupGameComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(44, 62, 80));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel player1Panel = createPlayerPanel("Player 1");
        player1Board = new GameBoard(this, 10, 20, 30);
        player1Panel.add(player1Board, BorderLayout.CENTER);

        JPanel player2Panel = createPlayerPanel("Player 2");
        player2Board = new GameBoard(this, 10, 20, 30);
        player2Panel.add(player2Board, BorderLayout.CENTER);

        mainPanel.add(player1Panel);
        mainPanel.add(player2Panel);
        add(mainPanel);
    }

    private JPanel createPlayerPanel(String playerName) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(44, 62, 80));

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(44, 62, 80));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        sidePanel.setPreferredSize(new Dimension(140, 0));

        JLabel nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(nameLabel);
        sidePanel.add(Box.createVerticalStrut(10));

        JLabel nextPieceLabel = new JLabel("Next Piece:");
        nextPieceLabel.setForeground(Color.WHITE);
        nextPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        PreviewPanel nextPanel = new PreviewPanel(4, 30);
        nextPanel.setPreferredSize(new Dimension(120, 120));
        nextPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        nextPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel holdPieceLabel = new JLabel("Hold Piece:");
        holdPieceLabel.setForeground(Color.WHITE);
        holdPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        PreviewPanel holdPanel = new PreviewPanel(4, 30);
        holdPanel.setPreferredSize(new Dimension(120, 120));
        holdPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        holdPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel linesLabel = new JLabel("Lines: 0");
        linesLabel.setForeground(Color.WHITE);
        linesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidePanel.add(nextPieceLabel);
        sidePanel.add(nextPanel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(holdPieceLabel);
        sidePanel.add(holdPanel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(scoreLabel);
        sidePanel.add(levelLabel);
        sidePanel.add(linesLabel);

        JLabel controlsLabel = new JLabel("Controls:");
        controlsLabel.setForeground(Color.WHITE);
        controlsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(controlsLabel);
        sidePanel.add(Box.createVerticalStrut(10));

        String[] controls = playerName.equals("Player 1") ? new String[] {
            "A/D : Move",
            "W : Rotate",
            "S : Soft Drop",
            "Space : Hard Drop",
            "C : Hold",
            "P : Pause"
        } : new String[] {
            "← → : Move",
            "↑ : Rotate",
            "↓ : Soft Drop",
            "Ctrl : Hard Drop",
            "Shift : Hold",
            "P : Pause"
        };

        for (String control : controls) {
            JLabel label = new JLabel(control);
            label.setForeground(Color.WHITE);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(label);
            sidePanel.add(Box.createVerticalStrut(5));
        }

        if (playerName.equals("Player 1")) {
            player1NextPanel = nextPanel;
            player1HoldPanel = holdPanel;
            player1ScoreLabel = scoreLabel;
            player1LevelLabel = levelLabel;
            player1LinesLabel = linesLabel;
        } else {
            player2NextPanel = nextPanel;
            player2HoldPanel = holdPanel;
            player2ScoreLabel = scoreLabel;
            player2LevelLabel = levelLabel;
            player2LinesLabel = linesLabel;
        }

        panel.add(sidePanel, BorderLayout.EAST);
        return panel;
    }

    private void setupKeyBindings() {
        player1Board.setFocusable(true);
        InputMap player1InputMap = player1Board.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap player1ActionMap = player1Board.getActionMap();

        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "moveLeft");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "moveRight");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "moveDown");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "rotate");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "hardDrop");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "holdPiece");

        player2Board.setFocusable(true);
        InputMap player2InputMap = player2Board.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap player2ActionMap = player2Board.getActionMap();

        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "rotate");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK), "hardDrop");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "holdPiece");

        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "returnToMenu");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "returnToMenu");
        player1InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        player2InputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");

        setupPlayerActions(player1ActionMap, player1Board, true);
        setupPlayerActions(player2ActionMap, player2Board, false);
    }

    private void setupPlayerActions(ActionMap actionMap, GameBoard board, boolean isPlayer1) {
        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused.get() && !isGameOver.get()) {
                    gameLock.lock();
                    try {
                        board.moveCurrentPieceLeft();
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
                        board.moveCurrentPieceRight();
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
                        if (!board.moveCurrentPieceDown()) {
                            board.placePiece();
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
                        board.rotateCurrentPiece();
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
                        board.hardDrop();
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
                        board.holdCurrentPiece();
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

        actionMap.put("returnToMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused.get() || isGameOver.get()) {
                    returnToMenu();
                }
            }
        });

        actionMap.put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isGameOver.get()) {
                    restartGame();
                }
            }
        });
    }

    private void togglePause() {
        isPaused.set(!isPaused.get());
        if (isPaused.get()) {
            player1Board.showPauseMessage();
            player2Board.showPauseMessage();
            soundManager.stopBackgroundMusic();
        } else {
            player1Board.clearMessage();
            player2Board.clearMessage();
            soundManager.playBackgroundMusic();
        }
    }

    public void startGame() {
        player1Board.initializeGame();
        player2Board.initializeGame();
        
        player1Thread = new GameThread(player1Board, "Player1");
        player2Thread = new GameThread(player2Board, "Player2");
        player1Thread.start();
        player2Thread.start();
        
        soundManager.playBackgroundMusic();
    }

    private void restartGame() {
        if (player1Thread != null && player1Thread.isAlive()) {
            player1Thread.interrupt();
        }
        if (player2Thread != null && player2Thread.isAlive()) {
            player2Thread.interrupt();
        }

        isGameOver.set(false);
        isPaused.set(false);
        
        player1ScoreLabel.setText("Score: 0");
        player2ScoreLabel.setText("Score: 0");
        player1LevelLabel.setText("Level: 1");
        player2LevelLabel.setText("Level: 1");
        player1LinesLabel.setText("Lines: 0");
        player2LinesLabel.setText("Lines: 0");
        
        player1Board.cleanup();
        player2Board.cleanup();
        player1Board.resetBoard();
        player2Board.resetBoard();
        
        player1Board.clearMessage();
        player2Board.clearMessage();
        
        player1Thread = new GameThread(player1Board, "Player1");
        player2Thread = new GameThread(player2Board, "Player2");
        
        player1Board.initializeGame();
        player2Board.initializeGame();
        player1Thread.start();
        player2Thread.start();
        
        soundManager.stopBackgroundMusic();
        soundManager.playBackgroundMusic();
    }

    private void returnToMenu() {
        if (player1Thread != null) {
            player1Thread.interrupt();
        }
        if (player2Thread != null) {
            player2Thread.interrupt();
        }
        soundManager.cleanup();
        player1Board.cleanup();
        player2Board.cleanup();
        new GameModeScreen().setVisible(true);
        dispose();
    }

    private class GameThread extends Thread {
        private final GameBoard board;
        private static final long FRAME_DELAY = 1000;
        private final String playerName;

        public GameThread(GameBoard board, String playerName) {
            super("GameThread-" + playerName);
            this.board = board;
            this.playerName = playerName;
        }

        @Override
        public void run() {
            while (!isInterrupted() && !isGameOver.get()) {
                try {
                    if (!isPaused.get()) {
                        gameLock.lock();
                        try {
                            if (!board.moveCurrentPieceDown()) {
                                board.placePiece();
                            }
                        } finally {
                            gameLock.unlock();
                        }
                    }
                    Thread.sleep(FRAME_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    public void updateNextPiecePanel(Tetromino piece) {
        if (piece == player1Board.getNextPiece()) {
            player1NextPanel.updatePreview(piece);
        } else {
            player2NextPanel.updatePreview(piece);
        }
    }

    @Override
    public void updateHoldPiecePanel(Tetromino piece) {
        if (piece == player1Board.getHoldPiece()) {
            player1HoldPanel.updatePreview(piece);
        } else {
            player2HoldPanel.updatePreview(piece);
        }
    }

    @Override
    public void gameOver() {
        if (isGameOver.get()) {
            return;
        }
        
        isGameOver.set(true);
        soundManager.stopBackgroundMusic();
        soundManager.playGameOverSound();
        
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        
        if (player1Thread != null) {
            player1Thread.interrupt();
        }
        if (player2Thread != null) {
            player2Thread.interrupt();
        }
        
        if (threadName.contains("Player1")) {
            player1Board.showGameOverMessage("YOU LOSE\nR = Restart\nM = Menu");
            player2Board.showGameOverMessage("YOU WIN!\nR = Restart\nM = Menu");
        } else if (threadName.contains("Player2")) {
            player1Board.showGameOverMessage("YOU WIN!\nR = Restart\nM = Menu");
            player2Board.showGameOverMessage("YOU LOSE\nR = Restart\nM = Menu");
        } else {
            player1Board.showGameOverMessage("GAME OVER\nR = Restart\nM = Menu");
            player2Board.showGameOverMessage("GAME OVER\nR = Restart\nM = Menu");
        }
    }

    @Override
    public boolean isPaused() {
        return isPaused.get();
    }

    @Override
    public boolean isGameOver() {
        return isGameOver.get();
    }

    @Override
    public ReentrantLock getGameLock() {
        return gameLock;
    }

    @Override
    public void updateScore(int linesCleared) {
        if (linesCleared > 0) {
            soundManager.playLineClearSound();
            
            if (linesCleared == player1Board.getLastLinesCleared()) {
                player1ScoreLabel.setText("Score: " + (Integer.parseInt(player1ScoreLabel.getText().split(": ")[1]) + linesCleared * 100));
            } else {
                player2ScoreLabel.setText("Score: " + (Integer.parseInt(player2ScoreLabel.getText().split(": ")[1]) + linesCleared * 100));
            }
        }
    }

    public void playPieceDropSound() {
        soundManager.playPieceDropSound();
    }
} 