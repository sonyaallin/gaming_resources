import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private JLabel statusLabel;

    public GamePanel(Player player, GameMap gameMap) {
        this.map = gameMap;
        this.player = player;
        this.statusLabel = new JLabel("");
        setPreferredSize(new Dimension(400, 400));  // Set width to 400px, height to 400px(400, 400);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Get the current width and height of the GamePanel
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        System.out.println(panelWidth);
        System.out.println(panelHeight);
        System.out.println(map.tileSize);

        // Determine the size of each tile based on the panel's dimensions
        int tileSizeX = panelWidth / map.tileSize;  // Horizontal tile size
        int tileSizeY = panelHeight / map.tileSize; // Vertical tile size

        // Use the smaller of the two dimensions to keep the map square
        //int tileSize = Math.min(tileSizeX, tileSizeY);

        // Render the map
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                char type = map.tiles[y][x].type;
                Color color = getTileColor(type);
                g.setColor(color);
                g.fillRect(x * tileSizeX, y * tileSizeY, tileSizeX, tileSizeY); // Draw each tile
                g.setColor(Color.BLACK);
                g.drawRect(x * tileSizeX, y * tileSizeY, tileSizeX, tileSizeY); // Draw each tile
            }
        }

        // Draw player (scaled to the tile size)
        int playerX = player.x * tileSizeX / 10; // Convert fine-grained position to tile scale
        int playerY = player.y * tileSizeY / 10;
        g.setColor(Color.BLUE);
        g.fillOval(playerX, playerY, tileSizeX * 3 / 10, tileSizeY * 3 / 10); // Draw player as a circle

        // Update status label in the bottom panel
        updateStatusLabel();
    }

    // Update the status label with player's current location
    private void updateStatusLabel() {
        int tileX = player.getTileX(10); // Get the tile X (in terms of tiles)
        int tileY = player.getTileY(10); // Get the tile Y (in terms of tiles)
        statusLabel.setText("Player Location: (" + tileX + ", " + tileY + ")");
    }

    public Color getTileColor(char C) {
        Color color = switch (C) {
            case '1' -> Color.GREEN;
            case '2' -> Color.ORANGE;
            case '3' -> Color.MAGENTA;
            case '4' -> Color.BLUE;
            default -> Color.BLACK;
        };
        return color;
    }

}
