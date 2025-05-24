# CMSC125_MP - Tetris Effect

A Java-based implementation of the classic Tetris game with modern visual effects, inspired by Tetris Effect. Features both single-player and two-player modes.

## Features

- **Multiple Game Modes**
  - Single Player Mode: Enhanced classic Tetris gameplay
  - Two Player Mode: Competitive Tetris for two players

- **Modern Visual Effects**
  - Dynamic particle systems for piece drops and line clears
  - Glowing blocks with pulsing effects
  - Ethereal ghost piece visualization
  - Cosmic background effects
  - Smooth animations and transitions

- **Polished UI**
  - Clean and intuitive interface with modern styling
  - Animated start screen with dynamic effects
  - Game mode selection screen
  - Preview panels for next and held pieces
  - Real-time score tracking
  - Visual feedback for all player actions

- **Sound System**
  - Dynamic sound effects for all game actions
  - Piece rotations and movements
  - Line clear effects
  - Level up celebrations
  - Game over sounds
  - Background music system

## Technical Details

The game is built using:
- Java Swing for the GUI
- Object-Oriented Programming principles
- Multi-threading for game logic and animations
- Custom particle system for visual effects
- Sound management system

### Key Components

- `TetrisApp.java`: Main application entry point
- `StartScreen.java`: Initial game screen with cosmic effects
- `GameModeScreen.java`: Mode selection interface
- `TetrisGame.java`: Single-player game implementation
- `TwoPlayerTetrisGame.java`: Two-player game implementation
- `GameBoard.java`: Core game board logic with visual effects
- `Tetromino.java`: Tetris piece implementation
- `CosmicEffects.java`: Visual effects management
- `SoundManager.java`: Audio system

## Controls

### Single Player Mode
- Arrow Keys: Move piece
- Up Arrow: Rotate piece
- Down Arrow: Soft drop
- Space: Hard drop
- C: Hold piece
- P: Pause game
- M: Return to menu (when paused)
- R: Restart (when game over)

### Two Player Mode
Player 1:
- WASD: Move piece
- W: Rotate piece
- S: Soft drop
- Space: Hard drop
- C: Hold piece

Player 2:
- Arrow Keys: Move piece
- Up Arrow: Rotate piece
- Down Arrow: Soft drop
- Ctrl: Hard drop
- Shift: Hold piece

Common:
- P: Pause game
- M: Return to menu (when paused)
- R: Restart (when game over)

## Project Structure

```
CMSC125_MPtetris/
├── src/
│   ├── sounds/         # Game sound effects
│   ├── TetrisApp.java  # Main entry point
│   ├── GameBoard.java  # Core game logic
│   ├── CosmicEffects.java  # Visual effects system
│   ├── SoundManager.java   # Audio management
│   └── ...            # Other game components
├── .idea/             # IDE configuration
└── .gitignore        # Git ignore rules
```

## Multi-threading Implementation

The game utilizes multiple threads to handle different aspects of gameplay:

### Single Player Mode
1. **Main/UI Thread**
   - Handles the Swing Event Dispatch Thread (EDT)
   - Manages user interface updates
   - Processes keyboard input
   - Updates visual effects

2. **Game Logic Thread**
   - Controls piece movement and game physics
   - Manages game state updates
   - Handles collision detection
   - Updates score and level progression
   - Manages particle effects timing

### Two Player Mode
1. **Main/UI Thread**
   - Handles the Swing Event Dispatch Thread (EDT)
   - Manages shared UI components
   - Processes keyboard inputs for both players
   - Updates visual effects

2. **Player 1 Game Thread**
   - Controls Player 1's game board
   - Manages piece movement and physics
   - Handles collision detection
   - Updates Player 1's score and level

3. **Player 2 Game Thread**
   - Controls Player 2's game board
   - Manages piece movement and physics
   - Handles collision detection
   - Updates Player 2's score and level

### Thread Synchronization
- Uses `ReentrantLock` for thread-safe game state updates
- Implements `AtomicBoolean` for pause and game over states
- Ensures thread safety between UI updates and game logic
- Prevents race conditions in piece movement and board updates
- Coordinates visual effects with game events

## Visual Effects System

The game features a sophisticated visual effects system:

1. **Particle Effects**
   - Line clear explosions with colorful particles
   - Soft glow effects for piece drops
   - Rotating grid animations
   - Dynamic background effects

2. **Block Rendering**
   - Glowing block outlines
   - Pulsing intensity based on game state
   - Color gradients and transparency
   - Dynamic lighting effects

3. **UI Animations**
   - Smooth transitions between screens
   - Pulsing text effects
   - Dynamic message overlays
   - Responsive visual feedback

## How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Java Runtime Environment (JRE)

### Running the Game

1. **Using an IDE (Recommended)**:
   - Open the project in your preferred Java IDE (IntelliJ IDEA, Eclipse, etc.)
   - Navigate to `src/TetrisApp.java`
   - Run the `main` method

2. **Using Command Line**:
   ```bash
   # Navigate to the project directory
   cd CMSC125_MPtetris

   # Compile the source files
   javac src/*.java

   # Run the game
   java -cp src TetrisApp
   ```

## Development

This project demonstrates:
- Advanced GUI Programming with custom effects
- Multi-threaded Game Development
- Event Handling and User Input
- Game State Management
- Sound System Implementation
- Particle Systems and Visual Effects