import org.firmata4j.IOEvent;
import org.firmata4j.firmata.*;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.IODeviceEventListener;

import javax.swing.*;

public class GameDriver {
    public static void main(String[] args) throws Exception {


        //window variables
        int tileSize = 32;
        int rows = 16;
        int columns = 16;
        int boardWidth = tileSize * columns; // 32 * 16 = 512px
        int boardHeight = tileSize * rows; // 32 * 16 = 512px

        JFrame frame = new JFrame("Space Invaders");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SpaceInvaderGame spaceInvaders = new SpaceInvaderGame();
        frame.add(spaceInvaders);
        frame.pack();
        spaceInvaders.requestFocus();
        frame.setVisible(true);

    }
}