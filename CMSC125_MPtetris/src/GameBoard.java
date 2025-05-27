import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

/**
 * The GameBoard class represents the main Tetris playing field.
 * It manages the current piece, the next piece, the held piece, and the game grid.
 */
public class GameBoard extends JPanel {
    // Constants
    private final int BOARD_WIDTH;
    private final int BOARD_HEIGHT;
    private final int BLOCK_SIZE;

    // Game state
    private int[][] board;
    private Tetromino currentPiece;
    private Tetromino nextPiece;
    private Tetromino holdPiece;
    private boolean canHold = true;
    private int currentX;
    private int currentY;
    private final Random random = new Random();
    private final TetrisGameInterface gameInstance;

    // Messages
    private String message = null;

    private int lastLinesCleared = 0;

    private CosmicEffects cosmicEffects;
    private Timer effectsTimer;

    /**
     * Constructor for the game board
     * @param game Reference to the main game instance
     * @param width Width of the board in blocks
     * @param height Height of the board in blocks
     * @param blockSize Size of each block in pixels
     */
    public GameBoard(TetrisGameInterface game, int width, int height, int blockSize) {
        this.gameInstance = game;
        this.BOARD_WIDTH = width;
        this.BOARD_HEIGHT = height;
        this.BLOCK_SIZE = blockSize;

        // Initialize the game board
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        setBackground(Color.BLACK);

        // Initialize cosmic effects
        cosmicEffects = new CosmicEffects(width * blockSize, height * blockSize);
        
        // Start effects timer
        effectsTimer = new Timer(16, e -> {
            cosmicEffects.update();
            repaint();
        });
        effectsTimer.start();
    }

    /**
     * Initializes the game by creating the first pieces
     */
    public void initializeGame() {
        // Clear the board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = 0;
            }
        }

        // Generate first pieces
        nextPiece = Tetromino.getRandomPiece(random);
        createNewPiece();
    }

    /**
     * Creates a new piece and places it at the top of the board
     */
    private void createNewPiece() {
        currentPiece = nextPiece;
        nextPiece = Tetromino.getRandomPiece(random);

        // Update the next piece preview
        gameInstance.updateNextPiecePanel(nextPiece);

        // Place the new piece at the top center of the board
        currentX = BOARD_WIDTH / 2 - currentPiece.getWidth() / 2;
        currentY = 0;

        // Check if the new piece can be placed
        if (!isValidPosition(currentX, currentY, currentPiece)) {
            gameInstance.gameOver();
        }

        // Piece can be placed, force repaint
        repaint();
    }

    /**
     * Holds the current piece and swaps it with the held piece
     */
    public void holdCurrentPiece() {
        if (!canHold) {
            return;
        }

        // First time holding a piece
        if (holdPiece == null) {
            holdPiece = currentPiece;
            gameInstance.updateHoldPiecePanel(holdPiece);
            createNewPiece();
        } else {
            // Swap current and held pieces
            Tetromino temp = currentPiece;
            currentPiece = holdPiece;
            holdPiece = temp;

            // Update hold preview
            gameInstance.updateHoldPiecePanel(holdPiece);

            // Reset position
            currentX = BOARD_WIDTH / 2 - currentPiece.getWidth() / 2;
            currentY = 0;
        }

        // Can't hold again until a piece is placed
        canHold = false;

        // Force repaint
        repaint();
    }

    /**
     * Moves the current piece to the left if possible
     * @return true if piece was moved, false otherwise
     */
    public boolean moveCurrentPieceLeft() {
        if (isValidPosition(currentX - 1, currentY, currentPiece)) {
            currentX--;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Moves the current piece to the right if possible
     * @return true if piece was moved, false otherwise
     */
    public boolean moveCurrentPieceRight() {
        if (isValidPosition(currentX + 1, currentY, currentPiece)) {
            currentX++;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Moves the current piece down if possible
     * @return true if piece was moved, false otherwise
     */
    public boolean moveCurrentPieceDown() {
        if (isValidPosition(currentX, currentY + 1, currentPiece)) {
            currentY++;
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Rotates the current piece if possible
     * @return true if piece was rotated, false otherwise
     */
    public boolean rotateCurrentPiece() {
        if (currentPiece == null || gameInstance.isGameOver()) return false;

        Tetromino rotated = currentPiece.getRotated();
        if (isValidPosition(currentX, currentY, rotated)) {
            currentPiece = rotated;
            
            // Trigger rotation effect
            int centerX = (currentX + currentPiece.getShape()[0].length/2) * BLOCK_SIZE;
            int centerY = (currentY + currentPiece.getShape().length/2) * BLOCK_SIZE;
            cosmicEffects.addRotationEffect(centerX, centerY);

            repaint();
            return true;
        }

        // Wall kick - try to adjust position to make rotation possible
        // Try moving left
        if (isValidPosition(currentX - 1, currentY, rotated)) {
            currentX--;
            currentPiece = rotated;
            repaint();
            return true;
        }

        // Try moving right
        if (isValidPosition(currentX + 1, currentY, rotated)) {
            currentX++;
            currentPiece = rotated;
            repaint();
            return true;
        }

        // Try moving up
        if (isValidPosition(currentX, currentY - 1, rotated)) {
            currentY--;
            currentPiece = rotated;
            repaint();
            return true;
        }

        return false;
    }

    /**
     * Drops the current piece to the bottom immediately
     */
    public void hardDrop() {
        int dropDistance = 0;

        // Find how far the piece can drop
        while (isValidPosition(currentX, currentY + dropDistance + 1, currentPiece)) {
            dropDistance++;
        }

        // Move piece down
        currentY += dropDistance;

        // Place the piece
        placePiece();

        // Force repaint
        repaint();
    }

    /**
     * Places the current piece on the board and creates a new piece
     */
    public void placePiece() {
        if (currentPiece == null) return;

        int[][] shape = currentPiece.getShape();
        int color = currentPiece.getColor();

        // Place the piece on the board
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    board[currentY + i][currentX + j] = color;
                }
            }
        }

        // Trigger drop effect at the piece's position
        int centerX = (currentX + shape[0].length/2) * BLOCK_SIZE;
        int centerY = (currentY + shape.length/2) * BLOCK_SIZE;
        cosmicEffects.addPieceDropEffect(centerX, centerY);

        // Check for completed lines
        int linesCleared = clearLines();
        if (linesCleared > 0) {
            // Trigger line clear effects
            for (int i = currentY; i < currentY + shape.length; i++) {
                cosmicEffects.addLineClearEffect(i * BLOCK_SIZE);
            }
            gameInstance.updateScore(linesCleared);
        }

        // Get next piece
        currentPiece = nextPiece;
        nextPiece = Tetromino.getRandomPiece(random);
        gameInstance.updateNextPiecePanel(nextPiece);

        // Reset position
        currentX = BOARD_WIDTH / 2 - currentPiece.getShape()[0].length / 2;
        currentY = 0;

        // Reset hold ability
        canHold = true;

        // Check if game is over
        if (!isValidPosition(currentX, currentY, currentPiece)) {
            gameInstance.gameOver();
        }

        // Play sound effect
        if (gameInstance instanceof TetrisGame) {
            ((TetrisGame) gameInstance).playPieceDropSound();
        } else if (gameInstance instanceof TwoPlayerTetrisGame) {
            ((TwoPlayerTetrisGame) gameInstance).playPieceDropSound();
        }
    }

    private int clearLines() {
        int linesCleared = 0;

        // Check each row from bottom to top
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean rowFull = true;

            // Check if row is full
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == 0) {
                    rowFull = false;
                    break;
                }
            }

            // If row is full, clear it and move lines above down
            if (rowFull) {
                linesCleared++;

                // Move all rows above down
                for (int k = i; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
                }

                // Clear top row
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[0][j] = 0;
                }

                // Since we removed a line, we need to check this line again
                i++;
            }
        }

        this.lastLinesCleared = linesCleared;
        return linesCleared;
    }

    /**
     * Checks if a piece can be placed at the specified position
     * @param x X position to check
     * @param y Y position to check
     * @param piece Piece to check
     * @return true if position is valid, false otherwise
     */
    private boolean isValidPosition(int x, int y, Tetromino piece) {
        int[][] shape = piece.getShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int newY = y + i;
                    int newX = x + j;

                    // Check if out of bounds
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                        return false;
                    }

                    // Check if already occupied (and not above the board)
                    if (newY >= 0 && board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Shows a pause message on the board
     */
    public void showPauseMessage() {
        message = "PAUSED\nM = Menu";
        repaint();
    }

    /**
     * Shows a game over message on the board
     */
    public void showGameOverMessage(String message) {
        this.message = message;
        repaint();
    }

    // Keep the original method for backward compatibility
    public void showGameOverMessage() {
        showGameOverMessage("GAME OVER\nR = Restart\nM = Menu");
    }

    /**
     * Custom painting of the game board
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw cosmic effects
        cosmicEffects.draw(g2d);

        // Draw semi-transparent game area
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);

        // Draw grid with glow effect
        drawGrid(g2d);

        // Draw placed blocks with glow effect
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != 0) {
                    drawGlowingBlock(g2d, j * BLOCK_SIZE, i * BLOCK_SIZE, board[i][j]);
                }
            }
        }

        // Draw current piece with glow effect
        if (currentPiece != null && !gameInstance.isGameOver()) {
            int[][] shape = currentPiece.getShape();
            int color = currentPiece.getColor();

            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int x = (currentX + j) * BLOCK_SIZE;
                        int y = (currentY + i) * BLOCK_SIZE;
                        drawGlowingBlock(g2d, x, y, color);
                    }
                }
            }
        }

        // Draw ghost piece with ethereal effect
        if (currentPiece != null && !gameInstance.isGameOver() && !gameInstance.isPaused()) {
            drawGhostPiece(g2d);
        }

        // Draw message if needed
        if (message != null) {
            drawMessage(g2d);
        }
    }

    private void drawGrid(Graphics2D g2d) {
        // Calculate dynamic grid opacity based on game state
        float baseOpacity = 0.12f;
        float pulseOpacity = (float)Math.sin(System.currentTimeMillis() / 1000.0) * 0.05f;
        int opacity = (int)((baseOpacity + pulseOpacity) * 255);

        g2d.setColor(new Color(255, 255, 255, opacity));
        
        // Draw horizontal lines
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            int y = i * BLOCK_SIZE;
            float lineOpacity = baseOpacity + (float)Math.sin(y / 50.0 + System.currentTimeMillis() / 1000.0) * 0.05f;
            g2d.setColor(new Color(255, 255, 255, (int)(lineOpacity * 255)));
            g2d.drawLine(0, y, BOARD_WIDTH * BLOCK_SIZE, y);
        }

        // Draw vertical lines
        for (int i = 0; i <= BOARD_WIDTH; i++) {
            int x = i * BLOCK_SIZE;
            float lineOpacity = baseOpacity + (float)Math.sin(x / 50.0 + System.currentTimeMillis() / 1000.0) * 0.05f;
            g2d.setColor(new Color(255, 255, 255, (int)(lineOpacity * 255)));
            g2d.drawLine(x, 0, x, BOARD_HEIGHT * BLOCK_SIZE);
        }
    }

    private void drawGlowingBlock(Graphics2D g2d, int x, int y, int colorIndex) {
        Color[] colors = {
            Color.BLACK,      // 0 - Empty
            new Color(0, 255, 255),  // 1 - I piece (Cyan)
            new Color(0, 0, 255),    // 2 - J piece (Blue)
            new Color(255, 165, 0),  // 3 - L piece (Orange)
            new Color(255, 255, 0),  // 4 - O piece (Yellow)
            new Color(0, 255, 0),    // 5 - S piece (Green)
            new Color(255, 0, 255),  // 6 - T piece (Magenta)
            new Color(255, 0, 0)     // 7 - Z piece (Red)
        };

        Color baseColor = colors[colorIndex];
   
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

 
        float animT = (float)((Math.sin(System.currentTimeMillis() / 400.0) + 1) / 2.0);
        Color borderColor = baseColor;
        boolean isActive = (currentPiece != null && colorIndex == currentPiece.getColor());
        if (isActive) {
            Color accent = new Color(255, 255, 255);
            borderColor = blend(baseColor, accent, animT * 0.5f);
        }

        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 180));
        g2d.drawRoundRect(x, y, BLOCK_SIZE, BLOCK_SIZE, 8, 8);

        float glowPhase = (float)(Math.sin(System.currentTimeMillis() / 300.0 + colorIndex) + 1) * 0.5f;
        float glowIntensity = isActive
            ? 1.5f + (float)Math.sin(System.currentTimeMillis() / 200.0) * 0.5f
            : 0.7f + glowPhase * 0.7f;
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                (int)(60 * glowIntensity / i)
            ));
            g2d.fillRoundRect(
                x - i * 2,
                y - i * 2,
                BLOCK_SIZE + i * 4,
                BLOCK_SIZE + i * 4,
                8,
                8
            );
        }

        GradientPaint gradient = new GradientPaint(
            x, y,
            baseColor,
            x + BLOCK_SIZE, y + BLOCK_SIZE,
            new Color(
                Math.max(0, baseColor.getRed() - 50),
                Math.max(0, baseColor.getGreen() - 50),
                Math.max(0, baseColor.getBlue() - 50)
            )
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x + 2, y + 2, BLOCK_SIZE - 4, BLOCK_SIZE - 4, 6, 6);

        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(x + 3, y + 3, BLOCK_SIZE - 6, BLOCK_SIZE - 6, 5, 5);

        GradientPaint highlight = new GradientPaint(
            x, y + 2,
            new Color(255, 255, 255, 120),
            x, y + BLOCK_SIZE / 2,
            new Color(255, 255, 255, 0)
        );
        g2d.setPaint(highlight);
        g2d.fillRoundRect(x + 4, y + 4, BLOCK_SIZE - 8, BLOCK_SIZE / 2 - 4, 6, 6);
    }

    private Color blend(Color c1, Color c2, float ratio) {
        int r = (int)(c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    private void drawGhostPiece(Graphics2D g2d) {
        int dropDistance = 0;
        while (isValidPosition(currentX, currentY + dropDistance + 1, currentPiece)) {
            dropDistance++;
        }

        int[][] shape = currentPiece.getShape();
        float ghostOpacity = 0.3f + (float)Math.sin(System.currentTimeMillis() / 400.0) * 0.1f;
        Color ghostColor = new Color(255, 255, 255, (int)(ghostOpacity * 255));

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int x = (currentX + j) * BLOCK_SIZE;
                    int y = (currentY + dropDistance + i) * BLOCK_SIZE;
                    
                    // Draw ethereal ghost block
                    g2d.setColor(ghostColor);
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    // Draw multiple layers for ethereal effect
                    for (int layer = 3; layer > 0; layer--) {
                        float layerOpacity = ghostOpacity / layer;
                        g2d.setColor(new Color(255, 255, 255, (int)(layerOpacity * 255)));
                        g2d.drawRoundRect(
                            x + layer,
                            y + layer,
                            BLOCK_SIZE - layer * 2,
                            BLOCK_SIZE - layer * 2,
                            4,
                            4
                        );
                    }
                }
            }
        }
    }

    private void drawMessage(Graphics2D g2d) {
        // Create semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw glowing text
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g2d.getFontMetrics();
        
        String[] lines = message.split("\n");
        int lineHeight = fm.getHeight();
        int totalHeight = lineHeight * lines.length;
        int startY = (getHeight() - totalHeight) / 2;
        
        for (int i = 0; i < lines.length; i++) {
            // Draw text glow
            g2d.setColor(new Color(0, 255, 255, 50));
            for (int j = 3; j > 0; j--) {
                int messageWidth = fm.stringWidth(lines[i]);
                g2d.drawString(lines[i],
                    (getWidth() - messageWidth) / 2 + j,
                    startY + (i * lineHeight) + j
                );
            }
            
            // Draw main text
            g2d.setColor(Color.WHITE);
            int messageWidth = fm.stringWidth(lines[i]);
            g2d.drawString(lines[i],
                (getWidth() - messageWidth) / 2,
                startY + (i * lineHeight)
            );
        }
    }

    /**
     * Clears the message shown on the game board
     */
    public void clearMessage() {
        message = null;
        repaint();
    }

    /**
     * Returns the current width of the board in blocks
     * @return The board width
     */
    public int getBoardWidth() {
        return BOARD_WIDTH;
    }

    /**
     * Returns the current height of the board in blocks
     * @return The board height
     */
    public int getBoardHeight() {
        return BOARD_HEIGHT;
    }

    /**
     * Returns the size of each block in pixels
     * @return The block size
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     * Returns the current board state
     * @return The game board array
     */
    public int[][] getBoardState() {
        return board;
    }

    /**
     * Sets a message to be displayed on the board
     * @param msg The message to display
     */
    public void setMessage(String msg) {
        message = msg;
        repaint();
    }

    /**
     * Resets the game board for a new game
     */
    public void resetBoard() {
        // Clear the board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = 0;
            }
        }

        // Reset pieces
        holdPiece = null;
        canHold = true;
        message = null;

        // Create new pieces
        nextPiece = Tetromino.getRandomPiece(random);
        createNewPiece();

        // Reinitialize cosmic effects
        if (effectsTimer != null) {
            effectsTimer.stop();
        }
        cosmicEffects = new CosmicEffects(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        effectsTimer = new Timer(16, e -> {
            cosmicEffects.update();
            repaint();
        });
        effectsTimer.start();

        repaint();
    }

    /**
     * Gets the preferred size of the panel
     * @return The preferred dimension
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public Tetromino getHoldPiece() {
        return holdPiece;
    }

    public int getLastLinesCleared() {
        return lastLinesCleared;
    }

    public void cleanup() {
        if (effectsTimer != null) {
            effectsTimer.stop();
        }
    }
}
