import org.firmata4j.IOEvent;
import org.firmata4j.firmata.*;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.IODeviceEventListener;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.IOException;

public class SpaceInvaderGame extends JPanel implements ActionListener, KeyListener, IODeviceEventListener {

    //board
    int tileSize = 32;
    int rows = 16;
    int columns = 16;

    int boardWidth = tileSize * columns; // 32 * 16
    int boardHeight = tileSize * rows; // 32 * 16

    float maxDistanceX = 40.0f;
    float refDistance = 1.f;
    Image shipImg, alienImg, alienCyanImg, alienMagentaImg, alienYellowImg;

    ArrayList<Image> alienImgArray;

    //SOUND
    SoundModule soundModule;

    //HAPTICS
    VibrationModule vibrationModule;

    //ship
    int shipWidth = tileSize*2;
    int shipHeight = tileSize;
    int shipX = tileSize * columns/2 - tileSize;
    int shipY = tileSize * rows - tileSize*2;
    int shipVelocityX = tileSize; //ship moving speed
    Block ship;

    //aliens
    ArrayList<Block> alienArray;
    int alienWidth = tileSize*2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = tileSize;

    int alienRows = 1;
    int alienColumns = 1;
    int alienCount = 0; //number of aliens to defeat
    int alienVelocityX = 1; //alien moving speed

    //bullets
    ArrayList<Block> bulletArray;
    int bulletWidth = tileSize/8;
    int bulletHeight = tileSize/2;
    int bulletVelocityY = -10; //bullet moving speed

    Timer gameLoop;
    boolean gameOver = false;
    int score = 0;

    private final String myPort = "/dev/cu.SLAB_USBtoUART"; // MODIFY THIS for your own computer & setup.
    private final IODevice myGroveBoard = new FirmataDevice(myPort); // using the name of a port
    private final int THEPOT = 14;
    private final int THEBUT = 6;
    private Pin thePot;
    private Pin theBut;

    SpaceInvaderGame() {

        initGroveBoard(); //init the grove

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setFocusable(true);
//        addKeyListener(this);

        //load images
        shipImg = new ImageIcon(getClass().getResource("./ship.png")).getImage();
        alienImg = new ImageIcon(getClass().getResource("./alien.png")).getImage();
        alienCyanImg = new ImageIcon(getClass().getResource("./alien-cyan.png")).getImage();
        alienMagentaImg = new ImageIcon(getClass().getResource("./alien-magenta.png")).getImage();
        alienYellowImg = new ImageIcon(getClass().getResource("./alien-yellow.png")).getImage();

        alienImgArray = new ArrayList<Image>();
        alienImgArray.add(alienImg);
        alienImgArray.add(alienCyanImg);
        alienImgArray.add(alienMagentaImg);
        alienImgArray.add(alienYellowImg);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);
        alienArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();

        //sounds
        soundModule = new SoundModule();
        soundModule.playShipSound();

        //vibrations
        vibrationModule= new VibrationModule(myGroveBoard);

        //listen for arduino events
        this.myGroveBoard.addEventListener(this);

        //game timer
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        createAliens();
        gameLoop.start();
    }

    /**
     * Initialize the Grove Board
     */
    public void initGroveBoard() {
        // try to communicate with the board
        try {
            myGroveBoard.start(); // start communication with board;
            myGroveBoard.ensureInitializationIsDone();
            System.out.println("Board started."); //hopefully we make it here.
        } catch (Exception ex) { // if not, detail the error.
            System.out.println("couldn't connect to board.");
            return; //no point continuing at this point.
        }

        this.thePot = myGroveBoard.getPin(THEPOT);
        this.theBut = myGroveBoard.getPin(THEBUT);
        try {
            this.thePot.setMode(Pin.Mode.ANALOG);
            this.theBut.setMode(Pin.Mode.INPUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stop the grove board
     */
    public void stopGroveBoard() {
        try {
            this.myGroveBoard.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //ship
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        //aliens
        for (int i = 0; i < alienArray.size(); i++) {
            Block alien = alienArray.get(i);
            if (alien.alive) {
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        //bullets
        g.setColor(Color.white);
        for (int i = 0; i < bulletArray.size(); i++) {
            Block bullet = bulletArray.get(i);
            if (!bullet.used) {
                g.drawRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        //score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {

        //alien
        for (int i = 0; i < alienArray.size(); i++) { //this will always be 1 for now
            Block alien = alienArray.get(i);
            if (alien.alive) {
                alien.x += alienVelocityX;

                //if alien touches the borders
                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienVelocityX *= -1;
                    alien.x += alienVelocityX*2;

                    //move all aliens up by one row
                    for (int j = 0; j < alienArray.size(); j++) {
                        alienArray.get(j).y += alienHeight;
                    }
                }

                //at this point we know where the ship is
                float x1 = (float)-1*(alien.x-boardWidth)/(float)boardWidth;
                float x2 = (float)-1*(ship.x-boardWidth)/(float)boardWidth;
                float x = (x2-x1)*maxDistanceX;

                float y1 = (float)-1*(alien.y-boardHeight)/(float)boardHeight;
                float y2 = (float)-1*(ship.y-boardHeight)/(float)boardHeight;
                float y = (y1-y2)*maxDistanceX;

                float[] pos1 = {x, y, refDistance}; // offset z to avoid artifacts
                soundModule.playShipSound(pos1);
                vibrationModule.setvibrationInterval((long)((x2-x1)* vibrationModule.maxInterval));
                vibrationModule.vibrate();

                if (alien.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        //bullets
        for (int i = 0; i < bulletArray.size(); i++) {
            Block bullet = bulletArray.get(i);
            bullet.y += bulletVelocityY;

            //bullet collision with aliens
            for (int j = 0; j < alienArray.size(); j++) {
                Block alien = alienArray.get(j);
                if (!bullet.used && alien.alive && detectCollision(bullet, alien)) {
                    float x = (float)-1*(alien.x-boardWidth)/(float)boardWidth;
                    x = x*maxDistanceX;
                    float y = (float)-1*(alien.y-boardHeight)/(float)boardHeight;
                    y = y*maxDistanceX;
                    System.out.println("SHOT FIRED x: " + x + " y: " + y);
                    float []pos = {x, y, refDistance};
                    soundModule.playBulletSound(pos);

                    bullet.used = true;
                    alien.alive = false;
                    alienCount--;
                    score += 100;
                }
            }
        }

        //clear bullets
        while (bulletArray.size() > 0 && (bulletArray.get(0).used || bulletArray.get(0).y < 0)) {
            bulletArray.remove(0); //removes the first element of the array
        }

        //next level
        if (alienCount == 0) {
            //increase the number of aliens in columns and rows by 1
            score += alienColumns * alienRows * 100;
            alienArray.clear();
            bulletArray.clear();
            createAliens();
        }
    }

    public void createAliens() {
        Random random = new Random();
        for (int c = 0; c < alienColumns; c++) {
            for (int r = 0; r < alienRows; r++) {
                int randomImgIndex = random.nextInt(alienImgArray.size());
                Block alien = new Block(
                        alienX + c*alienWidth,
                        alienY + r*alienHeight,
                        alienWidth,
                        alienHeight,
                        alienImgArray.get(randomImgIndex)
                );
                alienArray.add(alien);
            }
        }
        alienCount = alienArray.size();
    }


    public boolean detectCollision(Block a, Block b) {
        return  a.x < b.x + b.width &&  //a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&  //a's top right corner passes b's top left corner
                a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;   //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) { //any key to restart
            ship.x = shipX;
            bulletArray.clear();
            alienArray.clear();
            gameOver = false;
            score = 0;
            alienColumns = 1;
            alienRows = 1;
            alienVelocityX = 1;
            createAliens();
            gameLoop.start();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT  && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX; //move left one tile
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT  && ship.x + shipVelocityX + ship.width <= boardWidth) {
            ship.x += shipVelocityX; //move right one tile
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            //shoot bullet
            soundModule.playBulletSound();
            Block bullet = new Block(ship.x + shipWidth*15/32, ship.y, bulletWidth, bulletHeight, null);
            bulletArray.add(bullet);
        }

    }

    @Override
    public void onStart(IOEvent ioEvent) {

    }

    @Override
    public void onStop(IOEvent ioEvent) {

    }

    @Override
    public void onPinChange(IOEvent ioEvent) {
//        System.out.println("Pin change event: " + ioEvent.getPin().getIndex() + " value: " + ioEvent.getPin().getValue());

        // 1. Check if the event is from the potentiometer pin
        if (ioEvent.getPin().getIndex() != THEPOT && ioEvent.getPin().getIndex() != THEBUT) {
            return; // Ignore if not from potentiometer
        }

        if (ioEvent.getPin().getIndex() == THEPOT) {
            // 2. Get the potentiometer value (0 to 1023)
            long potValue = ioEvent.getPin().getValue();

            // 3. Map the value from 0-1023 to 0-WINDOW_HEIGHT
            int mappedValue = (int) (potValue * (boardWidth - shipWidth) / 1023.0);

            // 4. Assign the mapped value to paddlePosition
            ship.x = mappedValue;
        } else if (ioEvent.getPin().getIndex() == THEBUT) {
            //shoot bullet
            soundModule.playBulletSound();
            Block bullet = new Block(ship.x + shipWidth*15/32, ship.y, bulletWidth, bulletHeight, null);
            bulletArray.add(bullet);
        }
    }

    @Override
    public void onMessageReceive(IOEvent ioEvent, String s) {

    }
}
