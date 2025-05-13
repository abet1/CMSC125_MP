import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private Clip backgroundMusic;
    private Clip pieceDropSound;
    private Clip lineClearSound;
    private Clip gameOverSound;
    private boolean isMultiplayer;

    public SoundManager(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        loadSounds();
    }

    private void loadSounds() {
        try {

            URL bgMusicURL = getClass().getResource("/sounds/background.wav");
            if (bgMusicURL != null) {
                AudioInputStream bgMusicStream = AudioSystem.getAudioInputStream(bgMusicURL);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(bgMusicStream);
            }

            if (!isMultiplayer) {
                // Piece drop sound
                URL dropSoundURL = getClass().getResource("/sounds/drop.wav");
                if (dropSoundURL != null) {
                    AudioInputStream dropStream = AudioSystem.getAudioInputStream(dropSoundURL);
                    pieceDropSound = AudioSystem.getClip();
                    pieceDropSound.open(dropStream);
                }

                // Line clear sound
                URL clearSoundURL = getClass().getResource("/sounds/clear.wav");
                if (clearSoundURL != null) {
                    AudioInputStream clearStream = AudioSystem.getAudioInputStream(clearSoundURL);
                    lineClearSound = AudioSystem.getClip();
                    lineClearSound.open(clearStream);
                }

                // Game over sound
                URL gameOverURL = getClass().getResource("/sounds/gameover.wav");
                if (gameOverURL != null) {
                    AudioInputStream gameOverStream = AudioSystem.getAudioInputStream(gameOverURL);
                    gameOverSound = AudioSystem.getClip();
                    gameOverSound.open(gameOverStream);
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sounds: " + e.getMessage());
        }
    }

    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.setFramePosition(0);
        }
    }

    public void playPieceDropSound() {
        if (!isMultiplayer && pieceDropSound != null) {
            pieceDropSound.setFramePosition(0);
            pieceDropSound.start();
        }
    }

    public void playLineClearSound() {
        if (!isMultiplayer && lineClearSound != null) {
            lineClearSound.setFramePosition(0);
            lineClearSound.start();
        }
    }

    public void playGameOverSound() {
        if (!isMultiplayer && gameOverSound != null) {
            gameOverSound.setFramePosition(0);
            gameOverSound.start();
        }
    }

    public void cleanup() {
        if (backgroundMusic != null) backgroundMusic.close();
        if (pieceDropSound != null) pieceDropSound.close();
        if (lineClearSound != null) lineClearSound.close();
        if (gameOverSound != null) gameOverSound.close();
    }
} 