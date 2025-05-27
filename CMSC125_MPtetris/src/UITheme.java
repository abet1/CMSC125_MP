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
            private boolean hovered = false;
            private boolean pressed = false;
            private float hoverAnim = 0f;
            private float borderAnim = 0f;
            private Timer animTimer;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                animTimer = new Timer(16, e -> {
                    float target = hovered ? 1f : 0f;
                    if (Math.abs(hoverAnim - target) > 0.01f) {
                        hoverAnim += (target - hoverAnim) * 0.2f;
                        repaint();
                    } else if (hoverAnim != target) {
                        hoverAnim = target;
                        repaint();
                    }
                    borderAnim += 0.025f;
                    if (borderAnim > 1f) borderAnim -= 1f;
                    if (hovered) repaint();
                });
                animTimer.start();
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                    }
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        pressed = true;
                        repaint();
                    }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                Composite oldComp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
                GradientPaint glass = new GradientPaint(0, 0, new Color(40, 60, 80, 180), w, h, new Color(80, 100, 140, 120));
                g2d.setPaint(glass);
                g2d.fillRoundRect(0, 0, w, h, 32, 32);
                g2d.setComposite(oldComp);

                if (pressed) {
                    g2d.setColor(new Color(0,0,0,90));
                    g2d.fillRoundRect(4, 10, w-8, h-8, 32, 32);
                } else if (hovered) {
                    g2d.setColor(new Color(0,0,0,120));
                    g2d.fillRoundRect(2, 8, w-4, h-4, 32, 32);
                } else {
                    g2d.setColor(new Color(0,0,0,60));
                    g2d.fillRoundRect(4, 10, w-8, h-8, 32, 32);
                }

                if (hoverAnim > 0.01f) {
                    float t = (float)((Math.sin(borderAnim * 2 * Math.PI) + 1) / 2.0);
                    Color borderColor = blend(UITheme.ACCENT_PRIMARY, UITheme.ACCENT_SECONDARY, t);
                    int glowAlpha = (int)(120 * hoverAnim);
                    g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), glowAlpha));
                    g2d.setStroke(new BasicStroke(5f + 2f * hoverAnim));
                    g2d.drawRoundRect(2, 2, w-4, h-4, 28, 28);
                }

                g2d.setColor(new Color(255,255,255, hovered ? 60 : 30));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(2, 2, w-4, h-4, 28, 28);

                g2d.setFont(BUTTON_FONT.deriveFont(Font.BOLD, 22f));
                FontMetrics fm = g2d.getFontMetrics();
                String txt = getText();
                int textWidth = 0;
                for (char c : txt.toCharArray()) textWidth += fm.charWidth(c) + 2;
                int textX = (w - textWidth) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
                float gradT = (float)((Math.sin(borderAnim * 2 * Math.PI) + 1) / 2.0);
                GradientPaint textGrad = new GradientPaint(textX, textY - fm.getAscent(), blend(UITheme.ACCENT_PRIMARY, UITheme.ACCENT_SECONDARY, gradT), textX + textWidth, textY, blend(UITheme.ACCENT_SECONDARY, UITheme.ACCENT_PRIMARY, gradT));
                g2d.setColor(new Color(255,255,255,80));
                int tx = textX;
                for (char c : txt.toCharArray()) {
                    g2d.drawString(String.valueOf(c), tx+2, textY+2);
                    tx += fm.charWidth(c) + 2;
                }
                g2d.setPaint(textGrad);
                tx = textX;
                for (char c : txt.toCharArray()) {
                    g2d.drawString(String.valueOf(c), tx, textY);
                    tx += fm.charWidth(c) + 2;
                }
                g2d.dispose();
            }
            private Color blend(Color c1, Color c2, float ratio) {
                int r = (int)(c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
                int g = (int)(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
                int b = (int)(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
                return new Color(r, g, b);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(260, 60));
        button.setMaximumSize(new Dimension(280, 60));
        button.setMinimumSize(new Dimension(180, 52));
        button.setFont(BUTTON_FONT.deriveFont(Font.BOLD, 22f));
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