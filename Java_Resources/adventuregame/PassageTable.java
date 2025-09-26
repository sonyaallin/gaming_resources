import java.util.*;

public class PassageTable {

    public List<Passage> passages; //A list of all the Passages that exists from a room.

    /**
     * PassageTable constructor
     */
    public PassageTable() {
        this.passages = new ArrayList<>();
    }

    /**
     * addDirection
     * ____________________
     * This method adds an exit or passage to the table.
     *
     * @param entry: A Passage representing a particular exit from a room.
     */
    void addDirection(Passage entry) {
        passages.add(entry);
    }

    /**
     * getPassages
     * ____________________
     * Getter method for passages.
     *
     * @return this.passages
     * A list of all the Passages that exists from a room.
     */
    public List<Passage> getPassages(){ return this.passages; }

}