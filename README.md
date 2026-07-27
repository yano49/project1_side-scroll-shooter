# Side-Scroll Shooter Game

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Swing](https://img.shields.io/badge/Swing-007396?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/)

> A Java-based 2D side-scrolling shooter game where the player controls a spaceship, fights different types of aliens and mini-bosses, collects power-ups, upgrades weapons, and survives increasingly difficult enemy waves.

## 🎮 Game Overview

**Objective:** Survive as long as possible while destroying enemies and earning points. The game becomes progressively more challenging as enemy speed, movement patterns, and spawn rates increase over time.

### Core Features

- 🚀 Side-scrolling gameplay
- 👾 Random enemy spawning with multiple movement patterns
- 📈 Increasing difficulty over time
- 🔫 Multiple weapon levels with reload mechanics
- 🛸 Mini-boss battles
- ⚡ Power-up collectibles
- 🏆 Score and high-score tracking

---

## 🕹️ Main Features

### Player Movement

The player can move in four directions:

- ⬆️ Up
- ⬇️ Down
- ⬅️ Left
- ➡️ Right

_The player must avoid enemy collisions and incoming attacks while positioning the spaceship to shoot enemies._

### Enemy System

Enemies spawn from the right side of the screen and move toward the left.

**Enemy behaviors include:**

- Straight movement
- Zigzag movement
- Reverse-zigzag movement
- Random vertical movement
- Increasing speed over time
- Increasing spawn amount every 30 seconds

> **Note:** After reaching 100 points, additional aliens may appear with slower movement patterns. Their speed will not exceed the mini-boss speed.

---

## 📊 Difficulty Progression

| Time Period           | Features                                                   |
| --------------------- | ---------------------------------------------------------- |
| **0–30 Seconds**      | Aliens spawn slowly (5–7 aliens), mostly straight movement |
| **30–60 Seconds**     | Increased spawn rate, zigzag movement introduced           |
| **60–90 Seconds**     | Faster aliens, more unpredictable movement patterns        |
| **90–120 Seconds**    | First mini-boss appears, first power-up becomes available  |
| **After 120 Seconds** | Mini-bosses at scheduled intervals, increasing difficulty  |

---

## 🏆 Scoring System

| Enemy Type | Score     |
| ---------- | --------- |
| Alien      | +2 points |
| Mini-Boss  | +5 points |

**Display:**

- Current score (top-left)
- Best score (top-left)
- Current weapon level (top-left)
- Current ammunition (top-right)
- Reload status (top-right)

---

## 🔫 Weapon System

### Weapon Level 1

- **Unlocked:** Default
- **Shoots:** One straight bullet
- **Ammunition:** 3 shots
- **Reload Time:** 3 seconds

### Weapon Level 2

- **Unlocked:** 50 points
- **Shoots:** Two bullets
- **Ammunition:** 4 shots
- **Reload Time:** 4 seconds
- **Speed Bonus:** 1.5× original speed

### Weapon Level 3

- **Unlocked:** 150 points
- **Shoots:** Three bullets (diagonally up, straight, diagonally down)
- **Ammunition:** 6 shots
- **Reload Time:** 5 seconds
- **Speed:** Same as Level 2

### Weapon Level 4

- **Unlocked:** 250 points
- **Weapon:** Laser-beam (activates with `F` key)
- **Shoots:** Three laser beams
- **Ammunition:** 10 shots
- **Reload Time:** 6 seconds
- **Speed:** Level 2 movement speed

### Reload System

Each weapon level has a limited number of shots. When ammunition reaches zero:

1. Player temporarily cannot shoot
2. Reload timer begins
3. Ammunition restored after reload finishes
4. Display updates automatically

> **Power-Up Effect:** During active power-up, reloading is disabled and player can shoot without ammunition limitations.

---

## ⚡ Power-Up System

**Appearance:** Orange airplane collectible

**Spawning:** Every 30 seconds, moving from right side toward player

**Effects when collected:**

- 🟠 Changes to orange spaceship
- 🛡️ Temporary immunity to enemy bullets
- 💥 Can destroy aliens by colliding with them
- ⚡ Temporarily receives maximum weapon level
- 🏃 Level 2 movement speed
- 🔫 Unlimited shooting without reloading

_Power-up effect lasts for a limited duration. After effect ends, player returns to weapon level earned from current score._

---

## 👾 Mini-Boss System

**Appearance:** After surviving early enemy waves

**Behavior:**

- Enters from right side
- Remains inside visible game area
- Moves around the player
- Attempts to crash into player
- Requires multiple hits to defeat
- Moves slower than fastest aliens
- Reappears after defeat

**Reward:** +5 points

---

## 💥 Collision System

The game detects collisions between:

- Player and alien
- Player and mini-boss
- Player and enemy bullet
- Player bullet and alien
- Player bullet and mini-boss
- Player and power-up

> **Note:** Under normal conditions, colliding with an alien, mini-boss, or enemy bullet causes the player to lose. During power-up, the player can safely collide with aliens and destroy them.

================================================================================

## 🖥️ User Interface

================================================================================

### Top-Left Display

- Current score
- Best score
- Current weapon level

### Top-Right Display

- Current ammunition / Maximum ammunition
- Reloading status
- Reload countdown

Examples:
Ammo: 4 / 4 (Normal)
Reloading: 2.5s (During reload)
Ammo: Unlimited (During power-up)

================================================================================
CONTROLS
================================================================================

Key Action

---

Up Arrow / W Move up
Down Arrow / S Move down
Left Arrow / A Move left
Right Arrow / D Move right
Spacebar Shoot normal weapon
F Activate/shoot laser weapon
Enter Start/restart game
Escape Exit/pause game

================================================================================
TECHNOLOGIES USED
================================================================================

- Java
- Java Swing
- Java AWT
- Object-Oriented Programming
- Collision detection
- Timer-based game loop
- Sprite-based animation
- Git and GitHub

================================================================================
PROJECT STRUCTURE
================================================================================

project1_side-scroll-shooter/
│
├── src/
│ ├── Scene1.java
│ ├── Player.java
│ ├── Alien.java
│ ├── MiniBoss.java
│ ├── Bullet.java
│ ├── PowerUp.java
│ └── Main.java
│
├── assets/
│ ├── sprites.png
│ ├── background.png
│ └── other-game-assets
│
├── README.txt
└── .gitignore

================================================================================
HOW TO RUN THE PROJECT
================================================================================

PREREQUISITES

- Java Development Kit (JDK)
- Java-supported IDE (IntelliJ IDEA, Eclipse, or NetBeans)
- Git (if cloning the repository)

STEPS

1. Clone the repository
   git clone <repository-url>

2. Open the project
   - Open project folder in a Java IDE
   - Confirm image assets are in the correct asset folder
   - Locate the main Java class

3. Compile from terminal
   javac src/\*.java

4. Run the game
   java -cp src Main
   (Replace Main with actual main-class name if different)

================================================================================
TEAM MEMBERS AND CONTRIBUTIONS
================================================================================

## 6611201 Aung Kaung Myat

Contributions:

- Developed player movement system
- Implemented player controls
- Worked on player collision detection
- Assisted with main game loop
- Tested general gameplay functionality

## 6611932 Wai Yan Mya Thaung

Contributions:

- Developed mini-boss system
- Implemented mini-boss movement and repeated spawning
- Developed power-up system
- Added recurring power-up spawning
- Implemented temporary player immunity
- Added unlimited ammunition during power-up mode
- Developed score-based weapon upgrade system
- Implemented weapon levels 1-4
- Added ammunition and reload mechanics
- Added score and best-score displays
- Adjusted alien difficulty progression
- Fixed bullet alignment after weapon upgrades

## 6611950 Min Thu Htet

Contributions:

- Developed alien enemy system
- Implemented enemy spawning
- Added straight, zigzag, reverse-zigzag, and random movement
- Worked on enemy speed and spawn-rate progression
- Developed side-scrolling background
- Added game assets and sprite rendering
- Designed parts of user interface
- Assisted with collision detection, testing, and bug fixing

## TEAM COLLABORATION

The project was completed by three team members. Each member was responsible for different gameplay systems, while the team worked together to:

- Integrate all Java classes and features
- Test the complete game
- Resolve Git and GitHub merge conflicts
- Fix gameplay and visual bugs
- Balance enemy difficulty and spawn timing
- Improve the user interface
- Ensure all features worked correctly together

================================================================================
CURRENT PROGRESS
================================================================================

## COMPLETED FEATURES

[✔] Player four-direction movement
[✔] Side-scrolling background
[✔] Enemy spawning from the right
[✔] Multiple alien movement patterns
[✔] Time-based difficulty progression
[✔] Score system
[✔] Best-score tracking
[✔] Mini-boss system
[✔] Repeated mini-boss spawning
[✔] Power-up spawning
[✔] Power-up immunity
[✔] Weapon-level upgrades
[✔] Ammunition limits
[✔] Reload timers
[✔] Unlimited ammunition during power-up
[✔] User-interface information displays

================================================================================
FUTURE IMPROVEMENTS
================================================================================

- Add a final boss
- Add additional enemy types
- Add sound effects
- Add background music
- Add health points and lives
- Add more power-up types
- Add weapon-selection options
- Add a pause menu
- Add difficulty-selection modes
- Add additional game scenes
- Add player animation
- Add boss attack patterns
- Add online leaderboards
- Improve sprite and collision accuracy

================================================================================
KNOWN ISSUES
================================================================================

Possible issues requiring further testing:

- Bullet position after changing weapon levels
- Mini-boss spawn timing
- Power-up sprite alignment
- Enemy speed at high scores
- Laser duration and cooldown
- Reload timer synchronization
- Collision boundaries for different sprites
- Performance with many enemies simultaneously

================================================================================
LEARNING OUTCOMES
================================================================================

Through this project, the team practiced:

- Object-oriented programming
- Java game development
- Event handling
- Keyboard input handling
- Collision detection
- Sprite rendering
- Timers and animation loops
- Difficulty balancing
- Team collaboration
- Git version control
- GitHub conflict resolution
- Debugging and software testing

================================================================================
CREDITS
================================================================================

This project was created as a team programming project. Game assets, images, and sprites are used for educational purposes. Any externally sourced assets should be credited according to their respective licenses.

================================================================================
LICENSE
================================================================================

This project is intended for educational use. Add a formal open-source license, such as the MIT License, if the team plans to publish, distribute, or allow reuse of the source code.

================================================================================

---

## 🚀 How to Run the Project

### Prerequisites

- ☕ Java Development Kit (JDK)
- 🖥️ Java-supported IDE (IntelliJ IDEA, Eclipse, or NetBeans)
- 📦 Git (if cloning the repository)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yano49/project1_side-scroll-shooter.git
   ================================================================================
   ```
