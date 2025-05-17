import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        int startX, startY;
        char direction = 'U'; // U D L R
        int velocityX = 0, velocityY = 0;
        Image image;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = this.startX = x;
            this.y = this.startY = y;
            this.width = width;
            this.height = height;
        }

        void updateDirection(char dir) {
            char prev = direction;
            direction = dir;
            updateVelocity();
            x += velocityX;
            y += velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    x -= velocityX;
                    y -= velocityY;
                    direction = prev;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            int speed = tileSize / 4;
            switch (direction) {
                case 'U': velocityX = 0; velocityY = -speed; break;
                case 'D': velocityX = 0; velocityY = speed; break;
                case 'L': velocityX = -speed; velocityY = 0; break;
                case 'R': velocityX = speed; velocityY = 0; break;
            }
        }

        void reset() {
            x = startX;
            y = startY;
            velocityX = 0;
            velocityY = 0;
        }
    }

    private final int rowCount = 21, columnCount = 19, tileSize = 32;
    private final int boardWidth = columnCount * tileSize, boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    private final String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX", "X        X        X", "X XX XXX X XXX XX X",
        "X                 X", "X XX X XXXXX X XX X", "X    X       X    X",
        "XXXX XXXX XXXX XXXX", "OOOX X       X XOOO", "XXXX X XXrXX X XXXX",
        "O       bpo       O", "XXXX X XXXXX X XXXX", "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX", "X        X        X", "X XX XXX X XXX XX X",
        "X  X     P     X  X", "XX X X XXXXX X X XX", "X    X   X   X    X",
        "X XXXXXX X XXXXXX X", "X                 X", "XXXXXXXXXXXXXXXXXXX"
    };

    private HashSet<Block> walls = new HashSet<>();
    private HashSet<Block> foods = new HashSet<>();
    private HashSet<Block> ghosts = new HashSet<>();
    private Block pacman;

    private Timer gameLoop;
    private final char[] directions = {'U', 'D', 'L', 'R'};
    private final Random random = new Random();

    private int score = 0, lives = 3;
    private boolean gameOver = false;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadImages();
        restartGame();

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
    }

    private void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();
        pacman = null;

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                int x = c * tileSize, y = r * tileSize;
                char ch = tileMap[r].charAt(c);

                switch (ch) {
                    case 'X': walls.add(new Block(wallImage, x, y, tileSize, tileSize)); break;
                    case 'b': ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize)); break;
                    case 'o': ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize)); break;
                    case 'p': ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize)); break;
                    case 'r': ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize)); break;
                    case 'P': pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize); break;
                    case ' ': foods.add(new Block(null, x + 14, y + 14, 4, 4)); break;
                }
            }
        }

        if (pacman == null) {
            throw new IllegalStateException("Pac-Man not found in map.");
        }

        for (Block ghost : ghosts) {
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
    }

    private void restartGame() {
        loadMap();
        resetPositions();
        score = 0;
        lives = 3;
        gameOver = false;
    }

    private void resetPositions() {
        pacman.reset();
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection(directions[random.nextInt(4)]);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over! Score: " + score, tileSize, tileSize);
        } else {
            g.drawString("Lives x" + lives + "  Score: " + score, tileSize, tileSize);
        }
    }

    private void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(pacman, ghost)) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    gameLoop.stop();
                    return;
                }
                resetPositions();
                return;
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean hitWall = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    hitWall = true;
                    break;
                }
            }

            if (hitWall || ghost.x < 0 || ghost.x + ghost.width > boardWidth) {
                Collections.shuffle(Arrays.asList(directions));
                for (char dir : directions) {
                    ghost.updateDirection(dir);
                    ghost.x += ghost.velocityX;
                    ghost.y += ghost.velocityY;
                    boolean valid = true;
                    for (Block wall : walls) {
                        if (collision(ghost, wall)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) break;
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                }
            }
        }

        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        if (foodEaten != null) foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            restartGame();
            gameLoop.start();
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    pacman.updateDirection('U'); pacman.image = pacmanUpImage; break;
            case KeyEvent.VK_DOWN:  pacman.updateDirection('D'); pacman.image = pacmanDownImage; break;
            case KeyEvent.VK_LEFT:  pacman.updateDirection('L'); pacman.image = pacmanLeftImage; break;
            case KeyEvent.VK_RIGHT: pacman.updateDirection('R'); pacman.image = pacmanRightImage; break;
        }
    }

    // To launch the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("PacMan Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PacMan());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
