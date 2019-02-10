package SWDgui;

import SWDdata.SWDdatabase;
import SWDdata.SWDrelation;
import SWDgui.Sugiyama;
import SWDio.SWDlogger;
import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

public class SWDvis {
    LinkedList<SWDrelation> pcRelations = new LinkedList();
    HashMap<String, String> toShow = new HashMap();
    HashMap<String, String> isChild = new HashMap();
    HashMap<String, String> colorOf = new HashMap();
    int breaksize = 400;
    SWDdatabase database = null;

    public void setDatabase(SWDdatabase d) {
        this.database = d;
    }

    public void setBreaksize(int i) {
        this.breaksize = i;
    }

    public JScrollPane getGraphScrollPane() {
        this.cleanup();
        DefaultGraphModel model = new DefaultGraphModel();
        GraphLayoutCache view = new GraphLayoutCache((GraphModel)model, (CellViewFactory)new DefaultCellViewFactory());
        JGraph graph = new JGraph((GraphModel)model, view);
        graph.setAutoResizeGraph(true);
        LinkedList<LinkedList<String>> layers = this.getLayers();
        HashMap<String, Integer> NameToPos = this.getNumOrder(layers);
        LinkedList<DefaultGraphCell> l_cells = this.createRectangles(layers);
        int i_edgeCount = 0;
        for (SWDrelation i : this.pcRelations) {
            String s_child = i.getChild();
            String s_parent = i.getParent();
            if (!NameToPos.containsKey(s_child) || !NameToPos.containsKey(s_parent)) continue;
            ++i_edgeCount;
        }
        DefaultGraphCell[] cells = new DefaultGraphCell[l_cells.size() + i_edgeCount];
        Iterator<DefaultGraphCell> iter = l_cells.iterator();
        int c = 0;
        while (iter.hasNext()) {
            cells[c] = iter.next();
            ++c;
        }
        int k = l_cells.size();
        for (SWDrelation i : this.pcRelations) {
            String s_child = i.getChild();
            String s_parent = i.getParent();
            if (!NameToPos.containsKey(s_child) || !NameToPos.containsKey(s_parent)) continue;
            int i_source = NameToPos.get(s_parent);
            int i_target = NameToPos.get(s_child);
            DefaultEdge edge = new DefaultEdge();
            edge.setSource((DefaultGraphCell)cells[i_source].getChildAt(0));
            edge.setTarget((DefaultGraphCell)cells[i_target].getChildAt(0));
            cells[k] = edge;
            int arrow = 1;
            GraphConstants.setLineEnd((Map)edge.getAttributes(), (int)arrow);
            GraphConstants.setEndFill((Map)edge.getAttributes(), (boolean)true);
            ++k;
        }
        graph.getGraphLayoutCache().insert(cells);
        JScrollPane scroller = new JScrollPane((Component)graph);
        scroller.setWheelScrollingEnabled(true);
        return scroller;
    }

    private LinkedList<LinkedList<String>> splitLayers(LinkedList<LinkedList<String>> ebenen) {
        LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
        HashMap<String, Integer> nameToChildren = new HashMap<String, Integer>();
        for (String name : this.toShow.keySet()) {
            nameToChildren.put(name, new Integer(0));
        }
        int max = 0;
        for (SWDrelation r : this.pcRelations) {
            String child = r.getChild();
            String parent = r.getParent();
            if (!this.toShow.containsKey(child) || !this.toShow.containsKey(parent)) continue;
            nameToChildren.put(parent, (Integer)nameToChildren.get(parent) + 1);
            if (max >= (Integer)nameToChildren.get(parent)) continue;
            ++max;
        }
        LinkedList res = new LinkedList();
        for (LinkedList<String> ebene : ebenen) {
            ArrayList<LinkedList<String>> w = new ArrayList<LinkedList<String>>(max);
            int t = 0;
            while (t < max + 1) {
                w.add(new LinkedList<String>());
                ++t;
            }
            for (String name : ebene) {
                LinkedList<String> temp = (LinkedList<String> )w.get((Integer)nameToChildren.get(name));
                temp.add(name);
                w.set(nameToChildren.get(name), temp);
            }
            LinkedList<String> o = new LinkedList<String>();
            for (LinkedList<String> d : w) {
                for (String name : d) {
                    o.add(name);
                }
            }
            res.add(o);
        }
        ebenen = res;
        for (LinkedList<String> ebene : ebenen) {
            LinkedList<String> i = new LinkedList<String>();
            int space = 20;
            for (String entry : ebene) {
                space += entry.length() * 10 + 20;
                i.add(entry);
            }
            result.add(i);
        }
        return result;
    }

    private LinkedList<DefaultGraphCell> createRectangles(LinkedList<LinkedList<String>> layers) {
        int offset = 20;
        LinkedList<DefaultGraphCell> result = new LinkedList<DefaultGraphCell>();
        for (LinkedList<String> layer : layers) {
            LinkedList<DefaultGraphCell> l_temp = this.calculateRecLayer(layer, offset);
            Iterator<DefaultGraphCell> c = l_temp.iterator();
            while (c.hasNext()) {
                result.add(c.next());
            }
            offset += 60;
        }
        return result;
    }

    private LinkedList<DefaultGraphCell> calculateRecLayer(LinkedList<String> layer, int offset) {
        LinkedList<DefaultGraphCell> result = new LinkedList<DefaultGraphCell>();
        int distance = 20;
        if (this.database != null) {
            LinkedList<String> t = new LinkedList<String>();
            for (String content : layer) {
                content = String.valueOf(content) + " (" + this.database.getParentCount(content) + "/" + this.database.getChildrenCount(content) + ")";
                t.add(content);
            }
            layer = t;
        }
        for (String content : layer) {
            DefaultGraphCell cell = new DefaultGraphCell((Object)content);
            GraphConstants.setBounds((Map)cell.getAttributes(), (Rectangle2D)new Rectangle2D.Double(distance, offset, content.length() * 10, 20.0));
            if (this.colorOf.get(content.replaceFirst(" \\(.*\\)", "")) == null) {
                GraphConstants.setGradientColor((Map)cell.getAttributes(), (Color)Color.orange);
            } else if (this.colorOf.get(content.replaceFirst(" \\(.*\\)", "")).equals("red")) {
                GraphConstants.setGradientColor((Map)cell.getAttributes(), (Color)Color.red);
            } else {
                GraphConstants.setGradientColor((Map)cell.getAttributes(), (Color)Color.green);
            }
            GraphConstants.setOpaque((Map)cell.getAttributes(), (boolean)true);
            DefaultPort port = new DefaultPort();
            cell.add((MutableTreeNode)port);
            result.add(cell);
            distance += content.length() * 10;
            distance += 20;
        }
        return result;
    }

    private LinkedList<LinkedList<String>> getLayers() {
        LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
        LinkedList<SWDrelation> temp = this.pcRelations;
        this.pcRelations = new LinkedList();
        LinkedList<SWDrelation> autoZ = new LinkedList<SWDrelation>();
        for (SWDrelation rel : temp) {
            if (rel.getChild().equals(rel.getParent())) {
                autoZ.add(rel);
                SWDlogger.warn("SWD-Fehler: Element " + rel.getParent() + " ist autozyklisch!");
                continue;
            }
            this.pcRelations.add(rel);
        }
        temp = null;
        LinkedList<LinkedList<String>> subgraphs = this.getSubgraphs();
        for (LinkedList<String> subgraph : subgraphs) {
            if (subgraph.size() > 0) {
                result.addAll(this.align(subgraph));
                continue;
            }
            result.add(subgraph);
        }
        for (SWDrelation x : autoZ) {
            this.pcRelations.add(x);
        }
        return result;
    }

    private LinkedList<LinkedList<String>> align(LinkedList<String> part) {
        if (part.size() == 1) {
            LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
            result.add(part);
            return result;
        }
        Sugiyama t = new Sugiyama(this.breaksize);
        return t.performSugiyamaLayering(part, this.pcRelations, this.isChild);
    }

    private LinkedList<LinkedList<String>> getSubgraphs() {
        LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
        LinkedList<HashMap> temp = new LinkedList<HashMap>();
        for (String name : this.toShow.keySet()) {
            HashMap<String, String> tmp = new HashMap<String, String>();
            tmp.put(name, " ");
            temp.add(tmp);
        }
        for (SWDrelation relation : this.pcRelations) {
            String child = relation.getChild();
            String parent = relation.getParent();
            if (!this.toShow.containsKey(child) || !this.toShow.containsKey(parent)) continue;
            int i = 0;
            int r1 = 0;
            int r2 = 0;
            int a = 0;
            for (HashMap<String, String> map : temp) {
                if (map.containsKey(child)) {
                    r1 = i;
                    ++a;
                } else if (map.containsKey(parent)) {
                    r2 = i;
                    ++a;
                }
                ++i;
            }
            // TODO: Korrekte Bedingung in folgender Zeile?
            if (a <= 1) continue;
            HashMap<String, String> t1 = (HashMap<String, String>) temp.get(r1);
            HashMap<String, String> t2 = (HashMap<String, String>) temp.get(r2);
            if (r2 < r1) {
                temp.remove(r1);
                temp.remove(r2);
            } else {
                temp.remove(r2);
                temp.remove(r1);
            }
            for (String t : t2.keySet()) {
                t1.put(t, " ");
            }
            temp.add(t1);
        }
        LinkedList<String> one = new LinkedList<String>();
        for (HashMap<String, String> map : temp) {
            if (map.size() != 1) continue;
            for (String z : map.keySet()) {
                one.add(z);
            }
        }
        if (one.size() > 0) {
            result.add(one);
            result.add(new LinkedList());
        }
        for (HashMap<String, String> map : temp) {
            LinkedList<String> more = new LinkedList<String>();
            if (map.size() <= 1) continue;
            for (String z : map.keySet()) {
                more.add(z);
            }
            result.add(more);
            result.add(new LinkedList());
            more = new LinkedList();
        }
        return result;
    }

    private HashMap<String, Integer> getNumOrder(LinkedList<LinkedList<String>> layers) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        int i = 0;
        for (LinkedList<String> z : layers) {
            for (String name : z) {
                result.put(name, i);
                ++i;
            }
        }
        return result;
    }

    private void cleanup() {
        HashMap<String, Boolean> childParent = new HashMap<String, Boolean>();
        LinkedList<SWDrelation> temp = this.pcRelations;
        this.pcRelations = new LinkedList();
        for (SWDrelation element : temp) {
            String child = element.getChild();
            String parent = element.getParent();
            if (childParent.containsKey(String.valueOf(child) + "_!_" + parent)) continue;
            childParent.put(String.valueOf(child) + "_!_" + parent, true);
            this.pcRelations.add(element);
        }
    }

    public void setPCRelation(String a, String b) {
        SWDrelation entry = new SWDrelation(a, b);
        this.isChild.put(b, " ");
        this.pcRelations.add(entry);
    }

    public void setEntry(String a) {
        this.toShow.put(a, " ");
    }

    public void setEntry(String a, String color) {
        if (color != null) {
            this.toShow.put(a, " ");
            if (this.colorOf.get(a) == null) {
                this.colorOf.put(a, color);
            }
            if (!this.colorOf.get(a).equals("red")) {
                this.colorOf.put(a, color);
            }
        }
    }

    public static JScrollPane helloSWD() {
        DefaultGraphModel model = new DefaultGraphModel();
        GraphLayoutCache view = new GraphLayoutCache((GraphModel)model, (CellViewFactory)new DefaultCellViewFactory());
        JGraph graph = new JGraph((GraphModel)model, view);
        graph.setAutoResizeGraph(true);
        DefaultGraphCell[] cells = new DefaultGraphCell[3];
        cells[0] = new DefaultGraphCell((Object)new String("Hello"));
        GraphConstants.setBounds((Map)cells[0].getAttributes(), (Rectangle2D)new Rectangle2D.Double(20.0, 20.0, 40.0, 20.0));
        GraphConstants.setGradientColor((Map)cells[0].getAttributes(), (Color)Color.orange);
        GraphConstants.setOpaque((Map)cells[0].getAttributes(), (boolean)true);
        DefaultPort port0 = new DefaultPort();
        cells[0].add((MutableTreeNode)port0);
        cells[1] = new DefaultGraphCell((Object)new String("User"));
        GraphConstants.setBounds((Map)cells[1].getAttributes(), (Rectangle2D)new Rectangle2D.Double(140.0, 140.0, 40.0, 20.0));
        GraphConstants.setGradientColor((Map)cells[1].getAttributes(), (Color)Color.red);
        GraphConstants.setOpaque((Map)cells[1].getAttributes(), (boolean)true);
        DefaultPort port1 = new DefaultPort();
        cells[1].add((MutableTreeNode)port1);
        DefaultEdge edge = new DefaultEdge();
        edge.setSource((Object)cells[0].getChildAt(0));
        edge.setTarget((Object)cells[1].getChildAt(0));
        cells[2] = edge;
        int arrow = 1;
        GraphConstants.setLineEnd((Map)edge.getAttributes(), (int)arrow);
        GraphConstants.setEndFill((Map)edge.getAttributes(), (boolean)true);
        graph.getGraphLayoutCache().insert(cells);
        JScrollPane scroller = new JScrollPane((Component)graph);
        return scroller;
    }
}
