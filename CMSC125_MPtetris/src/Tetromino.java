import java.util.Random;

/**
 * The Tetromino class represents a tetris piece with its shape, color and rotation.
 * It contains all the standard tetris pieces (I, J, L, O, S, T, Z) and methods
 * to manipulate them.
 */
public class Tetromino {
    // Constants for piece types
    public static final int I_PIECE = 1;
    public static final int J_PIECE = 2;
    public static final int L_PIECE = 3;
    public static final int O_PIECE = 4;
    public static final int S_PIECE = 5;
    public static final int T_PIECE = 6;
    public static final int Z_PIECE = 7;

    // Piece properties
    private int[][] shape;
    private int color;
    private int type;

    /**
     * Constructor for a tetromino piece
     * @param type The type of piece (I, J, L, O, S, T, Z)
     */
    public Tetromino(int type) {
        this.type = type;
        this.color = type;
        initializeShape(type);
    }

    /**
     * Initialize the shape of the piece based on its type
     * @param type The type of piece
     */
    private void initializeShape(int type) {
        switch (type) {
            case I_PIECE:
                // I piece (cyan)
                shape = new int[][] {
                        {0, 0, 0, 0},
                        {1, 1, 1, 1},
                        {0, 0, 0, 0},
                        {0, 0, 0, 0}
                };
                break;
            case J_PIECE:
                // J piece (blue)
                shape = new int[][] {
                        {1, 0, 0},
                        {1, 1, 1},
                        {0, 0, 0}
                };
                break;
            case L_PIECE:
                // L piece (orange)
                shape = new int[][] {
                        {0, 0, 1},
                        {1, 1, 1},
                        {0, 0, 0}
                };
                break;
            case O_PIECE:
                // O piece (yellow)
                shape = new int[][] {
                        {1, 1},
                        {1, 1}
                };
                break;
            case S_PIECE:
                // S piece (green)
                shape = new int[][] {
                        {0, 1, 1},
                        {1, 1, 0},
                        {0, 0, 0}
                };
                break;
            case T_PIECE:
                // T piece (purple)
                shape = new int[][] {
                        {0, 1, 0},
                        {1, 1, 1},
                        {0, 0, 0}
                };
                break;
            case Z_PIECE:
                // Z piece (red)
                shape = new int[][] {
                        {1, 1, 0},
                        {0, 1, 1},
                        {0, 0, 0}
                };
                break;
        }
    }

    /**
     * Gets the shape of the piece
     * @return 2D array representing the shape
     */
    public int[][] getShape() {
        return shape;
    }

    /**
     * Gets the color index of the piece
     * @return Color index
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets the type of the piece
     * @return Type constant
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the width of the piece
     * @return Width in blocks
     */
    public int getWidth() {
        return shape[0].length;
    }

    /**
     * Gets the height of the piece
     * @return Height in blocks
     */
    public int getHeight() {
        return shape.length;
    }

    /**
     * Creates a new tetromino with the shape rotated 90 degrees clockwise
     * @return A new rotated tetromino
     */
    public Tetromino getRotated() {
        // O piece doesn't rotate
        if (type == O_PIECE) {
            return this;
        }

        Tetromino rotated = new Tetromino(type);
        int[][] oldShape = this.shape;
        int height = oldShape.length;
        int width = oldShape[0].length;

        // Create a new rotated shape
        int[][] newShape = new int[width][height];

        // Rotate 90 degrees clockwise
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                newShape[j][height - 1 - i] = oldShape[i][j];
            }
        }

        rotated.shape = newShape;
        return rotated;
    }

    /**
     * Creates a random tetromino piece
     * @param random Random number generator
     * @return A new random tetromino
     */
    public static Tetromino getRandomPiece(Random random) {
        int pieceType = random.nextInt(7) + 1; // 1-7 for the different piece types
        return new Tetromino(pieceType);
    }
}