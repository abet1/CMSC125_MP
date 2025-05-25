import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private Clip backgroundMusic;
    private Map<String, Clip> soundEffects;
    private float masterVolume = 1.0f;
    private boolean isMultiplayer;
    private FloatControl musicVolumeControl;

    public SoundManager(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        this.soundEffects = new HashMap<>();
        loadSounds();
    }

    private void loadSounds() {
        try {
            // Load background music
            URL musicURL = getClass().getResource("/sounds/music.wav");
            if (musicURL != null) {
                backgroundMusic = AudioSystem.getClip();
                AudioInputStream stream = AudioSystem.getAudioInputStream(musicURL);
                backgroundMusic.open(stream);
                
                // Get volume control
                if (backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    musicVolumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                }
            }

            // Load sound effects with variations
            loadSoundEffect("drop", 3);   // 3 variations for piece drops
            loadSoundEffect("rotate", 2);  // 2 variations for rotation
            loadSoundEffect("clear", 4);   // 4 variations for line clears
            loadSoundEffect("levelup", 1); // 1 level up sound
            loadSoundEffect("gameover", 1); // 1 game over sound

        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
        }
    }

    private void loadSoundEffect(String name, int variations) {
        try {
            for (int i = 1; i <= variations; i++) {
                URL effectURL = getClass().getResource("/sounds/" + name + i + ".wav");
                if (effectURL != null) {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream stream = AudioSystem.getAudioInputStream(effectURL);
                    clip.open(stream);
                    soundEffects.put(name + i, clip);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect " + name + ": " + e.getMessage());
        }
    }

    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.setFramePosition(0);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            updateMusicVolume();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void updateLevel(int level) {
        // Increase music volume slightly with level
        if (musicVolumeControl != null) {
            float volumeIncrease = Math.min((level - 1) * 0.1f, 0.5f); // Max 50% increase
            setMusicVolume(masterVolume + volumeIncrease);
        }
    }

    private void updateMusicVolume() {
        if (musicVolumeControl != null) {
            setMusicVolume(masterVolume);
        }
    }

    private void setMusicVolume(float volume) {
        if (musicVolumeControl != null) {
            float min = musicVolumeControl.getMinimum();
            float max = musicVolumeControl.getMaximum();
            float range = max - min;
            float adjustedVolume = min + (range * volume);
            musicVolumeControl.setValue(adjustedVolume);
        }
    }

    public void playPieceDropSound() {
        playRandomVariation("drop", 3);
    }

    public void playRotateSound() {
        playRandomVariation("rotate", 2);
    }

    public void playLineClearSound() {
        playRandomVariation("clear", 4);
    }

    public void playLevelUpSound() {
        playSound("levelup1");
    }

    public void playGameOverSound() {
        playSound("gameover1");
    }

    private void playRandomVariation(String baseName, int variations) {
        int variation = (int)(Math.random() * variations) + 1;
        playSound(baseName + variation);
    }

    private void playSound(String name) {
        Clip clip = soundEffects.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void cleanup() {
        if (backgroundMusic != null) {
            backgroundMusic.close();
        }
        for (Clip clip : soundEffects.values()) {
            if (clip != null) clip.close();
        }
    }
} 