
package grapheditor;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * An interface for menu items that are interested in knowning the currently selected edge and
 * its visualization component context.  Used with PopupVertexEdgeMenuMousePlugin.
  */
public interface EdgeMenuListener<E> {

     void setEdgeAndView(E e, VisualizationViewer visView);
    
}