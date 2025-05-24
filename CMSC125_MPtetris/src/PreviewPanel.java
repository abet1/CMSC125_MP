import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * The PreviewPanel class is responsible for displaying the next piece
 * and the held piece in the side panel.
 */
public class PreviewPanel extends JPanel {
    private final int SIZE;
    private final int BLOCK_SIZE;
    private Tetromino piece;
    private float glowPhase = 0;

    /**
     * Constructor for the preview panel
     * @param size Size of the panel in blocks
     * @param blockSize Size of each block in pixels
     */
    public PreviewPanel(int size, int blockSize) {
        this.SIZE = size;
        this.BLOCK_SIZE = blockSize;
        setOpaque(false);
        setPreferredSize(new Dimension(SIZE * BLOCK_SIZE, SIZE * BLOCK_SIZE));

        // Start glow animation
        Timer glowTimer = new Timer(50, e -> {
            glowPhase = (glowPhase + 0.1f) % (float)(Math.PI * 2);
            repaint();
        });
        glowTimer.start();
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
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw panel background
        drawPanelBackground(g2d);

        // Draw piece if there is one
        if (piece != null) {
            int[][] shape = piece.getShape();
            int color = piece.getColor();
            
            // Calculate piece dimensions
            int pieceWidth = 0;
            int pieceHeight = 0;
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        pieceWidth = Math.max(pieceWidth, j + 1);
                        pieceHeight = Math.max(pieceHeight, i + 1);
                    }
                }
            }

            // Calculate block size to fit the piece
            int maxBlockSize = Math.min(getWidth() / Math.max(pieceWidth, 1), 
                                      getHeight() / Math.max(pieceHeight, 1));
            int actualBlockSize = Math.min(maxBlockSize, BLOCK_SIZE);

            // Calculate centering offsets
            int totalWidth = pieceWidth * actualBlockSize;
            int totalHeight = pieceHeight * actualBlockSize;
            int offsetX = (getWidth() - totalWidth) / 2;
            int offsetY = (getHeight() - totalHeight) / 2;

            // Draw each block of the piece with glow effect
            for (int i = 0; i < pieceHeight; i++) {
                for (int j = 0; j < pieceWidth; j++) {
                    if (shape[i][j] == 1) {
                        drawGlowingBlock(g2d, 
                            offsetX + j * actualBlockSize, 
                            offsetY + i * actualBlockSize, 
                            color,
                            actualBlockSize);
                    }
                }
            }
        }

        g2d.dispose();
    }

    private void drawPanelBackground(Graphics2D g2d) {
        // Create rounded rectangle for the panel
        int arc = 15;
        RoundRectangle2D.Float panelShape = new RoundRectangle2D.Float(
            0, 0, getWidth() - 1, getHeight() - 1, arc, arc
        );

        // Draw semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fill(panelShape);

        // Draw border with glow effect
        float borderGlow = (float)(Math.sin(glowPhase) + 1) * 0.5f;
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                UITheme.ACCENT_PRIMARY.getRed(),
                UITheme.ACCENT_PRIMARY.getGreen(),
                UITheme.ACCENT_PRIMARY.getBlue(),
                (int)(borderGlow * 255 / (i * 2))
            ));
            g2d.setStroke(new BasicStroke(i * 2));
            g2d.draw(panelShape);
        }
    }

    private void drawGlowingBlock(Graphics2D g2d, int x, int y, int colorIndex, int blockSize) {
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
        float glow = (float)(Math.sin(glowPhase) + 1) * 0.5f;
        
        // Draw outer glow
        for (int i = 3; i > 0; i--) {
            g2d.setColor(new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                (int)(glow * 255 / (i * 2))
            ));
            g2d.fillRoundRect(
                x - i * 2,
                y - i * 2,
                blockSize + i * 4,
                blockSize + i * 4,
                8,
                8
            );
        }

        // Draw main block
        g2d.setColor(baseColor);
        g2d.fillRoundRect(x + 1, y + 1, blockSize - 2, blockSize - 2, 4, 4);

        // Draw highlight
        g2d.setColor(new Color(255, 255, 255, (int)(glow * 100)));
        g2d.fillRoundRect(x + 3, y + 3, blockSize - 12, blockSize - 12, 2, 2);
    }
}