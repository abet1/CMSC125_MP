import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Random;

public class CosmicEffects {
    private ArrayList<Particle> particles;
    private ArrayList<GlowingShape> glowingShapes;
    private ArrayList<WaveEffect> waves;
    private final Random random;
    private final int width;
    private final int height;
    private float hueShift = 0;
    private float gridRotation = 0;
    private float gridPulse = 0;
    private long lastTime;
    private static final int PIXEL_SIZE = 2; // Size of each "pixel" in the effects
    private static final Color[] TETRIS_COLORS = {
        new Color(255, 0, 0),    // Red
        new Color(0, 255, 255),  // Cyan
        new Color(255, 165, 0),  // Orange
        new Color(255, 255, 0),  // Yellow
        new Color(128, 0, 255),  // Purple
        new Color(0, 255, 0),    // Green
        new Color(0, 128, 255)   // Blue
    };

    public CosmicEffects(int width, int height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.particles = new ArrayList<>();
        this.glowingShapes = new ArrayList<>();
        this.waves = new ArrayList<>();
        this.lastTime = System.currentTimeMillis();
        initializeShapes();
    }

    private void initializeShapes() {
        // Add some initial floating shapes
        for (int i = 0; i < 5; i++) {
            glowingShapes.add(new GlowingShape(
                random.nextInt(width),
                random.nextInt(height),
                random.nextInt(30) + 20,
                random.nextFloat() * 360
            ));
        }
    }

    public void addLineClearEffect(int y) {
        // Add pixelated expanding ring
        waves.add(new PixelatedWaveEffect(width/2, y, width * 2f, 60));
        
        // Add pixel burst
        particles.add(new PixelatedFlashParticle(width/2, y));
        
        // Add explosive burst of pixel particles
        for (int i = 0; i < 100; i++) {
            float angle = random.nextFloat() * (float)Math.PI * 2;
            float speed = random.nextFloat() * 12 + 6;
            Color color = TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)];
            particles.add(new PixelatedParticle(
                width / 2,
                y,
                (float)Math.cos(angle) * speed,
                (float)Math.sin(angle) * speed,
                color,
                random.nextInt(4) + 2 // Random pixel size
            ));
        }

        // Add pixelated sparkles
        for (int i = 0; i < 50; i++) {
            float angle = random.nextFloat() * (float)Math.PI * 2;
            float speed = random.nextFloat() * 4 + 2;
            Color color = TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)];
            particles.add(new PixelatedSparkle(
                width / 2,
                y,
                (float)Math.cos(angle) * speed,
                (float)Math.sin(angle) * speed,
                color
            ));
        }
    }

    public void addPieceDropEffect(int x, int y) {
        // Add pixelated wave
        waves.add(new PixelatedWaveEffect(x, y, 60, 60));
        
        // Add pixel burst
        particles.add(new PixelatedFlashParticle(x, y));
        
        // Add pixelated light burst
        Color dropColor = TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)];
        for (int i = 0; i < 20; i++) {
            float angle = random.nextFloat() * (float)Math.PI * 2;
            float speed = random.nextFloat() * 3f + 1f;
            particles.add(new PixelatedParticle(
                x,
                y,
                (float)Math.cos(angle) * speed,
                (float)Math.sin(angle) * speed,
                dropColor,
                2 // Small pixel size
            ));
        }
    }

    public void addRotationEffect(int x, int y) {
        // Add spiral particles
        for (int i = 0; i < 8; i++) {
            float angle = (float)i / 8 * (float)Math.PI * 2;
            particles.add(new Particle(
                x,
                y,
                (float)Math.cos(angle) * 2,
                (float)Math.sin(angle) * 2,
                angle * 360 / (float)(Math.PI * 2)
            ));
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        // Update particles
        particles.removeIf(particle -> !particle.update(delta));

        // Update waves
        waves.removeIf(wave -> !wave.update(delta));

        // Update glowing shapes
        for (GlowingShape shape : glowingShapes) {
            shape.update(delta);
        }

        // Update grid effects
        gridRotation += delta * 0.1f;
        gridPulse = (float)Math.sin(currentTime / 1000.0) * 0.1f;

        // Update global hue shift
        hueShift = (hueShift + delta * 10) % 360;
    }

    public void draw(Graphics2D g2d) {
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw cosmic background
        drawCosmicBackground(g2d);

        // Draw wave effects
        for (WaveEffect wave : waves) {
            wave.draw(g2d);
        }

        // Draw particles
        for (Particle particle : particles) {
            particle.draw(g2d);
        }

        // Draw glowing shapes
        for (GlowingShape shape : glowingShapes) {
            shape.draw(g2d);
        }

        // Draw animated grid
        drawAnimatedGrid(g2d);
    }

    private void drawCosmicBackground(Graphics2D g2d) {
        // Create gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, Color.getHSBColor((hueShift / 360f), 0.8f, 0.2f),
            width, height, Color.getHSBColor(((hueShift + 180) / 360f), 0.8f, 0.1f)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // Add subtle star field
        g2d.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(2) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }

    private void drawAnimatedGrid(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 30));
        
        // Create transform for grid rotation
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(gridRotation, width/2, height/2);
        g2d.scale(1 + gridPulse, 1 + gridPulse);

        // Draw grid lines
        float spacing = 30;
        for (float x = -width; x <= width * 2; x += spacing) {
            g2d.draw(new Line2D.Float(x, -height, x, height * 2));
        }
        for (float y = -height; y <= height * 2; y += spacing) {
            g2d.draw(new Line2D.Float(-width, y, width * 2, y));
        }

        g2d.setTransform(oldTransform);
    }

    private class Particle {
        float x, y;
        float vx, vy;
        float hue;
        float alpha = 1.0f;
        float size;

        public Particle(float x, float y, float vx, float vy, float hue) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.hue = hue;
            this.size = random.nextFloat() * 4 + 2;
        }

        public boolean update(float delta) {
            x += vx * 30 * delta;
            y += vy * 30 * delta;
            alpha -= delta * 0.5f;
            return alpha > 0;
        }

        public void draw(Graphics2D g2d) {
            Color color = Color.getHSBColor(hue / 360f, 0.8f, 1.0f);
            g2d.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(alpha * 255)
            ));

            // Draw glowing particle
            for (int i = 3; i > 0; i--) {
                float glowSize = size * (i * 0.7f);
                g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    alpha * (1f / i)
                ));
                g2d.fill(new Ellipse2D.Float(
                    x - glowSize/2,
                    y - glowSize/2,
                    glowSize,
                    glowSize
                ));
            }
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }

    private class GlowingShape {
        float x, y;
        float angle;
        float rotationSpeed;
        float size;
        float hue;
        float pulsePhase;

        public GlowingShape(float x, float y, float size, float hue) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.hue = hue;
            this.angle = random.nextFloat() * 360;
            this.rotationSpeed = (random.nextFloat() - 0.5f) * 2;
            this.pulsePhase = random.nextFloat() * (float)Math.PI * 2;
        }

        public void update(float delta) {
            angle += rotationSpeed * delta * 45;
            pulsePhase += delta * 2;
            x += Math.sin(pulsePhase * 0.5) * delta * 5;
            y += Math.cos(pulsePhase * 0.7) * delta * 5;

            // Wrap around screen
            if (x < -size) x = width + size;
            if (x > width + size) x = -size;
            if (y < -size) y = height + size;
            if (y > height + size) y = -size;
        }

        public void draw(Graphics2D g2d) {
            float pulseSize = size * (0.8f + 0.2f * (float)Math.sin(pulsePhase));
            
            // Create shape path
            Path2D.Float path = new Path2D.Float();
            for (int i = 0; i < 5; i++) {
                double angleRad = Math.toRadians(angle + i * 72);
                float px = (float)(Math.cos(angleRad) * pulseSize);
                float py = (float)(Math.sin(angleRad) * pulseSize);
                if (i == 0) {
                    path.moveTo(x + px, y + py);
                } else {
                    path.lineTo(x + px, y + py);
                }
            }
            path.closePath();

            // Draw glowing shape
            Color baseColor = Color.getHSBColor(hue / 360f, 0.8f, 1.0f);
            for (int i = 4; i > 0; i--) {
                g2d.setColor(new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    255 / (i * 2)
                ));
                g2d.setStroke(new BasicStroke(i * 2));
                g2d.draw(path);
            }
        }
    }

    private class WaveEffect {
        float x, y, width, height;
        float lifetime = 1.0f;
        float intensity = 1.0f;

        public WaveEffect(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean update(float delta) {
            lifetime -= delta;
            intensity = lifetime;
            return lifetime > 0;
        }

        public void draw(Graphics2D g2d) {
            int alpha = (int)(intensity * 255);
            g2d.setColor(new Color(255, 255, 255, alpha));
            
            // Draw expanding rings
            for (int i = 0; i < 3; i++) {
                float scale = 1 + (1 - intensity) * (i + 1) * 0.5f;
                float currentWidth = width * scale;
                float currentHeight = height * scale;
                g2d.draw(new RoundRectangle2D.Float(
                    x - currentWidth/2,
                    y - currentHeight/2,
                    currentWidth,
                    currentHeight,
                    10,
                    10
                ));
            }
        }
    }

    private class PixelatedParticle extends Particle {
        protected Color color;
        protected int pixelSize;

        public PixelatedParticle(float x, float y, float vx, float vy, Color color, int pixelSize) {
            super(x, y, vx, vy, 0);
            this.color = color;
            this.pixelSize = pixelSize;
            this.alpha = 1.0f;
        }

        @Override
        public boolean update(float delta) {
            x += vx * delta * 60;
            y += vy * delta * 60;
            alpha -= delta * 0.8f;
            return alpha > 0;
        }

        @Override
        public void draw(Graphics2D g2d) {
            int alpha = (int)(this.alpha * 255);
            Color currentColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha
            );
            g2d.setColor(currentColor);

            // Draw pixelated particle
            int px = (int)(x - pixelSize * PIXEL_SIZE / 2);
            int py = (int)(y - pixelSize * PIXEL_SIZE / 2);
            for(int i = 0; i < pixelSize; i++) {
                for(int j = 0; j < pixelSize; j++) {
                    g2d.fillRect(
                        px + i * PIXEL_SIZE,
                        py + j * PIXEL_SIZE,
                        PIXEL_SIZE,
                        PIXEL_SIZE
                    );
                }
            }
        }
    }

    private class PixelatedSparkle extends PixelatedParticle {
        private float twinklePhase = 0;

        public PixelatedSparkle(float x, float y, float vx, float vy, Color color) {
            super(x, y, vx, vy, color, 2);
        }

        @Override
        public boolean update(float delta) {
            super.update(delta);
            twinklePhase += delta * 15;
            return alpha > 0;
        }

        @Override
        public void draw(Graphics2D g2d) {
            float twinkle = (float)Math.abs(Math.sin(twinklePhase));
            int alpha = (int)(this.alpha * twinkle * 255);
            Color currentColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha
            );
            g2d.setColor(currentColor);

            // Draw cross-shaped pixel sparkle
            g2d.fillRect((int)x - PIXEL_SIZE, (int)y, PIXEL_SIZE * 3, PIXEL_SIZE);
            g2d.fillRect((int)x, (int)y - PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE * 3);
        }
    }

    private class PixelatedFlashParticle extends Particle {
        private Color[] colors;

        public PixelatedFlashParticle(float x, float y) {
            super(x, y, 0, 0, 0);
            this.alpha = 1.0f;
            this.colors = new Color[]{
                TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)],
                TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)],
                TETRIS_COLORS[random.nextInt(TETRIS_COLORS.length)]
            };
        }

        @Override
        public boolean update(float delta) {
            alpha -= delta * 4f;
            return alpha > 0;
        }

        @Override
        public void draw(Graphics2D g2d) {
            int size = 20;
            int centerX = (int)x;
            int centerY = (int)y;

            for (int i = 0; i < 3; i++) {
                int currentSize = size - i * 4;
                Color color = new Color(
                    colors[i].getRed(),
                    colors[i].getGreen(),
                    colors[i].getBlue(),
                    (int)(alpha * 255 / (i + 1))
                );
                g2d.setColor(color);

                // Draw pixelated diamond shape
                for (int j = 0; j < currentSize; j++) {
                    int offset = Math.abs(currentSize/2 - j);
                    for (int k = 0; k < currentSize - offset*2; k++) {
                        g2d.fillRect(
                            centerX - (currentSize/2 - offset) * PIXEL_SIZE + k * PIXEL_SIZE,
                            centerY - currentSize/2 * PIXEL_SIZE + j * PIXEL_SIZE,
                            PIXEL_SIZE,
                            PIXEL_SIZE
                        );
                    }
                }
            }
        }
    }

    private class PixelatedWaveEffect extends WaveEffect {
        public PixelatedWaveEffect(float x, float y, float width, float height) {
            super(x, y, width, height);
        }

        @Override
        public void draw(Graphics2D g2d) {
            int alpha = (int)(intensity * 255);
            
            // Draw expanding pixelated rings
            for (int i = 0; i < 3; i++) {
                float scale = 1 + (1 - intensity) * (i + 1) * 0.5f;
                float currentWidth = width * scale;
                float currentHeight = height * scale;
                
                Color ringColor = TETRIS_COLORS[i % TETRIS_COLORS.length];
                g2d.setColor(new Color(
                    ringColor.getRed(),
                    ringColor.getGreen(),
                    ringColor.getBlue(),
                    alpha / (i + 1)
                ));

                // Draw pixelated rectangle outline
                int left = (int)(x - currentWidth/2);
                int top = (int)(y - currentHeight/2);
                int right = (int)(x + currentWidth/2);
                int bottom = (int)(y + currentHeight/2);

                // Draw horizontal lines
                for (int px = left; px < right; px += PIXEL_SIZE) {
                    g2d.fillRect(px, top, PIXEL_SIZE, PIXEL_SIZE);
                    g2d.fillRect(px, bottom, PIXEL_SIZE, PIXEL_SIZE);
                }
                // Draw vertical lines
                for (int py = top; py < bottom; py += PIXEL_SIZE) {
                    g2d.fillRect(left, py, PIXEL_SIZE, PIXEL_SIZE);
                    g2d.fillRect(right, py, PIXEL_SIZE, PIXEL_SIZE);
                }
            }
        }
    }
} 