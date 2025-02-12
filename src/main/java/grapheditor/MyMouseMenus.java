
package grapheditor;

import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * A collection of classes used to assemble popup mouse menus for the custom
 * edges and vertices developed in this example.
 */
public class MyMouseMenus {

    public static class EdgeMenu extends JPopupMenu {
        // private JFrame frame; 
        public EdgeMenu(final JFrame frame) {
            super("Edge Menu");

            this.add(new DeleteEdgeMenuItem());
            this.addSeparator();
            this.add(new WeightDisplay());

            this.addSeparator();
            this.add(new EdgePropItem(frame));
        }

    }

    public static class EdgePropItem extends JMenuItem implements EdgeMenuListener<grapheditor.GraphElements.MyEdge>,
            MenuPointListener {
        GraphElements.MyEdge edge;
        VisualizationViewer<GraphElements.MyVertex, GraphElements.MyEdge> visComp;
        Point2D point;

        @Override
        public void setEdgeAndView(GraphElements.MyEdge edge, VisualizationViewer visComp) {
            this.edge = edge;
            this.visComp = visComp;
        }

        @Override
        public void setPoint(Point2D point) {
            this.point = point;
        }

        public EdgePropItem(final JFrame frame) {
            super("Edit Edge Properties...");
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    EdgePropertyDialog dialog = new EdgePropertyDialog(frame, edge, visComp);
                    dialog.setLocation((int) point.getX() + frame.getX(), (int) point.getY() + frame.getY());
                    dialog.setVisible(true);
                }

            });
        }

    }

    public static class WeightDisplay extends JMenuItem implements EdgeMenuListener<grapheditor.GraphElements.MyEdge> {
        @Override
        public void setEdgeAndView(GraphElements.MyEdge e, VisualizationViewer visComp) {
            this.setText("" + e);
        }
    }


    public static class VertexMenu extends JPopupMenu {
        public VertexMenu() {
            super("Vertex Menu");
            this.add(new DeleteVertexMenuItem());

        }
    }
}
