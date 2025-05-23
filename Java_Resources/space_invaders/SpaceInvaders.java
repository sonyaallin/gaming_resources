import java.awt.*;
import java.awt.event.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

import com.jogamp.openal.*;
import com.jogamp.openal.util.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    //board
    int tileSize = 32;
    int rows = 16;
    int columns = 16;

    int boardWidth = tileSize * columns; // 32 * 16
    int boardHeight = tileSize * rows; // 32 * 16

    float maxDistanceX = 40.0f;
    float refDistance = 1.f;
    Image shipImg;
    Image alienImg;
    Image alienCyanImg;
    Image alienMagentaImg;
    Image alienYellowImg;
    ArrayList<Image> alienImgArray;

    //SOUND
    static final int NUM_BUFFERS = 3;
    static final int NUM_SOURCES = 3;

    static final int SHIP = 0;
    static final int BULLET = 1;
    static final int EXPLOSION = 2;

    static int[] buffers = new int[NUM_BUFFERS];
    static int[] sources = new int[NUM_SOURCES];

    static float[][] sourcePos = new float[NUM_SOURCES][3];
    static float[][] sourceVel = new float[NUM_SOURCES][3];
    static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
    static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
    static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

    static AL al;
    static ALC alc;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true; //used for aliens
        boolean used = false; //used for bullets
        
        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

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

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

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

        //initialize sound
        try {
            ALut.alutInit();
            al = ALFactory.getAL();
        } catch (ALException e) {
            e.printStackTrace();
            return;
        }

        if(loadALData() == AL.AL_FALSE) {
            System.exit(1);
        };

        setListenerValues();
        al.alSourcePlay(sources[SHIP]);

        //game timer
        gameLoop = new Timer(1000/60, this); //1000/60 = 16.6
        createAliens();
        gameLoop.start();
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
                // g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
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

//        System.out.println(alienArray.get(0).x + " " + alienArray.get(0).y);

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
                al.alSourcefv(sources[SHIP], AL.AL_POSITION, pos1, 0);
//                System.out.println("x: " + x + " y: " + y);

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
                    al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, pos, 0);
                    al.alSourcePlay(sources[EXPLOSION]);
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
            score += alienColumns * alienRows * 100; //bonus points :)
//            alienColumns = Math.min(alienColumns + 1, columns/2 -2); //cap at 16/2 -2 = 6
//            alienRows = Math.min(alienRows + 1, rows-6);  //cap at 16-6 = 10
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
            al.alSourcePlay(sources[BULLET]);
            Block bullet = new Block(ship.x + shipWidth*15/32, ship.y, bulletWidth, bulletHeight, null);
            bulletArray.add(bullet);

        }
    }



    static int loadALData() {
        //variables to load into
        int[] format = new int[1];
        int[] size = new int[1];
        ByteBuffer[] data = new ByteBuffer[1];
        int[] freq = new int[1];
        int[] loop = new int[1];

        // load wav data into buffers
        al.alGenBuffers(NUM_BUFFERS, buffers, 0);
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        ALut.alutLoadWAVFile(
                SpaceInvaders.class.getClassLoader().getResourceAsStream("fastinvader1.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[SHIP],
                format[0],
                data[0],
                size[0],
                freq[0]);

        ALut.alutLoadWAVFile(
                SpaceInvaders.class.getClassLoader().getResourceAsStream("shot.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[BULLET],
                format[0],
                data[0],
                size[0],
                freq[0]);

        ALut.alutLoadWAVFile(
                SpaceInvaders.class.getClassLoader().getResourceAsStream("explosion.wav"),
                format,
                data,
                size,
                freq,
                loop);
        al.alBufferData(buffers[EXPLOSION],
                format[0],
                data[0],
                size[0],
                freq[0]);

        // bind buffers into audio sources
        al.alGenSources(NUM_SOURCES, sources, 0);

        al.alSourcei(sources[SHIP], AL.AL_BUFFER, buffers[SHIP]);
        al.alSourcef(sources[SHIP], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[SHIP], AL.AL_GAIN, 1.0f);
        al.alSourcefv(sources[SHIP], AL.AL_POSITION, sourcePos[SHIP], 0);
        al.alSourcefv(sources[SHIP], AL.AL_POSITION, sourceVel[SHIP], 0);
        al.alSourcei(sources[SHIP], AL.AL_LOOPING, AL.AL_TRUE);

        al.alSourcei(sources[BULLET], AL.AL_BUFFER, buffers[BULLET]);
        al.alSourcef(sources[BULLET], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[BULLET], AL.AL_GAIN, 0.25f);
        al.alSourcefv(sources[BULLET], AL.AL_POSITION, sourcePos[BULLET], 0);
        al.alSourcefv(sources[BULLET], AL.AL_POSITION, sourceVel[BULLET], 0);
        al.alSourcei(sources[BULLET], AL.AL_LOOPING, AL.AL_FALSE);

        al.alSourcei(sources[EXPLOSION], AL.AL_BUFFER, buffers[EXPLOSION]);
        al.alSourcef(sources[EXPLOSION], AL.AL_PITCH, 1.0f);
        al.alSourcef(sources[EXPLOSION], AL.AL_GAIN, 1.00f);
        al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, sourcePos[EXPLOSION], 0);
        al.alSourcefv(sources[EXPLOSION], AL.AL_POSITION, sourceVel[EXPLOSION], 0);
        al.alSourcei(sources[EXPLOSION], AL.AL_LOOPING, AL.AL_FALSE);

        // do another error check and return
        if (al.alGetError() != AL.AL_NO_ERROR) {
            return AL.AL_FALSE;
        }

        return AL.AL_TRUE;
    }

    static void setListenerValues() {
        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
    }
}
