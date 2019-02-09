package SWDdata;

import SWDdata.SWDentry;
import SWDio.SWDlogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SWDdatabase {
    private HashMap<String, String> h_nameToName = new HashMap();
    private HashMap<String, SWDentry> h_nameToEntry = new HashMap();
    private LinkedList<SWDentry> l_entrylist = new LinkedList();
    private HashMap<String, LinkedList<String>> h_nameToChildren = new HashMap();
    private HashMap<String, LinkedList<String>> h_nameToParents = new HashMap();
    final Pattern pattern = Pattern.compile("^\\d\\d\\d$");
    ReentrantLock lock = new ReentrantLock();

    public SWDdatabase() {
    }

    public SWDdatabase(File infile) {
        this.l_entrylist = this.readFile(infile);
        this.updateHashes();
        String number = Integer.toString(this.l_entrylist.size());
        SWDlogger.info("Datei eingelesen. " + number + " Datensaetze");
    }

    public void read(File infile) {
        SWDlogger.info("Lese Datei " + infile.getName() + ". Bitte warten.");
        LinkedList<SWDentry> entrylist = new LinkedList();
        entrylist = (LinkedList<SWDentry>) this.readFile(infile);
        this.lock.lock();
        Iterator<SWDentry> iter = entrylist.iterator();
        while (iter.hasNext()) {
            this.l_entrylist.add((SWDentry)iter.next());
        }
        this.lock.unlock();
        this.updateHashes();
        String number = Integer.toString(this.l_entrylist.size());
        SWDlogger.info("Datei eingelesen. " + number + " Datensaetze");
        System.gc();
    }

    private LinkedList<SWDentry> readFile(File infile) {
        this.lock.lock();
        LinkedList<SWDentry> l_elist = new LinkedList<SWDentry>();
        try {
            LinkedList<String> l_linelist = new LinkedList<String>();
            SWDentry entry = null;
            boolean c = true;
            boolean first = true;
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            while (c) {
                String[] x;
                Matcher m;
                String s_line = reader.readLine();
                if (s_line == null) {
                    c = false;
                    continue;
                }
                String s_test = "";
                if ((s_line = s_line.trim()).isEmpty() || !(m = this.pattern.matcher(s_test = (x = s_line.split("\\s", 2))[0])).matches() && !s_test.equals("SET:")) continue;
                if (s_test.equals("SET:")) {
                    if (!first) {
                        entry = new SWDentry(l_linelist);
                        l_linelist = new LinkedList();
                        l_linelist.add(s_line);
                        l_elist.add(entry);
                        continue;
                    }
                    first = false;
                    l_linelist.add(s_line);
                    continue;
                }
                l_linelist.add(s_line);
            }
            if (l_linelist.size() > 0) {
                entry = new SWDentry(l_linelist);
                l_elist.add(entry);
            }
            reader.close();
        }
        catch (IOException e) {
            System.err.println("Datei " + infile.getName() + " kann nicht gelesen werden!");
            System.exit(0);
        }
        this.lock.unlock();
        return l_elist;
    }

    private void processCombinedEntries() {
        LinkedList<SWDentry> l_temp = new LinkedList<SWDentry>();
        for (SWDentry entry : this.l_entrylist) {
            if (entry.getCombined()) continue;
            l_temp.add(entry);
        }
        this.l_entrylist = l_temp;
        HashMap <String,LinkedList<String>> keyToNames = new HashMap();
        for (SWDentry entry : this.l_entrylist) {
            LinkedList<String> parents = entry.getParents();
            for (String parent : parents) {
                String[] s;
                if (!parent.contains("/")) continue;
                String key = "";
                TreeSet<String> sorter = new TreeSet<String>();
                String[] arrstring = s = parent.split("/");
                int n = arrstring.length;
                int n2 = 0;
                while (n2 < n) {
                    String name = arrstring[n2];
                    name = name.trim();
                    sorter.add(name);
                    ++n2;
                }
                for (String name : sorter) {
                    key = key.concat("_!_" + name);
                }
                if (!keyToNames.containsKey(key)) {
                    keyToNames.put(key, new LinkedList());
                }
                ((LinkedList)keyToNames.get(key)).add(parent);
            }
        }
        for (String key : keyToNames.keySet()) {
            LinkedList<String> names = new LinkedList<String>();
            HashMap<String, Boolean> red = new HashMap<String, Boolean>();
            for (String name : (LinkedList) keyToNames.get(key)) {
                if (red.containsKey(name)) continue;
                names.add(name);
                red.put(name, false);
            }
            SWDentry entry = new SWDentry(names, true);
            this.l_entrylist.add(entry);
        }
    }

    private void updateHashes() {
        this.processCombinedEntries();
        LinkedList<SWDentry> t = new LinkedList<SWDentry>();
        HashMap<String, String> names = new HashMap<String, String>();
        Iterator<SWDentry> iter = this.l_entrylist.descendingIterator();
        while (iter.hasNext()) {
            SWDentry entry = iter.next();
            String name = entry.getName();
            if (names.containsKey(name)) {
                String ppnAlt = (String)names.get(name);
                String ppnNeu = entry.getPPN();
                SWDlogger.warn("Dublette: " + (String)name + ". PPN-alt: " + ppnAlt + " PPN-neu: " + ppnNeu + " Das aeltere Element wird ueberschrieben.");
            } else {
                names.put(name, entry.getPPN());
                t.add(entry);
            }
            HashMap<String, String> u = new HashMap<String, String>();
            for (String s_name : entry.getAllNames()) {
                if (u.containsKey(s_name)) {
                    SWDlogger.warn("Der Eintrag: " + s_name + " spezifiziert denselben Namen mehrfach!");
                }
                u.put(s_name, "");
            }
        }
        this.l_entrylist = new LinkedList();
        iter = t.descendingIterator();
        while (iter.hasNext()) {
            this.l_entrylist.add(iter.next());
        }
        this.h_nameToEntry = new HashMap();
        this.h_nameToName = new HashMap();
        this.h_nameToChildren = new HashMap();
        this.h_nameToParents = new HashMap();
        for (SWDentry entry : this.l_entrylist) {
            LinkedList<String> content = entry.getAllNames();
            for (String name : content) {
                this.h_nameToName.put(name, entry.getName());
                this.h_nameToEntry.put(name, entry);
            }
        }
        for (SWDentry entry : this.l_entrylist) {
            LinkedList<String> parents = new LinkedList<String>();
            HashMap<String, Boolean> re = new HashMap<String, Boolean>();
            for (String parent : entry.getParents()) {
                String f = this.h_nameToName.get(parent);
                if (re.containsKey(f) || f == null) continue;
                parents.add(f);
                re.put(f, false);
            }
            this.h_nameToParents.put(entry.getName(), parents);
        }
        for (SWDentry entry : this.l_entrylist) {
            String name = entry.getName();
            LinkedList<String> l_parents = entry.getParents();
            Iterator<String> parents = l_parents.iterator();
            while (parents.hasNext()) {
                String parent = this.h_nameToName.get(parents.next());
                LinkedList temp = new LinkedList();
                if (this.h_nameToChildren.containsKey(parent)) {
                    temp = this.h_nameToChildren.get(parent);
                }
                temp.add(name);
                this.h_nameToChildren.put(parent, (LinkedList<String>)temp);
            }
        }
        for (String name : this.h_nameToChildren.keySet()) {
            LinkedList<String> rem = new LinkedList<String>();
            LinkedList<String> children = this.h_nameToChildren.get(name);
            HashMap<String, Boolean> u = new HashMap<String, Boolean>();
            for (String child : children) {
                u.put(child, false);
            }
            for (String child : u.keySet()) {
                rem.add(child);
            }
            this.h_nameToChildren.put(name, rem);
        }
    }

    public void printNames() {
        this.lock.lock();
        for (SWDentry one : this.l_entrylist) {
            String name = one.getName();
            System.out.println(name);
            Iterator<String> iterr = one.getParents().iterator();
            while (iterr.hasNext()) {
                System.out.println("     " + iterr.next());
            }
        }
        this.lock.unlock();
    }

    public String[] getNames() {
        String[] result = new String[this.l_entrylist.size()];
        Iterator<SWDentry> iter = this.l_entrylist.iterator();
        int i = 0;
        while (iter.hasNext()) {
            String name;
            SWDentry x = iter.next();
            result[i] = name = x.getName();
            ++i;
        }
        return result;
    }

    public LinkedList<String> getAllNames() {
        LinkedList<String> result = new LinkedList<String>();
        for (SWDentry entry : this.l_entrylist) {
            LinkedList<String> names = entry.getAllNames();
            for (String name : names) {
                result.add(name);
            }
        }
        return result;
    }

    public SWDentry[] getEntries() {
        SWDentry[] result = new SWDentry[this.l_entrylist.size()];
        Iterator<SWDentry> iter = this.l_entrylist.iterator();
        int i = 0;
        while (iter.hasNext()) {
            SWDentry x;
            result[i] = x = iter.next();
            ++i;
        }
        return result;
    }

    public LinkedList<String> getChildrenNames(String name) {
        LinkedList<String> result = new LinkedList<String>();
        if (this.h_nameToChildren.get(name = this.getNameByName(name)) != null) {
            for (String s : this.h_nameToChildren.get(name)) {
                result.add(s);
            }
        }
        return result;
    }

    public boolean getCombined(String name) {
        SWDentry entry = this.getEntryByName(name);
        if (entry != null) {
            return entry.getCombined();
        }
        return false;
    }

    public LinkedList<String> getParentNames(String name) {
        LinkedList<String> result = new LinkedList<String>();
        if (this.h_nameToParents.get(name = this.getNameByName(name)) != null) {
            for (String s : this.h_nameToParents.get(name)) {
                result.add(s);
            }
        }
        return result;
    }

    public int getParentCount(String name) {
        if (this.h_nameToParents.get(name = this.getNameByName(name)) != null) {
            return this.h_nameToParents.get(name).size();
        }
        return 0;
    }

    public int getChildrenCount(String name) {
        if (this.h_nameToChildren.get(name = this.getNameByName(name)) != null) {
            return this.h_nameToChildren.get(name).size();
        }
        return 0;
    }

    public SWDentry getEntryByName(String s) {
        s = s.replaceFirst(" \\(.*\\)", "");
        SWDentry result = null;
        if (this.h_nameToEntry.containsKey(s)) {
            result = this.h_nameToEntry.get(s);
        }
        return result;
    }

    public String getPPNByName(String s) {
        SWDentry entry = this.getEntryByName(s);
        if (entry == null) {
            return null;
        }
        return entry.getPPN();
    }

    public String getNameByName(String s) {
        s = s.replaceFirst(" \\(.*/.*\\)", "");
        String r = "";
        if (this.h_nameToName.containsKey(s)) {
            r = this.h_nameToName.get(s);
        }
        return r;
    }

    public String getDescription(String s) {
        if (s == null) {
            return "";
        }
        SWDentry x = this.getEntryByName(s);
        if (x != null) {
            return "PPN: " + x.getPPN() + "\n" + x.getContent();
        }
        return "";
    }

    public LinkedList<String> getAllEntriesWithChangeCommentary() {
        LinkedList<String> result = new LinkedList<String>();
        for (SWDentry entry : this.l_entrylist) {
            String name = entry.getChangeCommentary();
            if (name == null) continue;
            result.add(entry.getName());
        }
        return result;
    }
}
