import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.event.*;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.Dimension;
import java.awt.Font;

// JComponent is a base class for custom components 
public class Breakout extends JComponent {

    public enum Collision {NOTHING, TOP, BOTTOM, LEFT, RIGHT, TL_CORNER, TR_CORNER, BL_CORNER, BR_CORNER}

    static int BALL_RADIUS = 5;
    static float BALL_SPEED = 5.0f;
    static int WIDTH = 1000;
    static int HEIGHT = 600; 
    static int X_DIVISION = 14;
    static int Y_DIVISION = 20;
    static int X_OFFSET = 2;
    static int Y_OFFSET = 2;
    static int PADDLE_WIDTH = 50;
    static int PADDLE_HEIGHT = 4;
    static float PADDLE_SPEED = 10.0f;
    static int FRAME_RATE = 40;

    static int MAX_BALLS = 3;

    // These values are used for scaling object positions when the window is resized.
    static int PREVIOUS_WIDTH = 1000;
    static int PREVIOUS_HEIGHT = 600;
    static int PREVIOUS_WIDTH_P = 1000;
    static int PREVIOUS_HEIGHT_P = 600;

    // Levels
    static int LEVEL = 0;
    static int SCORE = 0;
    static int LIVES = 5;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() {
                if (args.length == 2) {
                    // Set the frame rate followed by the ball and paddle speed.
                    FRAME_RATE = Integer.parseInt(args[0]);
                    // We divide by 2^0.5 since BALL_SPEED is the x/y component of the speed.
                    BALL_SPEED = Integer.parseInt(args[1]) / (FRAME_RATE * 1.414f);
                    PADDLE_SPEED = 1.5f * Integer.parseInt(args[1]) / FRAME_RATE;
                }
                JFrame f = new JFrame("Breakout"); // jframe is the app window
                Breakout canvas = new Breakout(f);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(WIDTH, HEIGHT); // window size
                f.setMinimumSize(new Dimension(340, 330));
                f.setContentPane(canvas); // add canvas to jframe
                f.setVisible(true); // show the window
		        f.addKeyListener(canvas.new BreakoutKeyListener());
                
		        // Start thread here after the JFrame is up and running.
                (new Thread(canvas.new Test())).start();
            }
        });
    }

    public class BreakoutKeyListener extends KeyAdapter {
	    public void keyPressed(KeyEvent e) {
            if (LEVEL > 0) {
                // Left arrow
                if (e.getKeyCode() == 37) {
                    paddle.setSpeed(-PADDLE_SPEED);
                }
                // Right arrow
                if (e.getKeyCode() == 39) {
                    paddle.setSpeed(PADDLE_SPEED);
                }
            }
	    }
	    public void keyReleased(KeyEvent e) {
            if (LEVEL > 0) {
                if (e.getKeyCode() == 37 || e.getKeyCode() == 39) {
                    paddle.setSpeed(0);
                }
                // Space bar
                if (e.getKeyCode() == 32) {
                    // Only pause or unpause if the game has started.
                    if (started) {
                        active = !active;
                    }
                }
            }
            // Enter key
            if (e.getKeyCode() == 10) {
                if (LEVEL > 0) {
                    // If the game hasn't been started, start it.
                    if (!started) {
                        started = true;
                        active = true;
                    }
                }
                else {
                    LEVEL = 1;
                }
            }
            // q key
            if (e.getKeyCode() == 81) {
                jframe.dispose();
            }
            if (e.getKeyCode() == 50) {
                active = false;
                started = false;
                LEVEL = 2;
                brick_list.ResetBricks();
            }
	    }
    }

    // Object properties
    private JFrame jframe;
    private Ball[] balls;
    private BrickList brick_list;
    private Paddle paddle;
    boolean active;
    boolean started;
    

    // Constructor for Breakout
    public Breakout(JFrame j) {
        jframe = j; 
        balls = new Ball[MAX_BALLS];
        balls[0] = new Ball(0);
        balls[1] = null;
        balls[2] = null;
        brick_list = new BrickList(5, 10);
	    paddle = new Paddle();
        active = false;
        started = false;
    }

    public void PrintCenter(Graphics2D g2, String str, int y) {
        int width = g2.getFontMetrics().stringWidth(str);
        g2.drawString(str, (getWidth() - width) / 2, y);
    }

    // custom graphics drawing 
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
        					RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));

        if (LEVEL == -1) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            String message = "GAME OVER :(";
            int message_width = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (getWidth() - message_width) / 2, 30);
            String score = "SCORE: " + SCORE;
            int score_width = g2.getFontMetrics().stringWidth(score);
            g2.drawString(score, (getWidth() - score_width) / 2, 55);
        }
        else if (LEVEL == 0) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            
            String name = "Name: Liam Palmer";
            int name_width = g2.getFontMetrics().stringWidth(name);
            g2.drawString(name, (getWidth() - name_width) / 2, 30);
            String id = "UserID: lcpalmer";
            int id_width = g2.getFontMetrics().stringWidth(id);
            g2.drawString(id, (getWidth() - id_width) / 2, 55);
            String student = "Student #: 20534162";
            int student_width = g2.getFontMetrics().stringWidth(student);
            g2.drawString(student, (getWidth() - student_width) / 2, 80);
           
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            PrintCenter(g2, "GOAL 1: Keep ball from hitting bottom of screen!", 160);
            PrintCenter(g2, "GOAL 2: Eliminate all blocks in order to level up!", 180);
            PrintCenter(g2, "LEFT ARROW: Move paddle left.", 200);
            PrintCenter(g2, "RIGHT ARROW: Move paddle right.", 220);
            PrintCenter(g2, "Q: Quit Game", 240);
            PrintCenter(g2, "2: Skip to Level 2", 260);
            PrintCenter(g2, "ENTER: Start Game and Start Play", 280);
            PrintCenter(g2, "SPACE BAR: Pause Game", 300);
        }
        else if (LEVEL == 3) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            String message = "GAME WON :)";
            int message_width = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (getWidth() - message_width) / 2, 30);
            String score = "SCORE: " + SCORE;
            int score_width = g2.getFontMetrics().stringWidth(score);
            g2.drawString(score, (getWidth() - score_width) / 2, 55);
            // Final page showing data when won
        }
        else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString("SCORE: " + SCORE, 3, 17);
            String lives_text = "LIVES: " + LIVES;
            int lives_width = g2.getFontMetrics().stringWidth(lives_text);
            g2.drawString(lives_text, (getWidth() - lives_width + 2) / 2, 17);
            String level_text = "LEVEL: " + LEVEL;
            int level_width = g2.getFontMetrics().stringWidth(level_text);
            g2.drawString(level_text, getWidth() - level_width - 3, 17);
            for (int iter = 0; iter < MAX_BALLS; ++iter) {
                if (balls[iter] != null) {
                    balls[iter].draw(g2);
                }
            }
            brick_list.draw(g2);
	        paddle.draw(g2);
        }
    }

    public class Test implements Runnable {
        public void run() {
            while (true) {
                if (LEVEL > 0) {
                    // Move the paddle first so the ball can collide with it appropriately.
                    paddle.move();
                    // Once the paddle is in the correct position, move the ball.
                    for (int iter = 0; iter < MAX_BALLS; ++iter) {
                        if (balls[iter] != null) {
                            balls[iter].move();
                        }
                    }
                    if (brick_list.ZeroBricks()) {
                        if (LEVEL == 1) {
                            LEVEL = 2;
                            brick_list.ResetBricks();
                        }
                        else if (LEVEL == 2) {
                            LEVEL = 3;
                        }                    
                        active = false;
                        started = false;
                    }
                    if (LIVES == 0) {
                        LEVEL = -1;
                    }
                    repaint();
                }
                try {
                    Thread.sleep(1000 / FRAME_RATE);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public class Paddle {
        // The x and y positions are stored as floats to avoid significant rounding errors
        // during rescaling of the paddle position (when the window is resized).
	    private float x_position;
	    private float y_position;
	    private float paddle_speed;

	    public Paddle() {
	        x_position = WIDTH - (HEIGHT / 3);
    	    y_position = HEIGHT - 40;
	        paddle_speed = 0;
	    }

    	public void move() {
            // In the case of window resizing, scale the paddle's x position appropriately.
            if (PREVIOUS_WIDTH_P != getWidth()) {
                x_position = x_position * getWidth() / PREVIOUS_WIDTH_P;
                PREVIOUS_WIDTH_P = getWidth();
            }
            // In the case of window resizing, scale the paddle's y position appropriately.
            if (PREVIOUS_HEIGHT_P != getHeight()) {
                y_position = y_position * getHeight() / PREVIOUS_HEIGHT_P;
                PREVIOUS_HEIGHT_P = getHeight();
            }

            // Only change the x position if in active state.
            if (active) {
	            x_position += paddle_speed;
            }
            // When the game is not active, ensure the paddle speed is reset to zero.
            else {
                paddle_speed = 0;
            }

            // If we have not started, reset the paddle's position.
            if (!started) {
	            x_position = getWidth() - (getHeight() / 3);
                x_position = x_position < 0 ? 0 : x_position;
                return;
            }

            // Make sure the paddle remains on screen.
	        if (x_position > getWidth() - PADDLE_WIDTH) {
		        x_position = getWidth() - PADDLE_WIDTH;
	        }
	        if (x_position < 0) {
		        x_position = 0;
	        }
	    }

	    public void setSpeed(float speed) {
	        paddle_speed = speed;
	    }

        public Collision intersect(int pos_x, int pos_y){
            if (pos_x >= x_position && pos_x <= x_position + PADDLE_WIDTH) {
                if (pos_y >= y_position - BALL_RADIUS && pos_y <= y_position) {
                    return Collision.TOP;
                }
            }
            if (pos_y <= y_position + 3) {
	            if (Math.sqrt(Math.pow(pos_x - x_position, 2) + Math.pow(pos_y - y_position, 2)) < BALL_RADIUS) {
		            return Collision.TL_CORNER;
	            }
	            if (Math.sqrt(Math.pow(pos_x + - (x_position + PADDLE_WIDTH), 2) + Math.pow(pos_y - y_position, 2)) < BALL_RADIUS) {
		            return Collision.TR_CORNER;
	            }
            }
            return Collision.NOTHING;
        }

        public void draw(Graphics2D g) {
	        g.setColor(Color.WHITE);
            g.fill(new Rectangle2D.Double(x_position, y_position, PADDLE_WIDTH, PADDLE_HEIGHT));
	    }
    }

    public class Ball {
        private float position_x;
        private float position_y;
        private float direction_x;
        private float direction_y;
        // Which ball this is in the event of multiple balls.
        private int order;

        public Ball(int ball_order) {
            if (order == 0) {
                position_x = WIDTH - 20;
                position_y = 2 * HEIGHT / 3;
                direction_x = -BALL_SPEED;
                direction_y = BALL_SPEED;
            }
            order = ball_order;
        }

        public void move() {
            // In the case of window resizing, scale the paddle's x and y positions appropriately.
            if (PREVIOUS_WIDTH != getWidth()) {
                position_x = position_x * getWidth() / PREVIOUS_WIDTH;
                PREVIOUS_WIDTH = getWidth();
            }
            if (PREVIOUS_HEIGHT != getHeight()) {
                position_y = position_y * getHeight() / PREVIOUS_HEIGHT;
                PREVIOUS_HEIGHT = getHeight();
            }
            
            // If the game has not started, ensure the ball is positioned appropriately along the right vertical wall.
            if (!started) {
                position_x = getWidth() - 20;
                position_y = 2 * getHeight() / 3;
                position_x = position_x < 0 ? 0 : position_x;
                position_y = position_y < 0 ? 0 : position_y;
                direction_x = -BALL_SPEED;
                direction_y = BALL_SPEED;
                return;
            }
            // If the game is not active, do not move the ball.
            if (!active) {
                return;
            }

            // The new x and y positions after a full increment.
            float new_position_x = position_x + direction_x;
            float new_position_y = position_y + direction_y;
            // The signs of the x and y directions.
            float sign_direction_x = direction_x < 0 ? -1.0f : 1.0f;
            float sign_direction_y = direction_y < 0 ? -1.0f : 1.0f;
            // The magnitudes of the x and y directions.
            int magnitude_direction_x = (int)(direction_x * sign_direction_x);
            int magnitude_direction_y = (int)(direction_y * sign_direction_y);
            for(int iteration = 1; iteration <= magnitude_direction_x + 1; ++iteration) {
                // Update the signs of the x and y directions.
                sign_direction_x = direction_x < 0 ? -1.0f : 1.0f;
                sign_direction_y = direction_y < 0 ? -1.0f : 1.0f;
                if (iteration == magnitude_direction_x + 1) {
                    // Adding the following value to the positions will ensure we have travelled
                    // the correct distance.
                    float diff_x = ((float)Math.abs(new_position_x) - (int)Math.abs(new_position_x)) * sign_direction_x;
                    float diff_y = ((float)Math.abs(new_position_y) - (int)Math.abs(new_position_y)) * sign_direction_y;
                    if (diff_x == 0.0f) {
                        diff_x = 0.4f * sign_direction_x;
                    }
                    if (diff_y == 0.0f) {
                        diff_y = 0.4f * sign_direction_y;
                    }
                    position_x += diff_x;
                    position_y += diff_y;
                }
                else {
                    // Otherwise, increment the positions by 1 pixel and check for collisions.
                    position_x += sign_direction_x;
                    position_y += sign_direction_y;
                }
                // Check horizontal intersections (from above or below).
                Brick[][] bricks = brick_list.getBricks();
                for (int a = 0; a < bricks.length; ++a) {
                    for (int b = 0; b < bricks[a].length; ++b) {
                        if (bricks[a][b] != null) {
                            Collision hit = bricks[a][b].intersectHorizontal((int)position_x, (int)position_y);
                            if (hit == Collision.TOP) {
                                direction_y = -BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                            if (hit == Collision.BOTTOM) {
                                direction_y = BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                        }
                    }
                }
                // Check vertical intersections (from left or right).
                for (int a = 0; a < bricks.length; ++a) {
                    for (int b = 0; b < bricks[a].length; ++b) {
                        if (bricks[a][b] != null) {
                            Collision hit = bricks[a][b].intersectVertical((int)position_x, (int)position_y);
                            if (hit == Collision.LEFT) {
                                direction_x = -BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                            if (hit == Collision.RIGHT) {
                                direction_x = BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                        }
                    }
                }
                // Check corner intersections: Finding the appropriate intersection relies on not finding
                // a horizontal or vertical intersection above, so check those intersections first.
                for (int a = 0; a < bricks.length; ++a) {
                    for (int b = 0; b < bricks[a].length; ++b) {
                        if (bricks[a][b] != null) {
                            Collision hit = bricks[a][b].intersectCorner((int)position_x, (int)position_y);
                            if (hit == Collision.TR_CORNER) {
                                direction_x = BALL_SPEED;
                                direction_y = -BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                            if (hit == Collision.BL_CORNER) {
                                direction_x = -BALL_SPEED;
                                direction_y = BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                            if (hit == Collision.TL_CORNER) {
                                direction_x = -BALL_SPEED;
                                direction_y = -BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                            if (hit == Collision.BR_CORNER) {
                                direction_x = BALL_SPEED;
                                direction_y = BALL_SPEED;
                                brick_list.nullify(a, b);
                            }
                        }
                    }
                }

                // Check for intersections with the boundary of the window.
                if (position_x < BALL_RADIUS) {
                    position_x = BALL_RADIUS;
                    direction_x = BALL_SPEED;
                    SCORE += 10;
                }
                if (position_x > getWidth() - BALL_RADIUS) {
                    position_x = getWidth() - BALL_RADIUS;
                    direction_x = -BALL_SPEED;
                    SCORE += 10;
                }
                if (position_y < BALL_RADIUS) {
                    position_y = BALL_RADIUS;
                    direction_y = BALL_SPEED;
                    SCORE += 10;
                }

                // Check for intersections with the paddle.
                Collision hit = paddle.intersect((int)position_x, (int)position_y);
                if (hit == Collision.TOP) {
                    direction_y = -BALL_SPEED;
                    SCORE += 10;
                }
                else if (hit == Collision.TL_CORNER) {
                    direction_y = -BALL_SPEED;
                    direction_x = -BALL_SPEED;
                    SCORE += 10;
                }
                else if (hit == Collision.TR_CORNER) {
                    direction_y = -BALL_SPEED;
                    direction_x = BALL_SPEED;
                    SCORE += 10;
                }

                if (position_y > getHeight() - BALL_RADIUS) {
                    // Here we intersect the bottom of the screen
                    active = false;
                    started = false;
                    LIVES--;
                    position_x = getWidth() - 20;
                    position_y = 2 * getHeight() / 3;
                    direction_x = -BALL_SPEED;
                    direction_y = BALL_SPEED;
                    return;
                }
            }
        }

        public void draw(Graphics2D g) {
            g.setColor(Color.RED);
            g.fill(new Ellipse2D.Double(position_x - BALL_RADIUS, position_y - BALL_RADIUS, 2 * BALL_RADIUS, 2 * BALL_RADIUS));
        }
    }

    public class BrickList {
        private Brick[][] bricks;

	    public Brick[][] getBricks() {
	        return bricks;
	    }
        
        public boolean ZeroBricks() {
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    if (bricks[a][b] != null) {
                        return false;
                    }
                }
            }
            return true;
        }

        public void ResetBricks() {
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    bricks[a][b] = new Brick(a, b);
                }
            }
        }

        public BrickList(int r, int c) {
            bricks = new Brick[r][c];
            for (int a = 0; a < r; ++a) {
                for (int b = 0; b < c; ++b) {
                    bricks[a][b] = new Brick(a, b);
                }
            }
        }

        public void nullify(int r, int c) {
            if (r >= 0 && r < bricks.length && c >=0 && c < bricks[r].length) {
                if (bricks[r][c].getLives() == 0) {
                    bricks[r][c] = null;
                }
                else {
                    bricks[r][c].reduceLives();
                }
                SCORE += 10;
            }
        }

        public void draw(Graphics2D g) {
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    if (bricks[a][b] != null) {
                        bricks[a][b].draw(g);
                    }
                }
            }
        }
    }

    public class Brick {
        private int row;
        private int column;
        private int lives;

        public Brick(int r, int c) {
            row = r;
            column = c;
            if (LEVEL == 2 && r % 2 == 1 && c % 2 == 1) {
                lives = 1;
            }
            else {
                lives = 0;
            }
        }

        public int getLives() {
            return lives;
        }

        public void reduceLives() {
            lives--;
        }

	    public Collision intersectHorizontal(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
            if (position_x >= left_x_pixel && position_x <= right_x_pixel) {
		        if (position_y >= top_y_pixel - BALL_RADIUS && position_y <= top_y_pixel) {
                    return Collision.TOP;
                }
                if (position_y <= bottom_y_pixel + BALL_RADIUS && position_y >= bottom_y_pixel) {
		            return Collision.BOTTOM;
		        }
	        }
	        return Collision.NOTHING;
	    }

	    public Collision intersectVertical(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
            if (position_y >= top_y_pixel && position_y <= bottom_y_pixel) {
		        if (position_x >= left_x_pixel - BALL_RADIUS && position_x <= left_x_pixel) {
                    return Collision.LEFT;
                }
                if (position_x <= right_x_pixel + BALL_RADIUS && position_x >= right_x_pixel) {
		            return Collision.RIGHT;
		        }
	        }
	        return Collision.NOTHING;
	    }

	    public Collision intersectCorner(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
	        if (Math.sqrt(Math.pow(position_x - left_x_pixel, 2) + Math.pow(position_y - top_y_pixel, 2)) < BALL_RADIUS) {
		        return Collision.TL_CORNER;
	        }
	        if (Math.sqrt(Math.pow(position_x - left_x_pixel, 2) + Math.pow(position_y - bottom_y_pixel, 2)) < BALL_RADIUS) {
		        return Collision.BL_CORNER;
	        }
	        if (Math.sqrt(Math.pow(position_x - right_x_pixel, 2) + Math.pow(position_y - top_y_pixel, 2)) < BALL_RADIUS) {
		        return Collision.TR_CORNER;
	        }
	        if (Math.sqrt(Math.pow(position_x - right_x_pixel, 2) + Math.pow(position_y - bottom_y_pixel, 2)) < BALL_RADIUS) {
		        return Collision.BR_CORNER;
	        }
	        return Collision.NOTHING;
	    }

        public void draw(Graphics2D g) {
            if ((row + column) % 5 == 0) {
		        g.setColor(Color.ORANGE);
	        }
            if ((row + column) % 5 == 1) {
		        g.setColor(Color.GREEN);
	        }
            if ((row + column) % 5 == 2) {
		        g.setColor(Color.MAGENTA);
	        }
            if ((row + column) % 5 == 3) {
		        g.setColor(Color.PINK);
	        }
            if ((row + column) % 5 == 4) {
		        g.setColor(Color.YELLOW);
	        }
            if (LEVEL == 2) {
                if (row % 2 == 1 && column % 2 == 1) {
                    if (lives == 1) {
                        g.setColor(Color.YELLOW);
                    }
                    else {
                        g.setColor(Color.MAGENTA);
                    }
                }
                else {
                    g.setColor(Color.BLUE);
                }
            }
            g.fill(new Rectangle2D.Double(getWidth() * (column + X_OFFSET) / X_DIVISION,
					                      getHeight() * (row + Y_OFFSET) / Y_DIVISION,
					                      getWidth() / (X_DIVISION + 1),
					                      getHeight() / (Y_DIVISION + 2)));
        }
    }
}

