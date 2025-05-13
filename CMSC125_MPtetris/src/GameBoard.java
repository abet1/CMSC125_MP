import javax.swing.*;
import java.awt.*;
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
        Tetromino rotated = currentPiece.getRotated();

        // Try standard rotation
        if (isValidPosition(currentX, currentY, rotated)) {
            currentPiece = rotated;
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
        // Add the current piece to the board
        int[][] shape = currentPiece.getShape();
        int color = currentPiece.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int y = currentY + i;
                    int x = currentX + j;

                    if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                        board[y][x] = color;
                    }
                }
            }
        }

        // Play piece drop sound
        if (gameInstance instanceof TetrisGame) {
            ((TetrisGame) gameInstance).playPieceDropSound();
        }

        // Check for completed lines
        int linesCleared = clearLines();
        gameInstance.updateScore(linesCleared);

        // Reset hold flag
        canHold = true;

        // Create a new piece
        createNewPiece();
    }

    /**
     * Clears completed lines and moves lines above down
     * @return Number of lines cleared
     */
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

        // Draw background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw grid
        g2d.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g2d.drawLine(0, i * BLOCK_SIZE, BOARD_WIDTH * BLOCK_SIZE, i * BLOCK_SIZE);
        }

        for (int i = 0; i <= BOARD_WIDTH; i++) {
            g2d.drawLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        }

        // Draw placed blocks
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != 0) {
                    drawBlock(g2d, j * BLOCK_SIZE, i * BLOCK_SIZE, board[i][j]);
                }
            }
        }

        // Draw current piece
        if (currentPiece != null && !gameInstance.isGameOver()) {
            int[][] shape = currentPiece.getShape();
            int color = currentPiece.getColor();

            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int x = (currentX + j) * BLOCK_SIZE;
                        int y = (currentY + i) * BLOCK_SIZE;
                        drawBlock(g2d, x, y, color);
                    }
                }
            }
        }

        // Draw ghost piece (shows where the piece will land)
        if (currentPiece != null && !gameInstance.isGameOver() && !gameInstance.isPaused()) {
            int dropDistance = 0;
            while (isValidPosition(currentX, currentY + dropDistance + 1, currentPiece)) {
                dropDistance++;
            }

            int[][] shape = currentPiece.getShape();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int x = (currentX + j) * BLOCK_SIZE;
                        int y = (currentY + dropDistance + i) * BLOCK_SIZE;
                        drawGhostBlock(g2d, x, y);
                    }
                }
            }
        }

        // Draw message if needed
        if (message != null) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2d.getFontMetrics();
            
            // Split message into lines and draw each line
            String[] lines = message.split("\n");
            int lineHeight = fm.getHeight();
            int totalHeight = lineHeight * lines.length;
            int startY = (getHeight() - totalHeight) / 2;
            
            for (int i = 0; i < lines.length; i++) {
                int messageWidth = fm.stringWidth(lines[i]);
                g2d.drawString(lines[i], 
                    (getWidth() - messageWidth) / 2, 
                    startY + (i * lineHeight));
            }

            // If the game is paused, this message will clear when unpaused
            if (message.startsWith("PAUSED") && !gameInstance.isPaused()) {
                message = null;
            }
        }
    }

    /**
     * Draws a single block at the specified position
     * @param g2d Graphics context
     * @param x X position in pixels
     * @param y Y position in pixels
     * @param colorIndex Color index of the block
     */
    private void drawBlock(Graphics2D g2d, int x, int y, int colorIndex) {
        Color[] colors = {
                Color.BLACK,      // 0 - Empty
                Color.CYAN,       // 1 - I piece
                Color.BLUE,       // 2 - J piece
                Color.ORANGE,     // 3 - L piece
                Color.YELLOW,     // 4 - O piece
                Color.GREEN,      // 5 - S piece
                Color.MAGENTA,    // 6 - T piece
                Color.RED         // 7 - Z piece
        };

        Color color = colors[colorIndex];

        // Fill block
        g2d.setColor(color);
        g2d.fillRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);

        // Highlight
        g2d.setColor(color.brighter());
        g2d.drawLine(x + 1, y + 1, x + 1, y + BLOCK_SIZE - 2);
        g2d.drawLine(x + 1, y + 1, x + BLOCK_SIZE - 2, y + 1);

        // Shadow
        g2d.setColor(color.darker());
        g2d.drawLine(x + BLOCK_SIZE - 2, y + 1, x + BLOCK_SIZE - 2, y + BLOCK_SIZE - 2);
        g2d.drawLine(x + 1, y + BLOCK_SIZE - 2, x + BLOCK_SIZE - 2, y + BLOCK_SIZE - 2);
    }

    /**
     * Draws a ghost block showing where the piece will land
     * @param g2d Graphics context
     * @param x X position in pixels
     * @param y Y position in pixels
     */
    private void drawGhostBlock(Graphics2D g2d, int x, int y) {
        // Draw ghost block outline
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
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
}
