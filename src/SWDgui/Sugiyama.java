package SWDgui;

import SWDdata.SWDrelation;
import SWDgui.LexicographicCompare;
import SWDio.SWDlogger;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class Sugiyama {
    int breaksize = 750;
    int currentSize = 20;
    LinkedList<SWDrelation> pcRelations = new LinkedList();
    LinkedList<String> v = null;
    HashMap<String, String> isChild = null;
    LinkedList<LinkedList<String>> layers = new LinkedList();

    public Sugiyama(int breakSize) {
        this.breaksize = breakSize;
        if (this.breaksize <= 20) {
            this.breaksize = 100;
        }
    }

    public LinkedList<LinkedList<String>> performSugiyamaLayering(LinkedList<String> vinput, LinkedList<SWDrelation> rt, HashMap<String, String> isChildInput) {
        this.v = vinput;
        this.isChild = isChildInput;
        HashMap<String, Boolean> g = new HashMap<String, Boolean>();
        for (String vs : vinput) {
            g.put(vs, false);
        }
        for (SWDrelation rel : rt) {
            if (!g.containsKey(rel.getChild()) && !g.containsKey(rel.getParent())) continue;
            this.pcRelations.add(rel);
        }
        this.eliminateCircles();
        int nodecount = this.v.size();
        if (this.v.size() == 0) {
            return this.layers;
        }
        if (this.v.size() == 1) {
            this.layers.add(this.v);
            return this.layers;
        }
        if (this.v.size() == 2) {
            LinkedList<String> a = new LinkedList<String>();
            LinkedList<String> b = new LinkedList<String>();
            for (SWDrelation rel : rt) {
                if (rel.getChild().equals(this.v.get(0)) && rel.getParent().equals(this.v.get(1))) {
                    a.add(this.v.get(1));
                    b.add(this.v.get(0));
                    this.layers.add(a);
                    this.layers.add(b);
                    return this.layers;
                }
                if (!rel.getChild().equals(this.v.get(1)) || !rel.getParent().equals(this.v.get(0))) continue;
                a.add(this.v.get(0));
                b.add(this.v.get(1));
                this.layers.add(a);
                this.layers.add(b);
                return this.layers;
            }
        }
        this.coffmanGraham();
        this.insertDummies();
        this.medianCrossingReduction();
        this.removeDummies();
        int nodecount2 = 0;
        for (LinkedList<String> r : this.layers) {
            nodecount2 += r.size();
        }
        if (nodecount != nodecount2) {
            SWDlogger.error("Element count mismatch error in CoffmanGraham");
        }
        return this.layers;
    }

    private void eliminateCircles() {
        LinkedList<SWDrelation> temp1 = this.pcRelations;
        this.pcRelations = new LinkedList();
        for (SWDrelation rel : temp1) {
            if (rel.getChild().equals(rel.getParent())) continue;
            this.pcRelations.add(rel);
        }
        temp1 = this.pcRelations;
        this.pcRelations = new LinkedList();
        HashMap<String, Boolean> temp2 = new HashMap<String, Boolean>();
        HashMap<String, String> toDel = new HashMap<String, String>();
        for (SWDrelation rel : temp1) {
            String parent = rel.getParent();
            String child = rel.getChild();
            if (!temp2.containsKey(String.valueOf(parent) + "_!_" + child) && !temp2.containsKey(String.valueOf(child) + "_!_" + parent)) {
                temp2.put(String.valueOf(parent) + "_!_" + child, true);
                temp2.put(String.valueOf(child) + "_!_" + parent, true);
                continue;
            }
            if (parent.length() > child.length()) {
                toDel.put(parent, child);
                continue;
            }
            toDel.put(child, parent);
        }
        for (SWDrelation rel : temp1) {
            if (!toDel.containsKey(rel.getParent()) || !((String)toDel.get(rel.getParent())).equals(rel.getChild())) {
                this.pcRelations.add(rel);
                continue;
            }
            SWDlogger.warn("SWD-Fehler: Zweikreis gefunden! " + rel.getChild() + " und " + rel.getParent());
        }
    }

    private void coffmanGraham() {
        HashMap<String, Integer> pi = new HashMap<String, Integer>();
        boolean b = true;
        for (String element : this.v) {
            if (!b || this.isChild.containsKey(element)) continue;
            pi.put(element, 1);
            b = false;
        }
        int i = 2;
        while (i <= this.v.size()) {
            LinkedList<String> candidates = new LinkedList<String>();
            for (String element : this.v) {
                if (pi.containsKey(element)) continue;
                boolean label = true;
                for (SWDrelation relation2 : this.pcRelations) {
                    if (!relation2.getChild().equals(element) || pi.containsKey(relation2.getParent())) continue;
                    label = false;
                }
                if (!label) continue;
                candidates.add(element);
            }
            HashMap<String, TreeSet<Integer>> pre = new HashMap<String, TreeSet<Integer>>();
            for (String element : candidates) {
                TreeSet<Integer> vmenge = new TreeSet<Integer>();
                for (SWDrelation relation3 : this.pcRelations) {
                    if (!relation3.getChild().equals(element)) continue;
                    vmenge.add((Integer)pi.get(relation3.getParent()));
                }
                pre.put(element, vmenge);
            }
            LexicographicCompare compare = new LexicographicCompare(pre);
            TreeSet<String> sorter = new TreeSet<String>(compare);
            for (String element : candidates) {
                sorter.add(element);
            }
            pi.put((String)sorter.first(), i);
            ++i;
        }
        this.layers.add(new LinkedList());
        int k = 0;
        HashMap<String, String> u = new HashMap<String, String>();
        while (u.size() != this.v.size()) {
            if (this.currentSize > this.breaksize) {
                ++k;
                this.layers.add(new LinkedList());
                this.currentSize = 20;
            }
            LinkedList<String> candidates = new LinkedList<String>();
            HashMap<String, Boolean> contentOfDeeperLayers = new HashMap<String, Boolean>();
            int i2 = 0;
            while (i2 < this.layers.size() - 1) {
                for (String s2 : this.layers.get(i2)) {
                    contentOfDeeperLayers.put(s2, true);
                }
                ++i2;
            }
            for (String element2 : this.v) {
                if (u.containsKey(element2)) continue;
                boolean candidate = true;
                for (SWDrelation rel2 : this.pcRelations) {
                    if (!rel2.getParent().equals(element2) || contentOfDeeperLayers.containsKey(rel2.getChild())) continue;
                    candidate = false;
                }
                if (!candidate) continue;
                candidates.add(element2);
            }
            if (candidates.size() == 0) {
                ++k;
                this.layers.add(new LinkedList());
                this.currentSize = 20;
                continue;
            }
            HashMap<Integer, String> f = new HashMap<Integer, String>();
            TreeSet<Integer> t = new TreeSet<Integer>();
            for (String cand : candidates) {
                f.put((Integer)pi.get(cand), cand);
                t.add((Integer)pi.get(cand));
            }
            String the_u = (String)f.get(t.last());
            boolean allIncluded = true;
            HashMap<String, String> lowerLayers = new HashMap<String, String>();
            int z = this.layers.size();
            int i3 = 0;
            while (i3 < z) {
                for (String element3 : this.layers.get(i3)) {
                    lowerLayers.put(element3, " ");
                }
                ++i3;
            }
            for (SWDrelation rel3 : this.pcRelations) {
                if (!allIncluded || !rel3.getParent().equals(the_u) || lowerLayers.containsKey(rel3.getChild())) continue;
                allIncluded = false;
            }
            if (allIncluded) {
                this.layers.getLast().add(the_u);
                this.currentSize += the_u.length() * 10;
            } else {
                ++k;
                LinkedList<String> newl = new LinkedList<String>();
                newl.add(the_u);
                this.layers.add(newl);
                this.currentSize += the_u.length() * 10;
            }
            u.put(the_u, " ");
        }
        LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
        Iterator<LinkedList<String>> iter = this.layers.descendingIterator();
        while (iter.hasNext()) {
            LinkedList<String> r = iter.next();
            result.add(r);
        }
        this.layers = result;
    }

    private void insertDummies() {
        HashMap<String, Boolean> dontCopyBack = new HashMap<String, Boolean>();
        LinkedList<SWDrelation> localRelations = new LinkedList<SWDrelation>();
        HashMap<Object, Boolean> content = new HashMap<Object, Boolean>();
        int dummyCounter = 0;
        for (SWDrelation rel : this.pcRelations) {
            localRelations.add(rel);
        }
        for (LinkedList einzel : this.layers) {
            for (Object node : einzel) {
                content.put(node, false);
            }
        }
        LinkedList<String> f = this.layers.get(0);
        for (String s : f) {
            content.remove(s);
        }
        int i = 0;
        while (i < this.layers.size() - 1) {
            LinkedList<String> fi = this.layers.get(i + 1);
            for (String s : fi) {
                content.remove(s);
            }
            HashMap<String, Boolean> lc = new HashMap<String, Boolean>();
            f = this.layers.get(i);
            for (String s : f) {
                lc.put(s, false);
            }
            LinkedList<SWDrelation> toInsert = new LinkedList<SWDrelation>();
            for (SWDrelation rel : localRelations) {
                if (!lc.containsKey(rel.getParent()) || !content.containsKey(rel.getChild())) continue;
                String dummyName = "!_Dummy" + Integer.toString(dummyCounter);
                this.layers.get(i + 1).add(dummyName);
                ++dummyCounter;
                toInsert.add(new SWDrelation(rel.getParent(), dummyName));
                toInsert.add(new SWDrelation(dummyName, rel.getChild()));
                if (!rel.getParent().startsWith("!_Dummy")) continue;
                dontCopyBack.put(rel.getParent(), false);
            }
            localRelations.addAll(toInsert);
            LinkedList<SWDrelation> result = new LinkedList<SWDrelation>();
            for (SWDrelation rel : localRelations) {
                if (dontCopyBack.containsKey(rel.getParent()) && !rel.getChild().startsWith("!_Dummy")) continue;
                result.add(rel);
            }
            this.pcRelations = result;
            ++i;
        }
    }

    private void medianCrossingReduction() {
        int i = this.layers.size() - 1;
        while (i > 0) {
            this.twoLayerMedianCrossingReduction(i, i - 1);
            --i;
        }
        i = 0;
        while (i < this.layers.size() - 1) {
            this.twoLayerMedianCrossingReduction(i, i + 1);
            ++i;
        }
    }

    private void twoLayerMedianCrossingReduction(int i, int i2) {
        LinkedList<String> lone = this.layers.get(i);
        LinkedList<String> ltosort = this.layers.get(i2);
        HashMap<String, Integer> nameToNumber = new HashMap<String, Integer>();
        int y = 0;
        while (y < lone.size()) {
            nameToNumber.put(lone.get(y), y);
            ++y;
        }
        HashMap<String,TreeSet<Integer>> row = new HashMap<String,TreeSet<Integer>>();
        for (String node : ltosort) {
            row.put(node, new TreeSet<Integer>());
        }
        for (SWDrelation rel : this.pcRelations) {
            String parent = rel.getParent();
            String child = rel.getChild();
            if (nameToNumber.containsKey(child) && row.containsKey(parent)) {
                ((TreeSet<Integer>)row.get(parent)).add((Integer)nameToNumber.get(child));
            }
            if (!nameToNumber.containsKey(parent) || !row.containsKey(child)) continue;
            ((TreeSet<Integer>)row.get(child)).add((Integer)nameToNumber.get(parent));
        }
        for (String name : row.keySet()) {
            if (((TreeSet<Integer>)row.get(name)).size() != 0) continue;
            TreeSet<Integer> insert = new TreeSet<Integer>();
            insert.add(1000);
            row.put(name, insert);
        }
        HashMap<Double, String> medianToName = new HashMap<Double, String>();
        HashMap<String, Boolean> h_values = new HashMap<String, Boolean>();
        for (String name : row.keySet()) {
            TreeSet<Integer> values = (TreeSet<Integer>)row.get(name);
            double med = 0.5 * (double)values.size();
            med += 0.5;
            int last = 0;
            int h = 0;
            boolean cont = true;
            for (Integer ui : values) {
                double value;
                if (!cont) continue;
                if (med == (double)(++h)) {
                    value = ui.intValue();
                    while (h_values.containsKey(Double.toString(value))) {
                        value += 0.001;
                    }
                    medianToName.put(value, name);
                    h_values.put(Double.toString(value), false);
                    cont = false;
                }
                if (med < (double)h) {
                    value = ((double)ui.intValue() + (double)last) / 2.0;
                    while (h_values.containsKey(Double.toString(value))) {
                        value += 0.001;
                    }
                    medianToName.put(value, name);
                    h_values.put(Double.toString(value), false);
                    cont = false;
                }
                last = ui;
            }
        }
        LinkedList<String> result = new LinkedList<String>();
        TreeSet sorter = new TreeSet();
        sorter.addAll(medianToName.keySet());
        Iterator iterator = sorter.iterator();
        while (iterator.hasNext()) {
            double d = (Double)iterator.next();
            result.add((String)medianToName.get(d));
        }
        this.layers.set(i2, result);
    }

    private void removeDummies() {
        LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
        for (LinkedList<String> einzel : this.layers) {
            LinkedList t = new LinkedList();
            for (String node : einzel) {
                if (node.startsWith("!_Dummy")) continue;
                t.add(node);
            }
            result.add(t);
        }
        this.layers = result;
        LinkedList<SWDrelation> result2 = new LinkedList<SWDrelation>();
        for (SWDrelation rel : this.pcRelations) {
            if (rel.getParent().startsWith("!_Dummy") || rel.getChild().startsWith("!_Dummy")) continue;
            result2.add(rel);
        }
        this.pcRelations = result2;
    }
}
