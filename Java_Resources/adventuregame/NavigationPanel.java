import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class NavigationPanel extends JPanel {
    private GamePanel mapPanel;
    private ImagePanel topPanel;
    private JPanel bottomPanel;

    private AdventureGame game;
    private String playerDirection;

    public NavigationPanel(AdventureGame game, GamePanel mapPanel) {

        setPreferredSize(new Dimension(400, 400));  // Set width to 400px, height to 400px(400, 400);
        setLayout(new BorderLayout());

        this.mapPanel = mapPanel;

        // Top: rotating image panel
        topPanel = new ImagePanel("assets/compass.jpg", game); // Replace with your image path
        add(topPanel, BorderLayout.CENTER);

        // Bottom: clickable image panel
        bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(createClickableImage("assets/left.png", true));
        bottomPanel.add(createClickableImage("assets/right.png", false));
        add(bottomPanel, BorderLayout.SOUTH);

        this.game = game;
        this.playerDirection = "N";

        System.out.println("Created Navigation Panel");

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
            BufferedImage scaledImg = getScaledImage(img, 0.4);
            label.setIcon(new ImageIcon(scaledImg));
        } catch (IOException | IllegalArgumentException e) {
            label.setText("Image not found");
            System.err.println("Failed to load image: " + imagePath);
        }

        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int dx = 0; int dy = 0;
                if (game.player.currentDirection.equals("N")) dy = -1;
                else if (game.player.currentDirection.equals("S")) dy = 1;
                else if (game.player.currentDirection.equals("E")) dx = -1;
                else if (game.player.currentDirection.equals("W")) dx = 1;
                int moveTry = game.player.move(dx, dy, game.getGameMap());
                if (moveTry > 0) {
                    System.out.println("Player Location: (" + game.player.x + ", " + game.player.y + ")");
                    if (side) {
                        game.soundModuleAdventure.playLeft();
                    } else {
                        game.soundModuleAdventure.playRight();
                    }
                    if (moveTry == 2) {
                        game.setColor();
                        SwingUtilities.invokeLater(() -> {
                            game.textArea.setText(game.player.getCurrentRoom().getDescription());
                            new Thread(() -> {
                                game.soundModuleAdventure.playALData(game.player.getCurrentRoom().getDescription(), 0, "en-US-Standard-A");
                            }).start();
                        });
                    }
                    game.repaint();
                    mapPanel.repaint();
                }
                else game.soundModuleAdventure.playALData("You cannot move in that direction", 0, "en-GB-Standard-O");
            }
        });

        return label;
    }


    // Custom panel with rotating image on mouse drag
    class ImagePanel extends JPanel {
        private BufferedImage image;
        private double angle = 0.0;
        private Point center;
        private double initialAngle = 0;
        private boolean dragging = false;

        public ImagePanel(String imagePath, AdventureGame game) {

            Color color = switch (game.getRoomColor()) {
                case 'G' -> Color.GREEN;
                case 'R' -> Color.GRAY;
                case 'P' -> Color.MAGENTA;
                default -> Color.BLACK;
            };
            setBackground(color);
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

                    String direction = "";
                    double temp = (angle + (2*Math.PI))%(2*Math.PI);
                    if (temp > (Math.PI/4) && temp < (3*Math.PI/4)) { direction = "west"; playerDirection = "W"; }
                    else if (temp > (3*Math.PI/4) && temp < (5*Math.PI/4)) { direction = "south"; playerDirection = "S"; }
                    else if (temp > (5*Math.PI/4) && temp < (7*Math.PI/4)) { direction = "east"; playerDirection = "E"; }
                    else { direction = "north"; playerDirection = "N"; }

                    NavigationPanel.this.game.soundModuleAdventure.playALData("You are currently facing " + direction, 0, "en-GB-Standard-O");
                    NavigationPanel.this.game.player.currentDirection = playerDirection;

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
                int cy = 15*getHeight() / 16;
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