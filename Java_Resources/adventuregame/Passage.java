
public class Passage {

    private String direction; //The direction of movement in the game.
    private int destinationRoom; //The number of the room that this passage leads to.
    private String keyName; //The name of the object required to move along this passage.
    private boolean isBlocked; //boolean to represent if the passage is blocked.

    /**
     * Passage constructor.
     * ____________________
     *
     * @param direction: A string representation of a direction.
     * @param roomNumber: A string representation of a destination room number.
     */
    public Passage(String direction, String roomNumber) {
        this.direction = direction;
        this.destinationRoom = Integer.parseInt(roomNumber);
        this.keyName = null;
    }

    /**
     * Passage constructor.
     * ____________________
     *
     * @param direction: A string representation of a direction.
     * @param roomNumber: A string representation of a destination room number.
     * @param key: A string representation of a key required to unblock the passage
     */
    public Passage(String direction, String roomNumber, String key) {
        this.direction = direction;
        this.destinationRoom = Integer.parseInt(roomNumber);
        this.keyName = key;
        this.isBlocked = true;
    }

    /**
     * getPassageTable
     * ____________________
     * Returns the direction associated with this motion table entry.
     *
     * @return The direction to move in (e.g. "north", "south", etc.).
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * getDestinationRoom
     * ____________________
     * Returns the number of the room that this motion table entry leads to.
     *
     * @return The number of the destination room.
     */
    public int getDestinationRoom() {
        return this.destinationRoom;
    }

    /**
     * getKeyName
     * ____________________
     * Returns the name of the object required to move along this passage,
     * or null if no object is required.
     *
     * @return The name of the required object, or null if no object is required.
     */
    public String getKeyName() {
        return this.keyName;
    }

    /**
     * getIsBlocked
     * ____________________
     * Returns if the passage is blocked.
     *
     * @return True if the passage is blocked, false otherwise.
     */
    public boolean getIsBlocked() {
        return this.isBlocked;
    }

}
