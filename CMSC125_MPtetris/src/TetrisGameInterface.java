import java.util.concurrent.locks.ReentrantLock;

public interface TetrisGameInterface {
    void updateNextPiecePanel(Tetromino piece);
    void updateHoldPiecePanel(Tetromino piece);
    void updateScore(int linesCleared);
    void gameOver();
    boolean isPaused();
    boolean isGameOver();
    ReentrantLock getGameLock();
} 