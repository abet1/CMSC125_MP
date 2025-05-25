import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UITheme {
    // Color palette
    public static final Color BACKGROUND_DARK = new Color(16, 24, 32);
    public static final Color BACKGROUND_MEDIUM = new Color(28, 36, 48);
    public static final Color ACCENT_PRIMARY = new Color(66, 214, 237);    // Cyan
    public static final Color ACCENT_SECONDARY = new Color(255, 89, 158);  // Pink
    public static final Color TEXT_PRIMARY = new Color(240, 240, 255);
    public static final Color TEXT_SECONDARY = new Color(180, 180, 200);

    // Fonts
    public static final Font TITLE_FONT = new Font("Orbitron", Font.BOLD, 48);
    public static final Font SUBTITLE_FONT = new Font("Orbitron", Font.BOLD, 24);
    public static final Font BUTTON_FONT = new Font("Orbitron", Font.BOLD, 18);
    public static final Font TEXT_FONT = new Font("Orbitron", Font.PLAIN, 14);

    // Button styling
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, ACCENT_PRIMARY,
                    getWidth(), getHeight(), ACCENT_SECONDARY
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw text with glow effect
                g2d.setFont(BUTTON_FONT);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // Draw glow
                g2d.setColor(new Color(255, 255, 255, 50));
                for (int i = 2; i >= 0; i--) {
                    g2d.drawString(getText(), textX + i, textY + i);
                }

                // Draw main text
                g2d.setColor(TEXT_PRIMARY);
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(200, 50));

        return button;
    }

    // Panel styling
    public static JPanel createStyledPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND_DARK,
                    getWidth(), getHeight(), BACKGROUND_MEDIUM
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add subtle grid pattern
                g2d.setColor(new Color(255, 255, 255, 10));
                int gridSize = 20;
                for (int x = 0; x < getWidth(); x += gridSize) {
                    g2d.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += gridSize) {
                    g2d.drawLine(0, y, getWidth(), y);
                }

                g2d.dispose();
            }
        };

        panel.setOpaque(false);
        return panel;
    }

    // Label styling
    public static JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    // Border styling
    public static Border createGlowBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_PRIMARY, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );
    }
} 