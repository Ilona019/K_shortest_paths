
package grapheditor;

import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

/**
 * A class to implement the deletion of an edge from within a
 * PopupVertexEdgeMenuMousePlugin.
 */
public class DeleteEdgeMenuItem<E> extends JMenuItem implements EdgeMenuListener<E> {
    private E edge;
    private VisualizationViewer visComp;

    /**
     * Creates a new instance of DeleteEdgeMenuItem
     */
    public DeleteEdgeMenuItem() {
        super("Delete Edge");
        this.addActionListener(e -> {
            visComp.getPickedEdgeState().pick(edge, false);
            visComp.getGraphLayout().getGraph().removeEdge(edge);
            visComp.repaint();
        });

    }

    /**
     * Implements the EdgeMenuListener interface to update the menu item with info
     * on the currently chosen edge.
     *
     * @param edge
     * @param visComp
     */
    public void setEdgeAndView(E edge, VisualizationViewer visComp) {
        this.edge = edge;
        this.visComp = visComp;
        this.setText("Delete Edge " + edge.toString());
    }

}
