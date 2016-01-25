import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.event.*;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

// JComponent is a base class for custom components 
public class Breakout extends JComponent {

    static int BALL_RADIUS = 5;
    static int BALL_SPEED = 5;
    static int WIDTH = 1000;
    static int HEIGHT = 600; 
    static int X_DIVISION = 14;
    static int Y_DIVISION = 20;
    static int X_OFFSET = 2;
    static int Y_OFFSET = 2;
    static int PADDLE_WIDTH = 50;
    static int PADDLE_HEIGHT = 7;
    static int PADDLE_SPEED = 5;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() {	
                Breakout canvas = new Breakout();
                JFrame f = new JFrame("Breakout"); // jframe is the app window
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(WIDTH, HEIGHT); // window size
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
	        // Left arrow
            if (e.getKeyCode() == 37) {
    	    	paddle.setSpeed(-PADDLE_SPEED);
	        }
            // Right arrow
	        if (e.getKeyCode() == 39) {
		        paddle.setSpeed(PADDLE_SPEED);
	        }
	    }
	    public void keyReleased(KeyEvent e) {
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
            // Enter key
            if (e.getKeyCode() == 10) {
                // If the game hasn't been started, start it.
                if (!started) {
                    started = true;
                    active = true;
                }
            }
	    }
    }

    // Object properties
    private Ball ball;
    private BrickList brick_list;
    private Paddle paddle;
    boolean active;
    boolean started;

    // Constructor for Breakout
    public Breakout() {
        ball = new Ball();
        brick_list = new BrickList(5, 10);
	    paddle = new Paddle();
        active = false;
        started = false;
    }

    // custom graphics drawing 
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
        					RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));

        ball.draw(g2);
        brick_list.draw(g2);
	    paddle.draw(g2);
    }

    public class Test implements Runnable {
        public void run() {
            while (true) {
                ball.move();
		        paddle.move();
                repaint();
                try {
                    Thread.sleep(1000/40);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public class Paddle {
	    private int x_position;
	    private int y_position;
	    private int paddle_speed;

	    public Paddle() {
	        x_position = WIDTH - (HEIGHT / 3);
    	    y_position = HEIGHT - 40;
	        paddle_speed = 0;
	    }

    	public void move() {
            // Only change if in active state.
            if (active) {
	            x_position += paddle_speed;
            }
            else {
                paddle_speed = 0;
            }
	        if (x_position > getWidth() - PADDLE_WIDTH) {
		        x_position = getWidth() - PADDLE_WIDTH;
	        }
	        if (x_position < 0) {
		        x_position = 0;
	        }
            y_position = getHeight() - 40;
	    }

	    public void setSpeed(int speed) {
	        paddle_speed = speed;
	    }

        public boolean intersect(int pos_x, int pos_y){
            if (pos_x >= x_position && pos_x <= x_position + PADDLE_WIDTH) {
                if (pos_y >= y_position - BALL_RADIUS) {
                    return true;
                }
            }
            return false;
        }

        public void draw(Graphics2D g) {
	        g.setColor(Color.WHITE);
            g.fill(new Rectangle2D.Double(x_position, y_position, PADDLE_WIDTH, PADDLE_HEIGHT));
	    }
    }

    public class Ball {
        private int position_x;
        private int position_y;
        private int direction_x;
        private int direction_y;

        public Ball() {
            position_x = WIDTH - 20;
            position_y = 2 * HEIGHT / 3;
            direction_x = -BALL_SPEED;
            direction_y = BALL_SPEED;
        }

        public void move() {
            // Only change if in active state
            if (active) {
                position_x += direction_x;
                position_y += direction_y;
            }

            Brick[][] bricks = brick_list.getBricks();
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    if (bricks[a][b] != null) {
			            if (bricks[a][b].intersectHorizontal(position_x, position_y)) {
			                direction_y *= -1;
			                bricks[a][b] = null;
		    	            return;
			            }
	    	        }
		        }
	        }
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    if (bricks[a][b] != null) {
			            if (bricks[a][b].intersectVertical(position_x, position_y)) {
			                direction_x *= -1;
			                bricks[a][b] = null;
		 	                return;
			            }
		            }
		        }
	        }
            for (int a = 0; a < bricks.length; ++a) {
                for (int b = 0; b < bricks[a].length; ++b) {
                    if (bricks[a][b] != null) {
			            if (bricks[a][b].intersectCorner(position_x, position_y)) {
			                direction_x *= -1;
			                direction_y *= -1;
			                bricks[a][b] = null;
			                return;
			            }
		            }
		        }
	        }
            if (position_x < BALL_RADIUS) {
                position_x = BALL_RADIUS;
                direction_x *= -1;
            }
            if (position_x > getWidth() - BALL_RADIUS) {
                position_x = getWidth() - BALL_RADIUS;
                direction_x *= -1;
            }
            if (position_y < BALL_RADIUS) {
                position_y = BALL_RADIUS;
                direction_y *= -1;
            }
            if (position_y > getHeight() - BALL_RADIUS) {
                position_y = getHeight() - BALL_RADIUS;
                direction_y *= -1;
            }

            if (paddle.intersect(position_x, position_y)) {
                direction_y *= -1;
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

        public BrickList(int r, int c) {
            bricks = new Brick[r][c];
            for (int a = 0; a < r; ++a) {
                for (int b = 0; b < c; ++b) {
                    bricks[a][b] = new Brick(a, b);
                }
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

        public Brick(int r, int c) {
            row = r;
            column = c;
        }

	    public boolean intersectHorizontal(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
            if (position_x >= left_x_pixel && position_x <= right_x_pixel) {
		        if (position_y >= top_y_pixel - BALL_RADIUS && position_y <= bottom_y_pixel + BALL_RADIUS) {
		            return true;
		        }
	        }
	        return false;
	    }

	    public boolean intersectVertical(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
            if (position_y >= top_y_pixel && position_y <= bottom_y_pixel) {
		        if (position_x >= left_x_pixel - BALL_RADIUS && position_x <= right_x_pixel + BALL_RADIUS) {
		            return true;
		        }
	        }
	        return false;
	    }

	    public boolean intersectCorner(int position_x, int position_y) {
	        int left_x_pixel = getWidth() * (column + X_OFFSET) / X_DIVISION;
            int right_x_pixel = left_x_pixel + (getWidth() / (X_DIVISION + 1));
            int top_y_pixel = getHeight() * (row + Y_OFFSET) / Y_DIVISION;
	        int bottom_y_pixel = top_y_pixel + getHeight() / (Y_DIVISION + 2);
	        if (Math.sqrt(Math.pow(position_x - left_x_pixel, 2) + Math.pow(position_y - top_y_pixel, 2)) < BALL_RADIUS) {
		        return true;
	        }
	        if (Math.sqrt(Math.pow(position_x - left_x_pixel, 2) + Math.pow(position_y - bottom_y_pixel, 2)) < BALL_RADIUS) {
		        return true;
	        }
	        if (Math.sqrt(Math.pow(position_x - right_x_pixel, 2) + Math.pow(position_y - top_y_pixel, 2)) < BALL_RADIUS) {
		        return true;
	        }
	        if (Math.sqrt(Math.pow(position_x - right_x_pixel, 2) + Math.pow(position_y - bottom_y_pixel, 2)) < BALL_RADIUS) {
		        return true;
	        }
	        return false;
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
            g.fill(new Rectangle2D.Double(getWidth() * (column + X_OFFSET) / X_DIVISION,
					                      getHeight() * (row + Y_OFFSET) / Y_DIVISION,
					                      getWidth() / (X_DIVISION + 1),
					                      getHeight() / (Y_DIVISION + 2)));
        }
    }
}

