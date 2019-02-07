package SWDgui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class LexicographicCompare
implements Comparator {
    HashMap<String, TreeSet<Integer>> pre;

    public LexicographicCompare(HashMap<String, TreeSet<Integer>> p) {
        this.pre = p;
    }

    public int compare(Object o, Object t) {
        int i;
        TreeSet<Integer> one = this.pre.get((String)o);
        TreeSet<Integer> two = this.pre.get((String)t);
        if (one.size() == 0) {
            if (two.size() == 0) {
                return 0;
            }
            return -1;
        }
        if (two.size() == 0) {
            return 1;
        }
        if (one.last() < two.last()) {
            return -1;
        }
        if (two.last() < one.last()) {
            return 1;
        }
        int max = one.last();
        HashMap<String, TreeSet<Integer>> np = new HashMap<String, TreeSet<Integer>>();
        TreeSet<Integer> orev = new TreeSet<Integer>();
        TreeSet<Integer> trev = new TreeSet<Integer>();
        Iterator<Integer> iterator = one.iterator();
        while (iterator.hasNext()) {
            i = iterator.next();
            if (i == max) continue;
            orev.add(i);
        }
        iterator = two.iterator();
        while (iterator.hasNext()) {
            i = iterator.next();
            if (i == max) continue;
            trev.add(i);
        }
        np.put((String)o, orev);
        np.put((String)t, trev);
        LexicographicCompare compare = new LexicographicCompare(np);
        return compare.compare(o, t);
    }
}
