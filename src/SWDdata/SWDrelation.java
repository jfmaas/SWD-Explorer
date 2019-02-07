package SWDdata;

public class SWDrelation {
    String s_parent;
    String s_child;

    public SWDrelation(String a, String b) {
        this.s_parent = a;
        this.s_child = b;
    }

    public String getChild() {
        return this.s_child;
    }

    public String getParent() {
        return this.s_parent;
    }
}
