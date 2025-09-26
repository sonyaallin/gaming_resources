import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import java.io.*;
import javax.sound.sampled.*;

public class Room {

    private int roomNumber; //The number of the room.
    private String roomName; //The name of the room.
    private String roomDescription; //The description of the room.
    private PassageTable passageTable = new PassageTable(); //The passage table for the room.
    private boolean isVisited; //A boolean to store if the room has been visited or not
    public ArrayList<AdventureObject> objectsInRoom = new ArrayList<AdventureObject>(); //The list of objects in the room.

    /**
     * Room constructor.
     * __________________________
     * @param roomName The name of the room.
     * @param roomNumber The number of the room.
     * @param roomDescription The description of the room.
     * @param adventureName The name of the adventure, which corresponds to the name of the folder in
     *                      which audio descriptions of rooms can be found.
     */
    public Room(String roomName, int roomNumber, String roomDescription, String adventureName){
        this.roomName = roomName;
        this.roomNumber = roomNumber;
        this.roomDescription = roomDescription;
        this.isVisited = false;
    }

    /**
     * readRoom
     * __________________________
     * Read a single room from the BufferedReader.
     *
     * @param buff the BufferedReader pointing to the rooms file
     * @param adventureName the name of the adventure.  This corresponds to the
     *                      name of the folder in which the adventure assets are located.
     *                      Each room has audio files in this folder! You will need this information
     *                      to use the Room constructor.
     *
     * @return the room object that has been read
     * @throws IOException if a file I/O error occurs
     */
    public static Room readRoom(String adventureName, BufferedReader buff) throws IOException {
        String room_line = buff.readLine();
        try {
            Integer.parseInt(room_line);
        } catch(NumberFormatException e){
            throw new RuntimeException("NOT A VALID ROOM NUMBER");
        }
        int room_number = Integer.parseInt(room_line);
        String room_name = buff.readLine();

        room_line = buff.readLine();
        String room_description = room_line;
        room_line = buff.readLine();

        int counter = 0;
        while (! (room_line.equals("-----"))) {
            counter += 1;
            if (counter > 10) {
                throw new RuntimeException("DESCRIPTION OF ROOM IS TOO LONG");
            }
            room_description += "\n" + room_line;
            room_line = buff.readLine();
        }
        room_line = buff.readLine();
        PassageTable passagetable = new PassageTable();

        while (! room_line.equals("") && buff.ready()) {
            String[] pass = room_line.split("\\s+");

            String direction = pass[0];
            String dest_num = pass[1];

            if (pass[1].contains("/")) {
                String[] dest_key = pass[1].split("/");
                dest_num = dest_key[0];

                try {
                    Integer.parseInt(dest_num);
                } catch(NumberFormatException e){
                    throw new RuntimeException("NOT A VALID ROOM NUMBER");

                }
                String key = dest_key[1];
                Passage passage = new Passage(direction, dest_num, key);
                passagetable.addDirection(passage);
            }
            else {
                try {
                    Integer.parseInt(dest_num);
                } catch(NumberFormatException e){
                    throw new RuntimeException("NOT A VALID ROOM NUMBER");

                }
                Passage passage = new Passage(direction, dest_num);
                passagetable.addDirection(passage);

            }
            room_line = buff.readLine();
        }
        Room room = new Room(room_name, room_number, room_description, adventureName);
        room.passageTable = passagetable;
        return room;
    }

    /**
     * addObject
     * __________________________
     * This method adds a game object to the room.
     *
     * @param object to be added to the room.
     */
    public void addObject(AdventureObject object){
        this.objectsInRoom.add(object);
    }

    /**
     * removeObject
     * __________________________
     * This method removes a game object from the room.
     *
     * @param object to be removed from the room.
     */
    public void removeObject(AdventureObject object){
        this.objectsInRoom.remove(object);
    }

    /**
     * setVisited
     * __________________________
     * This method sets the isVisited attribute to true.
     */
    public void setVisited(){
        this.isVisited = true;
    }

    /**
     * getVisited
     * __________________________
     * Getter for isVisited attribute.
     */
    public boolean getVisited(){
        return this.isVisited;
    }

    /**
     * getRoomName
     * __________________________
     * Getter method for the name attribute.
     *
     * @return name of the room
     */
    public String getRoomName(){
        return this.roomName;
    }

    /**
     * getRoomNumber
     * __________________________
     * Getter method for the number attribute.
     *
     * @return number of the room
     */
    public int getRoomNumber(){
        return this.roomNumber;
    }

    /**
     * getDescription
     * __________________________
     *
     * @return long description of the room if not visited, else short description.
     */
    public String getDescription(){
        if (!this.isVisited) return this.roomDescription;
        else return "You are currently in the room named " + this.roomName +".";
    }

    /**
     * getPassageTable
     * __________________________
     * Getter method for the PassageTable.
     *
     * @return passage table of the room
     */
    public PassageTable getPassageTable(){
        return this.passageTable;
    }

    /**
     * hasObjects
     * __________________________
     * Determines if the room has objects
     *
     * @return true if the room contains objects, else false
     */
    public boolean hasObjects() {
        if (this.objectsInRoom.size() > 0) return true;
        else return false;
    }

    /**
     * printObjects
     * __________________________
     * Pretty print the names of objects in the room.
     */
    public void printObjects() {
        for (AdventureObject o: this.objectsInRoom)
            System.out.println(o.getDescription());
    }

}
