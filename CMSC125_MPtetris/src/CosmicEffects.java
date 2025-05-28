import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class CosmicEffects {
    private final int width;
    private final int height;
    private final List<Star> stars;
    private final List<Particle> particles;
    private final Random random;
    private final AtomicBoolean running;
    private Thread animationThread;
    private ArrayList<GlowingShape> glowingShapes;
    private ArrayList<WaveEffect> waves;
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
        this.stars = new ArrayList<>();
        this.particles = new ArrayList<>();
        this.random = new Random();
        this.running = new AtomicBoolean(true);
        this.glowingShapes = new ArrayList<>();
        this.waves = new ArrayList<>();
        this.lastTime = System.currentTimeMillis();

        // Initialize stars
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(
                random.nextInt(width),
                random.nextInt(height),
                random.nextFloat() * 2 + 1,
                random.nextFloat() * 0.5f + 0.1f
            ));
        }

        // Initialize floating shapes
        for (int i = 0; i < 5; i++) {
            glowingShapes.add(new GlowingShape(
                random.nextInt(width),
                random.nextInt(height),
                random.nextInt(30) + 20,
                random.nextFloat() * 360
            ));
        }

        // Start animation thread
        startAnimationThread();
    }

    private void startAnimationThread() {
        animationThread = new Thread(() -> {
            while (running.get()) {
                update();
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }

    public void update() {
        // Update stars
        for (Star star : stars) {
            star.update();
            if (star.y > height) {
                star.y = 0;
                star.x = random.nextInt(width);
            }
        }

        // Update particles
        particles.removeIf(particle -> particle.life <= 0);
        for (Particle particle : particles) {
            particle.update();
        }

        // Update waves
        waves.removeIf(wave -> !wave.update(0.016f));

        // Update glowing shapes
        for (GlowingShape shape : glowingShapes) {
            shape.update(0.016f);
        }

        // Update grid effects
        gridRotation += 0.016f * 0.1f;
        gridPulse = (float)Math.sin(System.currentTimeMillis() / 1000.0) * 0.1f;

        // Update global hue shift
        hueShift = (hueShift + 0.016f * 10) % 360;
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

        // Draw stars
        for (Star star : stars) {
            float alpha = 0.5f + (float)Math.sin(System.currentTimeMillis() * star.twinkleSpeed) * 0.5f;
            g2d.setColor(new Color(1f, 1f, 1f, alpha));
            g2d.fillOval((int)star.x, (int)star.y, (int)star.size, (int)star.size);
        }

        // Draw particles
        for (Particle particle : particles) {
            float alpha = particle.life / particle.maxLife;
            g2d.setColor(new Color(
                particle.color.getRed(),
                particle.color.getGreen(),
                particle.color.getBlue(),
                (int)(alpha * 255)
            ));
            g2d.fillOval(
                (int)(particle.x - particle.size/2),
                (int)(particle.y - particle.size/2),
                (int)particle.size,
                (int)particle.size
            );
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

    public void addRotationEffect(int x, int y) {
        for (int i = 0; i < 8; i++) {
            float angle = (float)(i * Math.PI / 4);
            float speed = 2f;
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, new Color(100, 200, 255)));
        }
    }

    public void addPieceDropEffect(int x, int y) {
        for (int i = 0; i < 12; i++) {
            float angle = random.nextFloat() * (float)Math.PI * 2;
            float speed = random.nextFloat() * 3f + 1f;
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            particles.add(new Particle(x, y, vx, vy, new Color(255, 255, 255)));
        }
    }

    public void addLineClearEffect(int y) {
        for (int i = 0; i < 20; i++) {
            float x = random.nextFloat() * width;
            float vx = (random.nextFloat() - 0.5f) * 4f;
            float vy = -random.nextFloat() * 4f - 2f;
            particles.add(new Particle(x, y, vx, vy, new Color(255, 200, 100)));
        }
    }

    public void cleanup() {
        running.set(false);
        if (animationThread != null) {
            animationThread.interrupt();
            try {
                animationThread.join(100); // Wait for thread to finish with timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            animationThread = null;
        }
        
        // Clear all collections
        stars.clear();
        particles.clear();
        glowingShapes.clear();
        waves.clear();
        
        // Force garbage collection
        System.gc();
    }

    private static class Star {
        float x, y;
        final float size;
        final float twinkleSpeed;

        Star(float x, float y, float size, float twinkleSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.twinkleSpeed = twinkleSpeed;
        }

        void update() {
            y += 0.2f;
        }
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        final float maxLife;
        final Color color;
        final float size;

        Particle(float x, float y, float vx, float vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLife = 60;
            this.life = maxLife;
            this.size = 4f;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.1f;
            life--;
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
} 