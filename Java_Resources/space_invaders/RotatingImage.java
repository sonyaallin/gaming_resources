import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RotatingImage extends JFrame {
    private ImagePanel topPanel;
    private JPanel bottomPanel;

    private SoundModuleAdventure soundModuleAdventure;

    public RotatingImage() {

        soundModuleAdventure = new SoundModuleAdventure();

        setTitle("Adventure Game Controls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Top: rotating image panel
        topPanel = new ImagePanel("compass.jpg"); // Replace with your image path
        add(topPanel, BorderLayout.CENTER);

        // Bottom: clickable image panel
        bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(createClickableImage("left.png", true));
        bottomPanel.add(createClickableImage("right.png", false));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private BufferedImage getScaledImage(BufferedImage src, double percent) {
        double width = src.getWidth()*percent;
        double height = src.getHeight()*percent;
        BufferedImage resized = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, (int)width, (int)height, null);
        g2.dispose();
        return resized;
    }

    private JLabel createClickableImage(String imagePath, boolean side) {
        JLabel label = new JLabel();
        try {
            BufferedImage img = ImageIO.read(getClass().getResource(imagePath));
            BufferedImage scaledImg = getScaledImage(img, 0.5);
            label.setIcon(new ImageIcon(scaledImg));
        } catch (IOException | IllegalArgumentException e) {
            label.setText("Image not found");
            System.err.println("Failed to load image: " + imagePath);
        }

        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (side) soundModuleAdventure.playLeft();
                else soundModuleAdventure.playRight();
            }
        });

        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RotatingImage app = new RotatingImage();
            app.setVisible(true);
        });
    }

    // Custom panel with rotating image on mouse drag
    class ImagePanel extends JPanel {
        private BufferedImage image;
        private double angle = 0.0;
        private Point center;
        private double initialAngle = 0;
        private boolean dragging = false;

        public ImagePanel(String imagePath) {
            setBackground(Color.WHITE);
            try {
                image = ImageIO.read(getClass().getResource(imagePath));
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Image load failed: " + e.getMessage());
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (image == null) return;
                    center = new Point(getWidth() / 2, getHeight() / 2);
                    initialAngle = getAngle(center, e.getPoint());
                    dragging = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!dragging || image == null) return;
                    double currentAngle = getAngle(center, e.getPoint());
                    double delta = currentAngle - initialAngle;
                    angle += delta;
                    initialAngle = currentAngle;
                    System.out.println(angle);
                    soundModuleAdventure.playMeow(angle);
                    repaint();
                }
            });
        }

        private double getAngle(Point center, Point target) {
            double dx = target.x - center.x;
            double dy = target.y - center.y;
            return Math.atan2(dy, dx);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int imgW = image.getWidth();
                int imgH = image.getHeight();

                AffineTransform at = new AffineTransform();
                at.translate(cx, cy);
                at.rotate(angle);
                at.translate(-imgW / 2, -imgH / 2);

                g2d.drawImage(image, at, null);
                g2d.dispose();
            }
        }
    }
}