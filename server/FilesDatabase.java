package server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilesDatabase implements Serializable {

    private int currentID;
    private Map<String, Integer> namesAndIDs;

    public FilesDatabase() {
        this.currentID = 0;
        this. namesAndIDs = new HashMap<>();
    }

    public int getCurrentID() {
        return currentID;
    }

    public void setCurrentID(int currentID) {
        this.currentID = currentID;
    }

    public Map<String, Integer> getNamesAndIDs() {
        return namesAndIDs;
    }

    public int saveToFilesDatabase(String fileName) {
        ++currentID;
        namesAndIDs.put(fileName, currentID);
        return currentID;
    }

    public String getNameByID(int id) {
        for (Map.Entry<String, Integer> entry : namesAndIDs.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        return "netu blin takogo ID !";
    }

    public void deleteByName(String fileName) {
        namesAndIDs.remove(fileName);
    }

    public void deleteByID(int id) {
        String key;
        for (Map.Entry<String, Integer> entry : namesAndIDs.entrySet()) {
            if (entry.getValue() == id) {
                key = entry.getKey();
                namesAndIDs.remove(key);
                return;
            }
        }
    }

}
