package SWDgui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintStream;
import javax.swing.SwingUtilities;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;

public class SWDCellMouseListener
implements MouseListener {
    JGraph graph = null;

    public SWDCellMouseListener(JGraph j) {
        this.graph = j;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (e.getClickCount() == 1) {
                System.out.println("Ein Klick genuegt!");
                DefaultGraphCell cell = (DefaultGraphCell)this.graph.getSelectionCell();
                String name = (String)cell.getUserObject();
                System.out.println(name);
            } else {
                System.out.println("Mehr Klicks!");
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }
}
