package SWDio;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JTextArea;
import javax.swing.text.Document;

public class SWDlogger {
    private static ReentrantLock lock = new ReentrantLock();
    private static String outfile = "SWDlog.log";
    private static JTextArea outWindow = null;

    public static void setOutfile(String file) {
        outfile = file;
    }

    public static void setOutWindow(JTextArea a) {
        outWindow = a;
    }

    public static void info(String message) {
        try {
            lock.lock();
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));
            out.write("Info: " + message);
            System.out.println("Info: " + message);
            out.newLine();
            out.close();
            lock.unlock();
        }
        catch (IOException e) {
            System.err.println("Logfile IO-Fehler!");
            System.err.println(e);
            System.exit(1);
        }
        if (outWindow != null) {
            outWindow.setCaretPosition(outWindow.getDocument().getLength());
            outWindow.append(String.valueOf(message) + "\n");
        }
    }

    public static void warn(String message) {
        try {
            lock.lock();
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));
            out.write("Warnung: " + message);
            out.newLine();
            out.close();
            lock.unlock();
        }
        catch (IOException e) {
            System.err.println("Logfile IO-Fehler!");
            System.exit(1);
        }
        if (outWindow != null) {
            outWindow.setCaretPosition(outWindow.getDocument().getLength());
            outWindow.append("Warnung: " + message + "\n");
        }
    }

    public static void error(String message) {
        try {
            lock.lock();
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));
            out.write("Error: " + message);
            System.out.println("Error: " + message);
            out.newLine();
            out.close();
            System.exit(1);
            lock.unlock();
        }
        catch (IOException e) {
            System.err.println("Logfile IO-Fehler!");
            System.exit(1);
        }
    }
}
