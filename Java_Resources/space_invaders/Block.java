import java.awt.*;

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