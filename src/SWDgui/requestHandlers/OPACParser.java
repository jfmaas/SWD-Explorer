package SWDgui.requestHandlers;

import SWDdata.SWDatabase;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class OPACParser {
    SWDatabase database;
    String input = "";

    public OPACParser(SWDatabase d) {
        this.database = d;
    }

    public void setInput(String i) {
        this.input = i;
    }

    public LinkedList<String> parseInput() {
        LinkedList<String> result = this.processInput(this.input);
        System.out.println(result);
        return result;
    }

    private LinkedList<String> processInput(String input) {
        LinkedList<String> eingabe = new LinkedList<String>();
        input = input.replaceAll("\\(", " ( ");
        input = input.replaceAll("\\)", " ) ");
        String[] result = input.split("\\s");
        int x = 0;
        while (x < result.length) {
            String t = result[x];
            t.trim();
            if (!result[x].equals("")) {
                eingabe.add(result[x]);
            }
            ++x;
        }
        return this.processTerm(eingabe);
    }

    private LinkedList<String> processTerm(LinkedList<String> eingabe) {
        LinkedList<Object> result = new LinkedList();
        String first = eingabe.removeFirst();
        if (first.equals("(")) {
            int x = 1;
            LinkedList<String> part = new LinkedList<String>();
            while (x > 0) {
                String pt = eingabe.removeFirst();
                if (pt.equals("(")) {
                    ++x;
                }
                if (pt.equals(")")) {
                    --x;
                }
                part.add(pt);
            }
            result = this.processTerm(part);
        } else {
            boolean t = true;
            while (t && eingabe.size() > 0) {
                if (eingabe.getFirst().equals("AND") || eingabe.getFirst().equals("OR")) {
                    t = false;
                    continue;
                }
                first = first.concat(" " + eingabe.removeFirst());
            }
            result = this.search(first);
        }
        if (eingabe.size() == 0) {
            return result;
        }
        String connector = eingabe.removeFirst();
        if (connector.equals("AND")) {
            LinkedList<String> rest = this.processTerm(eingabe);
            HashMap<String, String> check = new HashMap<String, String>();
            for (String h : rest) {
                check.put(h, " ");
            }
            LinkedList<String> re = new LinkedList<String>();
            for (String h : result) {
                if (!check.containsKey(h)) continue;
                re.add(h);
            }
            result = re;
        }
        if (connector.equals("OR")) {
            result.addAll(this.processTerm(eingabe));
        }
        return result;
    }

    private LinkedList<String> search(String e) {
        LinkedList<String> result = new LinkedList<String>();
        boolean truncated = false;
        LinkedList<String> allNames = this.database.getAllNames();
        System.out.println(">" + e + "<");
        if (e.endsWith("$")) {
            e = e.replaceAll("\\$", "");
            truncated = true;
        }
        System.out.println(e);
        for (String name : allNames) {
            if (truncated) {
                String e1;
                String n1 = name.toLowerCase();
                if (!n1.startsWith(e1 = e.toLowerCase())) continue;
                result.add(name);
                continue;
            }
            if (!name.equalsIgnoreCase(e)) continue;
            result.add(name);
        }
        HashMap<String, String> sdl = new HashMap<String, String>();
        for (String name : result) {
            sdl.put(this.database.getNameByName(name), " ");
        }
        result = new LinkedList();
        result.addAll(sdl.keySet());
        return result;
    }
}
