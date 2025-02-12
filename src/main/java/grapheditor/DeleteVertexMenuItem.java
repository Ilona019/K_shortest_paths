
package grapheditor;

import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 * A class to implement the deletion of a vertex from within a
 * PopupVertexEdgeMenuMousePlugin.
 */
public class DeleteVertexMenuItem<V, E> extends JMenuItem implements VertexMenuListener<V> {
    private V vertex;
    private VisualizationViewer<V, E> visComp;

    /**
     * Creates a new instance of DeleteVertexMenuItem
     */
    public DeleteVertexMenuItem() {
        super("Delete Vertex");
        this.addActionListener((ActionEvent e) -> {
            visComp.getPickedVertexState().pick(vertex, false);
            visComp.getGraphLayout().getGraph().removeVertex(vertex);
            visComp.repaint();
        });
    }

    /**
     * Implements the VertexMenuListener interface.
     *
     * @param v
     * @param visComp
     */
    @Override
    public void setVertexAndView(V v, VisualizationViewer visComp) {
        this.vertex = v;
        this.visComp = visComp;
        this.setText("Delete Vertex " + v.toString());
    }

}
