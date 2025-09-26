import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameMap {
    public Tile[][] tiles;
    public int width, height;
    public int tileSize;

    public GameMap(String filename, int tileSize) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        height = lines.size();
        width = lines.get(0).length();
        tiles = new Tile[height][width];

        for (int y = 0; y < height; y++) {
            char[] row = lines.get(y).toCharArray();
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(row[x]);
            }
        }

        this.tileSize = tileSize;
    }
}