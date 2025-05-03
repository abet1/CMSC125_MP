import javax.swing.*;
import java.awt.*;

/**
 * The PreviewPanel class is responsible for displaying the next piece
 * and the held piece in the side panel.
 */
public class PreviewPanel extends JPanel {
    private final int SIZE;
    private final int BLOCK_SIZE;
    private Tetromino piece;

    /**
     * Constructor for the preview panel
     * @param size Size of the panel in blocks
     * @param blockSize Size of each block in pixels
     */
    public PreviewPanel(int size, int blockSize) {
        this.SIZE = size;
        this.BLOCK_SIZE = blockSize;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(SIZE * BLOCK_SIZE, SIZE * BLOCK_SIZE));
    }

    /**
     * Updates the preview with a new piece
     * @param piece The piece to preview
     */
    public void updatePreview(Tetromino piece) {
        this.piece = piece;
        repaint();
    }

    /**
     * Custom painting for the preview panel
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw piece if there is one
        if (piece != null) {
            int[][] shape = piece.getShape();
            int color = piece.getColor();
            int width = shape[0].length;
            int height = shape.length;

            // Calculate centering offsets
            int offsetX = (SIZE - width) * BLOCK_SIZE / 2;
            int offsetY = (SIZE - height) * BLOCK_SIZE / 2;

            // Draw each block of the piece
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (shape[i][j] == 1) {
                        drawBlock(g2d, offsetX + j * BLOCK_SIZE, offsetY + i * BLOCK_SIZE, color);
                    }
                }
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
}