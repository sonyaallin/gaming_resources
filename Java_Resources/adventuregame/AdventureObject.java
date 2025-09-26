import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * AdventureObject Class.
 * This class keeps track of the props or the objects in the game.
 * These objects have a name, description, and location in the game.
 * The player with the objects can pick or drop them as they like and
 * these objects can be used to pass certain passages in the game.
 * Inspired by assignments created by Eric Roberts
 * and John Estell. Course code tailored by the CSC207
 * instructional team at UTM, with special thanks to:
 *
 * @author anshag01
 * @author mustafassami
 * @author guninkakr03
 *  */
public class AdventureObject {

    private String objectName; //The name of the object.
    private String description; // The description of the object.

    /**
     * AdventureObject Constructor
     * ___________________________
     * This constructor sets the name, description, and location of the object.
     *
     * @param name: The name of the Object in the game.
     * @param description: One line description of the Object.
     * @param location: The location of the Object in the game.
     */
    public AdventureObject(String name, String description, Room location){
        this.objectName = name;
        this.description = description;
    }

    /**
     * readObject
     * __________________________
     * Read a single object from the BufferedReader.
     * Place the object in the Room indicated in the file.
     *
     * @param buff the BufferedReader pointing to the rooms file
     * @param rooms the collection of rooms in the adventure.  Place the object in one of these rooms!
     *
     * @throws IOException if a file I/O error occurs
     */
    public static void readObject(BufferedReader buff, HashMap<Integer, Room> rooms) throws IOException {
        String object_line = buff.readLine();
        String name = object_line;
        object_line = buff.readLine();
        String description = object_line;
        object_line = buff.readLine();
        try {
            Integer.parseInt(object_line);
            rooms.get(Integer.parseInt(object_line));
        } catch (Exception e){
            throw new RuntimeException("THE LOCATION ROOM IS NOT A NUMBER OR THE ROOM DOES NOT EXIST");
        }
        Room room = rooms.get(Integer.parseInt(object_line));
        AdventureObject object = new AdventureObject(name, description, room);
        room.addObject(object);
        buff.readLine();
    }

    /**
     * getName
     * ___________________________
     * Getter method for the name attribute.
     *
     * @return: name of the object
     */
    public String getName(){
        return this.objectName;
    }

    /**
     * getDescription
     * ___________________________
     * Getter method for the description attribute.
     *
     * @return: description of the game
     */
    public String getDescription(){
        return this.description;
    }


}
