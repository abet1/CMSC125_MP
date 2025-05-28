import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final String HIGHSCORE_FILE = "highscores.txt";
    private static final int MAX_HIGHSCORES = 5;
    private List<ScoreEntry> highScores;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        private String username;
        private int score;
        private int lines;
        private int level;

        public ScoreEntry(String username, int score, int lines, int level) {
            this.username = username;
            this.score = score;
            this.lines = lines;
            this.level = level;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // Descending order
        }

        public String getUsername() { return username; }
        public int getScore() { return score; }
        public int getLines() { return lines; }
        public int getLevel() { return level; }
    }

    public HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }

    private void loadHighScores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        String username = parts[0].trim();
                        int score = Integer.parseInt(parts[1].trim());
                        int lines = Integer.parseInt(parts[2].trim());
                        int level = Integer.parseInt(parts[3].trim());
                        highScores.add(new ScoreEntry(username, score, lines, level));
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        } catch (IOException e) {
            // File doesn't exist yet, that's okay
        }
        Collections.sort(highScores);
    }

    public void saveHighScore(String username, int score, int lines, int level) {
        if (isHighScore(score)) {
            highScores.add(new ScoreEntry(username, score, lines, level));
            Collections.sort(highScores);
            
            // Keep only top MAX_HIGHSCORES
            while (highScores.size() > MAX_HIGHSCORES) {
                highScores.remove(highScores.size() - 1);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(HIGHSCORE_FILE))) {
                for (ScoreEntry entry : highScores) {
                    writer.println(entry.getUsername() + "," + 
                                 entry.getScore() + "," + 
                                 entry.getLines() + "," + 
                                 entry.getLevel());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isHighScore(int score) {
        return highScores.size() < MAX_HIGHSCORES || score > highScores.get(highScores.size() - 1).getScore();
    }

    public List<ScoreEntry> getHighScores() {
        return new ArrayList<>(highScores);
    }

    public int getHighestScore() {
        return highScores.isEmpty() ? 0 : highScores.get(0).getScore();
    }
} 