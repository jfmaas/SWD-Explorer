package SWDgui.requestHandlers;

import SWDdata.SWDdatabase;
import SWDgui.SWDvis;
import SWDgui.requestHandlers.OPACParser;
import SWDio.SWDlogger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextParser {
    private SWDvis graph;
    private SWDdatabase database;
    int childrenDepth = 0;
    int parentsDepth = 0;
    boolean showSiblings = false;
    int searchMode = 0;

    public TextParser(SWDvis g, SWDdatabase d, int c, int p, boolean s, int sm) {
        this.graph = g;
        this.database = d;
        this.childrenDepth = c;
        this.parentsDepth = p;
        this.showSiblings = s;
        this.searchMode = sm;
    }

    public SWDvis getGraph(String input) {
        LinkedList<String> queriedElements = new LinkedList<String>();
        if (this.searchMode == 0) {
            queriedElements = this.getNamesByRegExp(input);
        }
        if (this.searchMode == 1) {
            LinkedList<String> names = this.database.getAllNames();
            for (String s : names) {
                if (!s.equalsIgnoreCase(input) || s == null || s.equals("")) continue;
                queriedElements.add(this.database.getNameByName(s));
            }
        }
        if (this.searchMode == 2) {
            String smallinput = input.toLowerCase();
            LinkedList<String> names = this.database.getAllNames();
            Iterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                String s = iterator.next();
                String smalls = s.toLowerCase();
                if (!smalls.startsWith(smallinput) || s == null || s.equals("")) continue;
                queriedElements.add(this.database.getNameByName(s));
            }
        }
        if (this.searchMode == 3) {
            queriedElements = this.getNamesByOPAC(input);
        }
        if (this.searchMode == 4) {
            queriedElements = this.getNamesByPPN(input);
        }
        if (queriedElements.size() == 0) {
            SWDlogger.info("Es wurden keine matchenden Ausdr\u00fccke im Datensatz gefunden.");
            return null;
        }
        for (String name : queriedElements) {
            this.graph.setEntry(name, "red");
        }
        if (this.childrenDepth < 1 || this.parentsDepth < 1) {
            this.getRelationsInCaseOfDepthEqualsZero(queriedElements);
        }
        this.processRelatives(queriedElements, this.childrenDepth, this.parentsDepth);
        return this.graph;
    }

    public SWDvis getGraphFromNameList(LinkedList<String> input) {
        LinkedList<String> queriedElements = new LinkedList<String>();
        for (String name : input) {
            String q = this.database.getNameByName(name);
            if (q == null) continue;
            queriedElements.add(q);
        }
        if (queriedElements.size() == 0) {
            SWDlogger.info("Es wurden keine matchenden Ausdr\u00fccke im Datensatz gefunden.");
            return null;
        }
        for (String name : queriedElements) {
            this.graph.setEntry(name, "red");
        }
        if (this.childrenDepth < 1 || this.parentsDepth < 1) {
            this.getRelationsInCaseOfDepthEqualsZero(queriedElements);
        }
        this.processRelatives(queriedElements, this.childrenDepth, this.parentsDepth);
        return this.graph;
    }

    private void processRelatives(LinkedList<String> names, int l_childrenDepth, int l_parentsDepth) {
        HashMap<String, Boolean> entriesToShow = new HashMap<String, Boolean>();
        entriesToShow.putAll(this.processChildren(names, l_childrenDepth));
        entriesToShow.putAll(this.processParents(names, l_parentsDepth));
        for (String name : entriesToShow.keySet()) {
            this.graph.setEntry(name);
            if (this.database.getCombined(name)) {
                this.graph.setEntry(name, "green");
            }
            for (String child : this.database.getChildrenNames(name)) {
                if (!entriesToShow.containsKey(child)) continue;
                this.graph.setPCRelation(name, child);
            }
        }
    }

    private HashMap<String, Boolean> processChildren(LinkedList<String> names, int l_depth) {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        if (l_depth < 1) {
            return result;
        }
        --l_depth;
        for (String name : names) {
            LinkedList<String> l_children = this.database.getChildrenNames(name);
            for (String child : l_children) {
                result.put(name, true);
                result.put(child, true);
            }
            if (l_depth <= 0 || l_children.size() <= 0) continue;
            result.putAll(this.processChildren(l_children, l_depth));
        }
        return result;
    }

    private HashMap<String, Boolean> processParents(LinkedList<String> names, int depth) {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        if (depth < 1) {
            return result;
        }
        --depth;
        for (String name : names) {
            LinkedList<String> l_parents = this.database.getParentNames(name);
            for (String parent : l_parents) {
                result.put(name, true);
                result.put(parent, true);
            }
            if (depth <= 0 || l_parents.size() <= 0) continue;
            result.putAll(this.processParents(l_parents, depth));
        }
        return result;
    }

    private LinkedList<String> getNamesByOPAC(String input) {
        OPACParser parser = new OPACParser(this.database);
        parser.setInput(input);
        return parser.parseInput();
    }

    private LinkedList<String> getNamesByPPN(String input) {
        String[] names;
        LinkedList<String> result = new LinkedList<String>();
        String[] arrstring = names = this.database.getNames();
        int n = arrstring.length;
        int n2 = 0;
        while (n2 < n) {
            String name = arrstring[n2];
            String ppn = this.database.getPPNByName(name);
            if (ppn != null && ppn.equals(input)) {
                result.add(name);
            }
            ++n2;
        }
        return result;
    }

    private LinkedList<String> getNamesByRegExp(String input) {
        LinkedList<String> result = new LinkedList<String>();
        LinkedList<String> allNames = this.database.getAllNames();
        if (!input.equals("") && input != null) {
            try {
                Pattern p = Pattern.compile(input, 66);
                for (String name : allNames) {
                    Matcher m = p.matcher(name);
                    boolean b = m.matches();
                    if (!b) continue;
                    result.add(name);
                }
            }
            catch (PatternSyntaxException e) {
                SWDlogger.warn("Nicht zul\u00e4ssiger Suchbegriff: Kein regul\u00e4rer Ausdruck!");
                return new LinkedList<String>();
            }
            HashMap<String, Boolean> trzt = new HashMap<String, Boolean>();
            Boolean blah = new Boolean(false);
            for (String name : result) {
                String g = this.database.getNameByName(name);
                trzt.put(g, blah);
            }
            result = new LinkedList();
            for (String key : trzt.keySet()) {
                result.add(key);
            }
        }
        return result;
    }

    private void getRelationsInCaseOfDepthEqualsZero(LinkedList<String> names) {
        HashMap<String, String> contained = new HashMap<String, String>();
        for (String name : names) {
            contained.put(name, " ");
        }
        for (String name : names) {
            LinkedList<String> l_children = this.database.getChildrenNames(name);
            for (String element : l_children) {
                if (!contained.containsKey(element)) continue;
                this.graph.setPCRelation(name, element);
            }
        }
    }
}
