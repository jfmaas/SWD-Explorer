package SWDgui;

import SWDdata.SWDdatabase;
import SWDgui.SWDrecentlyOpenedFilesManager;
import SWDgui.SWDvis;
import SWDgui.requestHandlers.TextParser;
import SWDio.SWDlogger;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;

public class SWDgui {
    SWDdatabase database = null;
    JPanel leftpanel = null;
    JPanel mainpanel = null;
    JMenu fileMenu = null;
    JFrame frame = null;
    JTextField textIn = null;
    JTextArea descriptionOut = null;
    JTextArea textOut = null;
    JGraph graphArea = null;
    SWDrecentlyOpenedFilesManager recOpened = null;
    String currentPPN = "";
    String currentName = "";
    String currentPICA = "";
    LinkedList<String> history = new LinkedList();
    int historyCounter = 0;
    int childrenDepth = 1;
    int parentsDepth = 4;
    boolean showSiblings = false;
    int breaksize = 500;
    public static final int SHOW_REGEXP = 0;
    public static final int SHOW_EXACT = 1;
    public static final int SHOW_TRUNC = 2;
    public static final int SHOW_OPAC = 3;
    public static final int SHOW_PPN = 4;
    int searchMode = 2;

    public SWDgui(SWDdatabase x) {
        this.database = x;
    }

    public void createGUI() {
        JMenuBar menuBar = new JMenuBar();
        this.recOpened = new SWDrecentlyOpenedFilesManager();
        this.fileMenu = new JMenu("Datei");
        JMenuItem da = new JMenuItem("Datei oeffnen");
        JMenuItem dh = new JMenuItem("Speichere Hauptansetzungsformen in...");
        JMenuItem dc = new JMenuItem("Speichere alle Ansetzungsformen in...");
        JMenuItem db = new JMenuItem("Beenden");
        this.fileMenu.add(da);
        this.fileMenu.add(dh);
        this.fileMenu.add(dc);
        this.fileMenu.add(db);
        this.updateFileMenu();
        menuBar.add(this.fileMenu);
        JMenu viewMenu = new JMenu("Bearbeiten");
        JMenuItem vn1 = new JMenuItem("Kopiere PPN");
        ActionListener al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!SWDgui.this.currentPPN.equals("")) {
                    Clipboard systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                    systemClip.setContents(new StringSelection(SWDgui.this.currentPPN), null);
                    SWDlogger.info("Kopiere Text in Zwischenablage: " + SWDgui.this.currentPPN);
                }
            }
        };
        vn1.addActionListener(al);
        JMenuItem vn2 = new JMenuItem("Kopiere Hauptansetzungsform");
        al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!SWDgui.this.currentName.equals("")) {
                    Clipboard systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                    systemClip.setContents(new StringSelection(SWDgui.this.currentName), null);
                    SWDlogger.info("Kopiere Text in Zwischenablage: " + SWDgui.this.currentName);
                }
            }
        };
        vn2.addActionListener(al);
        JMenuItem vn3 = new JMenuItem("Kopiere Eintrag");
        al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!SWDgui.this.currentName.equals("")) {
                    Clipboard systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                    systemClip.setContents(new StringSelection(SWDgui.this.currentPICA), null);
                    SWDlogger.info("Kopiere Eintrag in Zwischenablage");
                }
            }
        };
        vn3.addActionListener(al);
        viewMenu.add(vn1);
        viewMenu.add(vn2);
        viewMenu.add(vn3);
        menuBar.add(viewMenu);
        JMenu helpMenu = new JMenu("Hilfe");
        helpMenu.add(new JMenuItem("Hilfe"));
        helpMenu.add(new JMenuItem("Version"));
        menuBar.add(helpMenu);
        JButton b1 = new JButton("<-");
        JButton b2 = new JButton("->");
        al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (SWDgui.this.historyCounter > 0) {
                    --SWDgui.this.historyCounter;
                    SWDgui.this.processTextInput(SWDgui.this.history.get(SWDgui.this.historyCounter));
                }
            }
        };
        b1.addActionListener(al);
        al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (SWDgui.this.history.size() - 1 > SWDgui.this.historyCounter) {
                    ++SWDgui.this.historyCounter;
                    SWDgui.this.processTextInput(SWDgui.this.history.get(SWDgui.this.historyCounter));
                }
            }
        };
        b2.addActionListener(al);
        menuBar.add(b1);
        menuBar.add(b2);
        this.frame = new JFrame("SWD-Explorer V1.0.1 beta");
        this.frame.setDefaultCloseOperation(3);
        this.mainpanel = new JPanel();
        this.mainpanel.setLayout(new BorderLayout());
        JPanel menupanel = new JPanel();
        this.mainpanel.add((Component)menupanel, "First");
        this.leftpanel = new JPanel();
        this.leftpanel.setLayout(new BoxLayout(this.leftpanel, 1));
        this.mainpanel.add((Component)this.leftpanel, "Before");
        this.textIn = new JTextField("Suchbegriff", 25);
        menupanel.add(menuBar);
        menupanel.add(this.textIn);
        JButton b3 = new JButton("Suchen");
        al = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                String text = SWDgui.this.textIn.getText();
                SWDgui.this.textIn.selectAll();
                SWDgui.this.processTextInput(text);
                while (SWDgui.this.history.size() - 1 > SWDgui.this.historyCounter) {
                    SWDgui.this.history.removeLast();
                }
                SWDgui.this.history.add(text);
                SWDgui.this.historyCounter = SWDgui.this.history.size() - 1;
            }
        };
        b3.addActionListener(al);
        menupanel.add(b3);
        JComboBox<String> inputSelectorBox = new JComboBox<String>();
        inputSelectorBox.addItem("Exakte Uebereinstimmung");
        inputSelectorBox.addItem("Trunkierung (rechts)");
        inputSelectorBox.addItem("OPAC-Stil");
        inputSelectorBox.addItem("Regulaerer Ausdruck");
        inputSelectorBox.addItem("PPN");
        inputSelectorBox.setSelectedItem("Trunkierung (rechts)");
        menupanel.add(inputSelectorBox);
        JComboBox<String> childrenBox = new JComboBox<String>();
        JComboBox<String> parentBox = new JComboBox<String>();
        childrenBox.addItem("0 Unterbegriffe");
        childrenBox.addItem("1 Unterbegriff");
        childrenBox.addItem("2 Unterbegriffe");
        childrenBox.addItem("3 Unterbegriffe");
        childrenBox.addItem("4 Unterbegriffe");
        childrenBox.addItem("unendlich");
        childrenBox.setSelectedItem("1 Unterbegriff");
        parentBox.addItem("0 Oberbegriffe");
        parentBox.addItem("1 Oberbegriff");
        parentBox.addItem("2 Oberbegriffe");
        parentBox.addItem("3 Oberbegriffe");
        parentBox.addItem("4 Oberbegriffe");
        parentBox.addItem("unendlich");
        parentBox.setSelectedItem("4 Oberbegriffe");
        menupanel.add(childrenBox);
        menupanel.add(parentBox);
        this.textOut = new JTextArea(20, 40);
        this.textOut.setEditable(false);
        this.textOut.append("Systeminformationen\n\n");
        SWDlogger.setOutWindow(this.textOut);
        this.leftpanel.add(new JScrollPane(this.textOut));
        this.descriptionOut = new JTextArea(20, 40);
        this.descriptionOut.setEditable(false);
        this.descriptionOut.append("Objektinformationen\n");
        this.leftpanel.add(new JScrollPane(this.descriptionOut));
        JScrollPane temp = SWDvis.helloSWD();
        final JGraph temp2 = (JGraph)temp.getViewport().getView();
        MouseListener ml = new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    DefaultGraphCell cell = (DefaultGraphCell)temp2.getSelectionCell();
                    String name = (String)cell.getUserObject();
                    SWDgui.this.descriptionOut.append("\n" + name);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        };
        temp2.addMouseListener(ml);
        this.mainpanel.add((Component)temp, "Center");
        inputSelectorBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox selectedChoice = (JComboBox)e.getSource();
                if (selectedChoice.getSelectedItem().equals("Exakte Uebereinstimmung")) {
                    SWDgui.this.searchMode = 1;
                }
                if (selectedChoice.getSelectedItem().equals("Trunkierung (rechts)")) {
                    SWDgui.this.searchMode = 2;
                }
                if (selectedChoice.getSelectedItem().equals("OPAC-Stil")) {
                    SWDgui.this.searchMode = 3;
                }
                if (selectedChoice.getSelectedItem().equals("Regulaerer Ausdruck")) {
                    SWDgui.this.searchMode = 0;
                }
                if (selectedChoice.getSelectedItem().equals("PPN")) {
                    SWDgui.this.searchMode = 4;
                }
            }
        });
        childrenBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox selectedChoice = (JComboBox)e.getSource();
                if (selectedChoice.getSelectedItem().equals("0 Unterbegriffe")) {
                    SWDgui.this.childrenDepth = 0;
                }
                if (selectedChoice.getSelectedItem().equals("1 Unterbegriff")) {
                    SWDgui.this.childrenDepth = 1;
                }
                if (selectedChoice.getSelectedItem().equals("2 Unterbegriffe")) {
                    SWDgui.this.childrenDepth = 2;
                }
                if (selectedChoice.getSelectedItem().equals("3 Unterbegriffe")) {
                    SWDgui.this.childrenDepth = 3;
                }
                if (selectedChoice.getSelectedItem().equals("4 Unterbegriffe")) {
                    SWDgui.this.childrenDepth = 4;
                }
                if (selectedChoice.getSelectedItem().equals("unendlich")) {
                    SWDgui.this.childrenDepth = 99;
                }
            }
        });
        parentBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox selectedChoice = (JComboBox)e.getSource();
                if (selectedChoice.getSelectedItem().equals("0 Oberbegriffe")) {
                    SWDgui.this.parentsDepth = 0;
                }
                if (selectedChoice.getSelectedItem().equals("1 Oberbegriff")) {
                    SWDgui.this.parentsDepth = 1;
                }
                if (selectedChoice.getSelectedItem().equals("2 Oberbegriffe")) {
                    SWDgui.this.parentsDepth = 2;
                }
                if (selectedChoice.getSelectedItem().equals("3 Oberbegriffe")) {
                    SWDgui.this.parentsDepth = 3;
                }
                if (selectedChoice.getSelectedItem().equals("4 Oberbegriffe")) {
                    SWDgui.this.parentsDepth = 4;
                }
                if (selectedChoice.getSelectedItem().equals("unendlich")) {
                    SWDgui.this.parentsDepth = 99;
                }
            }
        });
        ActionListener textEingabe = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent evt) {
                String text = SWDgui.this.textIn.getText();
                SWDgui.this.textIn.selectAll();
                SWDgui.this.processTextInput(text);
                while (SWDgui.this.history.size() - 1 > SWDgui.this.historyCounter) {
                    SWDgui.this.history.removeLast();
                }
                SWDgui.this.history.add(text);
                SWDgui.this.historyCounter = SWDgui.this.history.size() - 1;
            }
        };
        this.textIn.addActionListener(textEingabe);
        ActionListener dateiBeenden = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        db.addActionListener(dateiBeenden);
        ActionListener dateiOeffnen = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int state = chooser.showOpenDialog(null);
                if (state == 0) {
                    File file = chooser.getSelectedFile();
                    SWDlogger.info("Oeffnen der Datei: " + file.getName());
                    SWDgui.this.recOpened.setRecentlyOpenedFile(file.getAbsolutePath());
                    SWDgui.this.updateFileMenu();
                    SWDgui.this.database.read(file);
                } else {
                    SWDlogger.info("Auswahl abgebrochen");
                }
            }
        };
        da.addActionListener(dateiOeffnen);
        ActionListener hauptformenSchreiben = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int state = chooser.showOpenDialog(null);
                if (state == 0) {
                    File file = chooser.getSelectedFile();
                    SWDlogger.info("Schreibe aktuelle Hauptansetzungsformen in Datei: " + file.getName());
                    BufferedWriter bf = null;
                    try {
                        try {
                            String[] names;
                            bf = new BufferedWriter(new FileWriter(file));
                            String[] arrstring = names = SWDgui.this.database.getNames();
                            int n = arrstring.length;
                            int n2 = 0;
                            while (n2 < n) {
                                String name = arrstring[n2];
                                bf.append(name);
                                bf.newLine();
                                ++n2;
                            }
                        }
                        catch (IOException erro) {
                            System.err.println("Konnte Datei nicht erstellen");
                            try {
                                bf.close();
                            }
                            catch (IOException iOException) {}
                        }
                    }
                    finally {
                        try {
                            bf.close();
                        }
                        catch (IOException iOException) {}
                    }
                } else {
                    SWDlogger.info("Auswahl abgebrochen");
                }
            }
        };
        dh.addActionListener(hauptformenSchreiben);
        ActionListener alleAnsetzungsformenSchreiben = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int state = chooser.showOpenDialog(null);
                if (state == 0) {
                    File file = chooser.getSelectedFile();
                    SWDlogger.info("Schreibe alle aktuellen Ansetzungsformen in Datei: " + file.getName());
                    BufferedWriter bf = null;
                    try {
                        try {
                            bf = new BufferedWriter(new FileWriter(file));
                            LinkedList<String> names = SWDgui.this.database.getAllNames();
                            for (String name : names) {
                                bf.append(name);
                                bf.newLine();
                            }
                        }
                        catch (IOException erro) {
                            System.err.println("Konnte Datei nicht erstellen");
                            try {
                                bf.close();
                            }
                            catch (IOException iOException) {}
                        }
                    }
                    finally {
                        try {
                            bf.close();
                        }
                        catch (IOException iOException) {}
                    }
                } else {
                    SWDlogger.info("Auswahl abgebrochen");
                }
            }
        };
        dc.addActionListener(alleAnsetzungsformenSchreiben);
        this.frame.add(this.mainpanel);
        this.frame.pack();
        this.frame.setVisible(true);
    }

    public void processTextInput(String text) {
        SWDlogger.info("Auswahl: " + text);
        SWDvis visualisierung = new SWDvis();
        visualisierung.setDatabase(this.database);
        visualisierung.setBreaksize(this.breaksize);
        this.textIn.setText(text);
        TextParser parser = new TextParser(visualisierung, this.database, this.childrenDepth, this.parentsDepth, this.showSiblings, this.searchMode);
        visualisierung = parser.getGraph(text);
        if (visualisierung != null) {
            this.updateWindow(visualisierung);
        }
    }

    private void updateFileMenu() {
        int itemCount = this.fileMenu.getMenuComponentCount();
        LinkedList<String> files = this.recOpened.getRecentlyOpenedFiles();
        LinkedList toDel = new LinkedList();
        if (itemCount > 4) {
            int z = 4;
            while (z < itemCount) {
                this.fileMenu.remove(4);
                ++z;
            }
        }
        if (files.size() > 0) {
            LinkedList<JMenuItem> entries = new LinkedList<JMenuItem>();
            this.fileMenu.addSeparator();
            int r = 0;
            for (String file : files) {
                int a2;
                int a = 0;
                int a1 = file.lastIndexOf("\\");
                a = a1 > (a2 = file.lastIndexOf("/")) ? a1 : a2;
                file = file.substring(a + 1);
                JMenuItem fileIns = new JMenuItem(String.valueOf(Integer.toString(++r)) + " " + file);
                this.fileMenu.add(fileIns);
                entries.add(fileIns);
            }
            int q = 0;
            while (q < files.size()) {
                JMenuItem menuItem = (JMenuItem)entries.get(q);
                final String filename = files.get(q);
                ActionListener specOpen = new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SWDlogger.info("Oeffnen der Datei: " + filename);
                        SWDgui.this.database.read(new File(filename));
                    }
                };
                menuItem.addActionListener(specOpen);
                ++q;
            }
        }
        this.fileMenu.updateUI();
    }

    private void updateWindow(SWDvis graph) {
        boolean maximized = false;
        int state = this.frame.getExtendedState();
        if ((state & 6) == 6) {
            maximized = true;
        }
        this.mainpanel.remove(2);
        JScrollPane temp = graph.getGraphScrollPane();
        final JGraph temp2 = (JGraph)temp.getViewport().getView();
        MouseListener ml = new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    try {
                        if (e.getClickCount() == 1) {
                            DefaultGraphCell cell = (DefaultGraphCell)temp2.getSelectionCell();
                            String name = (String)cell.getUserObject();
                            String insert = SWDgui.this.database.getDescription(name);
                            SWDgui.this.descriptionOut.setText("");
                            SWDgui.this.descriptionOut.append(insert);
                            SWDgui.this.currentName = SWDgui.this.database.getNameByName(name);
                            SWDgui.this.currentPPN = SWDgui.this.database.getPPNByName(name);
                            SWDgui.this.currentPICA = SWDgui.this.database.getDescription(SWDgui.this.currentName);
                        } else {
                            DefaultGraphCell cell = (DefaultGraphCell)temp2.getSelectionCell();
                            String name = (String)cell.getUserObject();
                            name = SWDgui.this.database.getNameByName(name);
                            int currentSearchMode = SWDgui.this.searchMode;
                            SWDgui.this.searchMode = 1;
                            SWDgui.this.processTextInput(name);
                            SWDgui.this.searchMode = currentSearchMode;
                            while (SWDgui.this.history.size() - 1 > SWDgui.this.historyCounter) {
                                SWDgui.this.history.removeLast();
                            }
                            SWDgui.this.history.add(name);
                            SWDgui.this.historyCounter = SWDgui.this.history.size() - 1;
                        }
                    }
                    catch (NullPointerException cell) {
                        // empty catch block
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        };
        temp2.addMouseListener(ml);
        this.mainpanel.add((Component)temp, "Center");
        this.frame.pack();
        this.frame.validate();
        int i = 0;
        while (i < 3) {
            try {
                Thread.sleep(500L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (maximized) {
                this.frame.setExtendedState(6);
            }
            ++i;
        }
    }
}
