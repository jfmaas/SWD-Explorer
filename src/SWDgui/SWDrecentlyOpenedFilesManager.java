package SWDgui;

import SWDio.SWDlogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.LinkedList;

public class SWDrecentlyOpenedFilesManager {
    String configFileName = "recentlyOpened.txt";
    File configFile = new File(this.configFileName);
    LinkedList<String> files = new LinkedList();

    public SWDrecentlyOpenedFilesManager() {
        if (!this.configFile.canWrite()) {
            SWDlogger.warn("Kann Konfigurationsdatei nicht schreiben!");
            return;
        }
        if (!this.configFile.exists()) {
            SWDlogger.info("Erzeuge neue Konfigurationsdatei.");
            try {
                this.configFile.createNewFile();
            }
            catch (IOException e) {
                SWDlogger.warn("Kann Konfigurationsdatei nicht schreiben!");
                return;
            }
        }
    }

    public LinkedList<String> getRecentlyOpenedFiles() {
        this.files = new LinkedList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.configFile));
            boolean c = true;
            while (c) {
                String line = reader.readLine();
                if (line == null) {
                    c = false;
                    continue;
                }
                this.files.add(line);
            }
            reader.close();
        }
        catch (Exception e) {
            SWDlogger.warn("Fehler beim Lesen der Konfigurationsdatei");
        }
        return this.files;
    }

    public void setRecentlyOpenedFile(String filename) {
        int isAlready = 0;
        int i = 0;
        for (String list : this.files) {
            if (filename.equals(list)) {
                isAlready = i;
            }
            ++i;
        }
        if (isAlready > 0) {
            this.files.remove(isAlready);
        }
        this.files.addFirst(filename);
        while (this.files.size() > 4) {
            this.files.removeLast();
        }
        this.writeFile();
    }

    public void clearRecentlyOpenedFiles() {
        this.files = new LinkedList();
        this.writeFile();
    }

    private void writeFile() {
        try {
            PrintWriter out = new PrintWriter(this.configFileName);
            for (String line : this.files) {
                out.println(line);
            }
            out.close();
        }
        catch (Exception e) {
            SWDlogger.warn("Fehler beim Schreiben der Konfigurationsdatei");
        }
    }
}
