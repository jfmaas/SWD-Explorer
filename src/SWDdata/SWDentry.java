package SWDdata;

import SWDio.SWDlogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SWDentry {
    static Pattern ppnPattern = Pattern.compile("PPN:\\s\\d\\d*\\w?");
    boolean b_combined = false;
    String s_content = "";
    private String s_ppn;
    private ArrayList<String> l_namen = new ArrayList();
    private ArrayList<String> l_typen = new ArrayList();
    private HashMap<String, Boolean> h_oberbegriff = new HashMap();
    private LinkedList<String> l_verwandte = new LinkedList();
    private String changeCommentary = null;
    private boolean b_code = false;
    private boolean b_ppn = false;
    private boolean b_name = false;

    SWDentry(LinkedList<String> namen, boolean zusammengesetzt) {
        this.s_content = "Kein Datensatz vorhanden. Zusammengesetzter Begriff.";
        this.b_combined = true;
        for (String name : namen) {
            this.s_content = this.s_content.concat("\n(Alternativer) Name: " + name);
        }
        HashMap<String, Boolean> parents = new HashMap<String, Boolean>();
        for (String name : namen) {
            String[] result;
            name = name.trim();
            this.l_namen.add(name);
            String[] arrstring = result = name.split("/");
            int n = arrstring.length;
            int n2 = 0;
            while (n2 < n) {
                String s = arrstring[n2];
                s = s.trim();
                parents.put(s, false);
                ++n2;
            }
        }
        for (String parent : parents.keySet()) {
            this.h_oberbegriff.put(parent, false);
            this.s_content = this.s_content.concat("\nVirtueller Oberbegriff: " + parent);
        }
        this.s_ppn = "000000000";
    }

    SWDentry(LinkedList<String> input) {
        Iterator x = input.listIterator();
        while (x.hasNext()) {
            String number = "";
            String a = "";
            boolean b_rest = true;
            String t = (String)x.next();
            if (t.startsWith("SET:")) {
                this.process_ppn(t);
                b_rest = false;
            } else {
                this.s_content = this.s_content.concat(t);
                this.s_content = this.s_content.concat("\n");
                number = this.getNumber(t);
                a = this.getRest(t);
            }
            if (number.equals("005")) {
                this.process_code(a);
                b_rest = false;
            }
            if (number.equals("020")) {
                b_rest = false;
            }
            if (number.equals("021")) {
                b_rest = false;
            }
            if (number.equals("800")) {
                this.process_name(a);
                b_rest = false;
            }
            if (number.equals("801")) {
                this.process_multiple_names(a);
                b_rest = false;
            }
            if (number.equals("802")) {
                this.process_multiple_names(a);
                b_rest = false;
            }
            if (number.equals("803")) {
                this.process_multiple_names(a);
                b_rest = false;
            }
            if (number.equals("804")) {
                this.process_multiple_names(a);
                b_rest = false;
            }
            if (number.equals("805")) {
                this.process_multiple_names(a);
                b_rest = false;
            }
            if (number.equals("808")) {
                this.process_desc(a);
                b_rest = false;
            }
            if (number.equals("810")) {
                this.process_cate(a);
                b_rest = false;
            }
            if (number.equals("811")) {
                this.process_origin(a);
                b_rest = false;
            }
            if (number.equals("820")) {
                this.process_altname(a);
                b_rest = false;
            }
            if (number.equals("830")) {
                this.process_altname(a);
                b_rest = false;
            }
            if (number.equals("845")) {
                this.process_para(a);
                b_rest = false;
            }
            if (number.equals("850")) {
                this.process_para(a);
                b_rest = false;
            }
            if (number.equals("860")) {
                this.process_rel(a);
                b_rest = false;
            }
            if (number.equals("900")) {
                b_rest = false;
            }
            if (number.equals("950")) {
                this.process_changeComment(a);
                b_rest = false;
            }
            if (number.equals("0")) {
                b_rest = false;
            }
            if (!b_rest) continue;
            String name = " ";
            if (this.l_namen.size() > 0) {
                name = name.concat(this.l_namen.get(0));
            }
            SWDlogger.warn("Nicht beruecksichtigter Zahlcode: " + number + name);
        }
        this.checkContent();
    }

    public String getName() {
        return this.l_namen.get(0);
    }

    public LinkedList<String> getAllNames() {
        LinkedList<String> result = new LinkedList<String>();
        for (String i : this.l_namen) {
            result.add(i);
        }
        return result;
    }

    public String getPPN() {
        return this.s_ppn;
    }

    public boolean getCombined() {
        return this.b_combined;
    }

    public LinkedList<String> getParents() {
        LinkedList<String> result = new LinkedList<String>();
        for (String t : this.h_oberbegriff.keySet()) {
            result.add(t);
        }
        return result;
    }

    public String getContent() {
        return this.s_content;
    }

    private void process_ppn(String a) {
        String result = "";
        Matcher m = ppnPattern.matcher(a);
        if (m.find()) {
            result = m.group(0).substring(4);
        }
        this.s_ppn = result = result.trim();
        this.b_ppn = true;
    }

    private void process_code(String a) {
        this.b_code = true;
    }

    private void process_name(String a) {
        this.l_typen.add(0, this.getIdentifyer(a));
        this.l_namen.add(0, this.stripIdentifyer(a));
        this.b_name = true;
    }

    private void process_multiple_names(String a) {
        String name = this.l_namen.get(0);
        name = name.concat(" / " + this.stripIdentifyer(a));
        this.l_namen.set(0, name);
    }

    private void process_desc(String a) {
    }

    private void process_cate(String a) {
    }

    private void process_origin(String a) {
    }

    private void process_altname(String a) {
        if (!this.getIdentifyer(a).equals("v")) {
            this.l_typen.add(this.getIdentifyer(a));
            this.l_namen.add(this.stripIdentifyer(a));
        }
    }

    private void process_para(String a) {
        String s_input = this.stripIdentifyer(a);
        s_input = s_input.trim();
        this.h_oberbegriff.put(s_input, true);
    }

    private void process_rel(String a) {
        String s_result = this.stripIdentifyer(a);
        this.l_verwandte.add(s_result);
    }

    private void process_changeComment(String a) {
        this.changeCommentary = a;
    }

    private void checkContent() {
        if (!this.b_code) {
            SWDlogger.warn("Kritischer Fehler: Code: NN.");
        }
        if (!this.b_name) {
            SWDlogger.error("Kritischer Fehler: Name: NN.");
        }
        if (!this.b_ppn) {
            SWDlogger.warn("PPN: NN. bei " + this.l_namen.get(0));
        }
    }

    private String getNumber(String a) {
        if ((a = a.trim()).isEmpty()) {
            return "0";
        }
        String[] result = a.split("\\s", 2);
        return result[0];
    }

    private String getRest(String a) {
        if ((a = a.trim()).isEmpty()) {
            return "";
        }
        String[] result = a.split("\\s", 2);
        return result[1];
    }

    private String getIdentifyer(String x) {
        char[] type = x.substring(0, 3).toCharArray();
        String s_result = "";
        if (type[0] == '|' && type[2] == '|') {
            s_result = String.valueOf(type[1]);
        }
        return s_result;
    }

    private String stripIdentifyer(String x) {
        char[] type = x.substring(0, 3).toCharArray();
        String s_result = x;
        if (type[0] == '|' && type[2] == '|') {
            s_result = x.substring(3);
        }
        s_result = s_result.trim();
        return s_result;
    }

    public String getChangeCommentary() {
        return this.changeCommentary;
    }
}
