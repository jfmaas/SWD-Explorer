package SWDgui.requestHandlers;

import SWDgui.SWDvis;

public class ToyExample {
    SWDvis graph = null;

    public ToyExample(SWDvis g) {
        this.graph = g;
    }

    public SWDvis getGraph() {
        this.graph.setEntry("hallo");
        this.graph.setEntry("wo ist");
        this.graph.setEntry("Alice");
        this.graph.setEntry("Bob");
        this.graph.setEntry("EveTheGreat");
        this.graph.setEntry("Knister");
        this.graph.setPCRelation("wo ist", "Alice");
        this.graph.setPCRelation("wo ist", "Knister");
        this.graph.setPCRelation("hallo", "Alice");
        this.graph.setPCRelation("hallo", "Knister");
        this.graph.setPCRelation("hallo", "Bob");
        this.graph.setPCRelation("Alice", "EveTheGreat");
        this.graph.setPCRelation("EveTheGreat", "Bob");
        this.graph.setPCRelation("Knister", "Bob");
        return this.graph;
    }
}
