import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Player {
    private final HashMap<Integer,Room> rooms;
    private Room currentRoom; //The current room that the player is located in.
    public ArrayList<AdventureObject> inventory; //The list of items that the player is carrying at the moment.
    public int x, y;
    public String currentDirection;
    public int lastTileX, lastTileY;
    public char currentTileType;

    /**
     * Player Constructor
     * __________________________
     * Initializes attributes
     **/
    public Player(HashMap<Integer, Room> rooms, GameMap g, int startX, int startY, int tileSize) {
        this.inventory = new ArrayList<AdventureObject>();
        this.x = startX * tileSize;
        this.y = startY * tileSize;
        this.lastTileX = startX;
        this.lastTileY = startY;
        this.currentTileType = g.tiles[getTileX(tileSize)][getTileY(tileSize)].type;
        this.rooms = rooms;
        this.currentRoom = rooms.get(Character.getNumericValue(this.currentTileType));
        this.currentDirection = "N";
    }

    public void setCurrentDirection(String currentDirection) {
        this.currentDirection = currentDirection;
    }

    public int move(int dx, int dy, GameMap map) {
        System.out.println("moving");
        int retVal = 0;
        int newX = x + dx;
        int newY = y + dy;

        int maxX = map.width * map.tileSize;
        int maxY = map.height * map.tileSize;

        if (newX >= 0 && newX < maxX && newY >= 0 && newY < maxY) {
            retVal = 1;
            x = newX;
            y = newY;

            int tileX = x / map.tileSize;
            int tileY = y / map.tileSize;

            // Check for region change
            if (tileX != lastTileX || tileY != lastTileY) {
                char newTileType = map.tiles[tileY][tileX].type;
                if (newTileType != currentTileType) {
                    System.out.printf("Region changed: %c -> %c%n", currentTileType, newTileType);
                    currentTileType = newTileType;
                    this.currentRoom = this.rooms.get(Character.getNumericValue(this.currentTileType));
                    retVal = 2;
                }
                lastTileX = tileX;
                lastTileY = tileY;
            }

            return retVal;
        }
        return retVal;
    }

    public int getTileX(int tileSize) { return x / tileSize; }
    public int getTileY(int tileSize) { return y / tileSize; }

    /**
     * takeObject
     * _________________________
     * This method adds an object to a player's inventory (and removes it from the room)
     * if the object is present in the room.  It then returns true.
     * If the object is not present in the room, the method
     * returns false.
     *
     * @param object name of the object to take
     * @return true if object is taken, false otherwise
     */
    public boolean takeObject(String object){
        boolean found = false;
        int i = 0;
        while (i < this.currentRoom.objectsInRoom.size() && ! found) {
            if (this.currentRoom.objectsInRoom.get(i).getName().equals(object)) {
                found = true;
                this.inventory.add(this.currentRoom.objectsInRoom.get(i));
            }
        }
        return found;
    }

    /**
     * dropObject
     * _________________________
     * This method removes an object from the inventory of the player, if it exists.
     * The object, once dropped, should be added to the current room.
     * If the object is not in the inventory, this method will do nothing.
     *
     * @param s String name of prop or object to be removed to the inventory.
     */
    public void dropObject(String s) {
        boolean found = false;
        int i = 0;

        while (!found && i < this.inventory.size()) {
            if (this.inventory.get(i).getName().equals(s)) {
                found = true;
                AdventureObject object = this.inventory.get(i);
                this.inventory.remove(object);
                this.currentRoom.addObject(object);
            }
        }
    }

    /**
     * setCurrentRoom
     * _________________________
     * Setter method for the current room attribute.
     *
     * @param currentRoom The location of the player in the game.
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * getCurrentRoom
     * _________________________
     * Getter method for the current room attribute.
     *
     * @return current room the player is in.
     */
    public Room getCurrentRoom() {
        return this.currentRoom;
    }

}
