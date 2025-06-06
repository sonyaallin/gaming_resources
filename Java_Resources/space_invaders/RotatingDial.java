import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

import java.nio.ByteBuffer;

public class RotatingDial extends JFrame {

    private static double w, h;
    int i = 0;
    private JPanel compassPanel, footPanel;

    public RotatingDial() {
        w = 600;
        h = 600;
        setTitle("Navigation Test");
        compassPanel = new JPanel(); // main panel
        compassPanel.setLayout(new GridLayout(2, 1));
        compassPanel.add(new JLabel("Compass", SwingConstants.CENTER));
        compassPanel.setBackground(Color.white);
        compassPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        footPanel = new JPanel(); // sub-panel 1
        footPanel.add(new JLabel("Feet", SwingConstants.CENTER));
        footPanel.setBackground(Color.white);

        compassPanel.add(footPanel);

        setSize((int)w,(int)h);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }
    public static void main(String args[]) {
        new RotatingDial();
    }

    public void paintComponent(Graphics g) {

        BufferedImage compass = LoadImage("compass.jpg");
        double tx = (w - compass.getWidth())/2;
        double ty = (h - compass.getHeight())/2;

        AffineTransform at = AffineTransform.getTranslateInstance(tx,ty);
        at.rotate(Math.toRadians(i++), compass.getWidth()/2, compass.getHeight()/2);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(compass, at, null);

        repaint();

    }

    BufferedImage LoadImage(String fname) {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File(fname));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return img;

    }
}


