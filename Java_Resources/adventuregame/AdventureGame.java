import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class AdventureGame extends JPanel {

    final int TILE_SIZE = 10; // 10x10 player moves per tile
    private String introText; //An attribute to store the Introductory text of the game.
    private String helpText; //A variable to store the Help text of the game. This text is displayed when the user types "HELP" command.
    private GameMap gameMap;
    private HashMap<Integer, Room> rooms; //A list of all the rooms in the game.
    private HashMap<String,String> synonyms = new HashMap<>(); //A HashMap to store synonyms of commands.
    private final String[] actionVerbs = {"QUIT","HELP","LOOK","INVENTORY","TAKE","DROP"}; //List of action verbs (other than motions) that exist in all games. Motion vary depending on the room and game.
    public Player player; //The Player of the game.
    SoundModuleAdventure soundModuleAdventure;
    JTextArea textArea;
    public JLabel statusLabel;

    public AdventureGame() {

        setPreferredSize(new Dimension(400, 400));  // Set width to 400px, height to 400px(400, 400);

        this.synonyms = new HashMap<>();
        this.rooms = new HashMap<>();

        try {
            gameMap = new GameMap("./maps/tinyWorld.txt", TILE_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            setUpGame("story", gameMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Color color = getColor();

        add(new JLabel("Adventure Story"));

        this.textArea = new JTextArea(40,30);
        textArea.setText(this.introText);
        textArea.setBackground(color);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFocusable(false); // Prevent focus
        textArea.setEditable(false); // Optional: makes it read-only
        textArea.getCaret().setVisible(false); // Hide the caret

        // Add padding around the text
        textArea.setBorder(new EmptyBorder(20, 20, 20, 20)); // top, left, bottom, right

        // Put the text area in a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Set layout and add scroll pane
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.WEST);

        soundModuleAdventure = new SoundModuleAdventure();

        soundModuleAdventure.loadALData();
        System.out.println("Loaded Sound Model");

        //begin the story here
        new Thread(() -> {
            soundModuleAdventure.playALData(introText, 0, "en-GB-Standard-N");
            // Update UI on EDT
            SwingUtilities.invokeLater(() -> {
                textArea.setText(this.player.getCurrentRoom().getDescription());
                new Thread(() -> {
                    soundModuleAdventure.playALData(this.player.getCurrentRoom().getDescription(), 0, "en-US-Standard-A");
                }).start();
            });
        }).start();

    }

    public GameMap getGameMap() {
        return this.gameMap;
    }

    public Color getColor() {
        Color color = switch (getRoomColor()) {
            case '1' -> Color.GREEN;
            case '2' -> Color.ORANGE;
            case '3' -> Color.MAGENTA;
            case '4' -> Color.BLUE;
            default -> Color.BLACK;
        };
        return color;
    }
    public void setColor() {
        Color color = getColor();
        this.textArea.setBackground(color);
    }

    /**
     * setUpGame
     */
    public void setUpGame(String directoryName, GameMap gameMap) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(directoryName + File.separator + "synonyms.txt"));
        String synonym_line = br.readLine();
        while (synonym_line != null) {
            java.util.List line_ = List.of(synonym_line.split("="));
            this.synonyms.put((String) line_.get(0), (String) line_.get(1));
            synonym_line = br.readLine();
        }

        BufferedReader br2 = new BufferedReader(new FileReader(directoryName + File.separator + "rooms.txt"));
        Room room = Room.readRoom(directoryName, br2);
        this.rooms.put(1, room);
        int counter = 1;
        while (br2.ready()) {
            counter += 1;
            room = Room.readRoom(directoryName, br2);
            this.rooms.put(counter, room);
        }

        BufferedReader br3 = new BufferedReader(new FileReader(directoryName + File.separator + "introduction.txt"));
        String intro_line = br3.readLine();
        this.introText = "";
        while (! intro_line.equals("************************************************************************")) {
            this.introText += intro_line + " ";
            intro_line = br3.readLine();
        }

        BufferedReader br4 = new BufferedReader(new FileReader(directoryName + File.separator + "help.txt"));
        String help_line = br4.readLine();
        this.helpText = "";
        while (help_line != null) {
            this.helpText += help_line + " ";
            help_line = br4.readLine();
        }

        BufferedReader br5 = new BufferedReader(new FileReader(directoryName + File.separator + "objects.txt"));
        AdventureObject.readObject(br5, this.rooms);

        while (br5.ready()) {
            AdventureObject.readObject(br5, this.rooms);
        }

        this.player = new Player(this.rooms, gameMap, 1, 1, gameMap.tileSize);

    }


    /**
     * playGame
     * __________________________
     * This function is the Game Loop.
     *
     * It keeps track of the player's input and performs actions requested by the player.
     * The game loop ends when the player types "QUIT" or when the player dies.
     */
    public void playGame(){

        String input = "";
        while(true){
            if (!input.equals("LOOK") && !input.equals("L")) { //if the command is to LOOK, printing the description is redundant.
                System.out.println(this.player.getCurrentRoom().getDescription()); //print where we are to the console
                SwingUtilities.invokeLater(() -> {
                    new Thread(() -> {
                        soundModuleAdventure.playALData(this.player.getCurrentRoom().getDescription(), 0, "en-US-Standard-A");
                        if (this.player.getCurrentRoom().hasObjects()) { //are there objects here?
                              //think about this
//                            System.out.println("The following object(s) are here:");
//                            this.player.getCurrentRoom().printObjects();
                        }
                    }).start();
                });
            }
            System.out.print("> "); //prompt for the user
            Scanner scanner = new Scanner(System.in); //read input from the user
            input = scanner.nextLine().toUpperCase(); //convert input to upper case
            //need to get moves from the GUI clicks too
            if (!executeAction(input)) return; //execute the command, if possible.
        }
    }

    public boolean executeAction(String command){
        boolean check = true;
        //first, look up synonyms and convert the user's input string to standard tokens
        String[] inputArray = convertCommand(command);
        if (inputArray[0].equals("QUIT")) {
            System.out.println("GAME OVER");
            check = false;
        }
        else if (inputArray[0].equals("HELP")) {
            System.out.print(this.helpText);
        }
        else if (inputArray[0].equals("LOOK")) {
            this.player.getCurrentRoom().getDescription();
        }
        else if (inputArray[0].equals("INVENTORY")) {
            if (this.player.inventory.size() == 0) {
                System.out.println("INVENTORY EMPTY");
            }
            else {
                System.out.print(this.player.inventory);
            }
        }
        else if (inputArray[0].equals("TAKE")) {
            if (inputArray.length == 1) {
                System.out.println("THE TAKE COMMAND REQUIRES AN OBJECT");
            }
            else if (this.player.getCurrentRoom().objectsInRoom.contains(inputArray[1])) {
                System.out.println(String.format(inputArray[1], "HAS BEEN TAKEN"));
                this.player.takeObject(inputArray[1]);

            }
            else if (this.player.getCurrentRoom().objectsInRoom.contains(inputArray[1]) == false) {
                System.out.println(String.format(inputArray[1], "IS NOT IN ROOM"));
            }
        }
        else if (inputArray[0].equals("DROP")) {
            if (inputArray.length == 1) {
                System.out.println("THE DROP COMMAND REQUIRES AN OBJECT");
            }
            else if (this.player.inventory.contains(inputArray[1])) {
                System.out.println(String.format(inputArray[1], "HAS BEEN DROPPED"));
                this.player.dropObject(inputArray[1]);
            }
            else if (this.player.inventory.contains(inputArray[1]) == false) {
                System.out.println(String.format(inputArray[1], "IS NOT IN INVENTORY"));
            }
        }
        else {
            check = movePlayer(inputArray[0]);
            if (! check) {
                System.out.println("GAME OVER");
            }
        }

        return check; //replace this line as needed!!

    }

    public String[] convertCommand(String command){
        command.strip();
        if (command == "") {
            return new String[0];
        }
        else {
            String[] commands = command.split(" ");
            commands[0] = commands[0].toUpperCase();

            if (commands.length > 2) {
                return new String[0];
            }

            else {
                if (commands.length == 2) {
                    commands[1] = commands[1].toUpperCase();
                }
                for (HashMap.Entry<String, String> entry : this.synonyms.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals(commands[0])) {
                        commands[0] = entry.getValue();
                    }
                    else if (commands.length == 2 && key.equals(commands[1])) {
                        commands[1] = entry.getValue();
                    }
                }
                return commands;
            }
        }
    }

    public boolean movePlayer(String direction) {
        return true;
    }

    public char getRoomColor() {
        return this.player.currentTileType;
    }
}
