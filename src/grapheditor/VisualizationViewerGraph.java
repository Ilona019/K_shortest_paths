package grapheditor;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Font;
import java.util.*;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeRenderer;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import grapheditor.GraphElements.MyEdge;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javafx.scene.control.TextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import geneticalgorithm.GenerationMatrix;
import geneticalgorithm.Population;
import org.apache.commons.collections15.Predicate;

/**
 *
 * @author Илона
 */
public class VisualizationViewerGraph {

    private SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> graph;
    private GenerationMatrix matrix;
    private ArrayList<MyEdge> chEdgeList;
    private ArrayList<ArrayList<GraphElements.MyEdge>> paintedEdgeslist;
    private int numColor;
    private VisualizationViewer<GraphElements.MyVertex, GraphElements.MyEdge> vv;
    private Layout<GraphElements.MyVertex, GraphElements.MyEdge> layout;
    private JButton btnDeleteParalEdges;
    private JButton btnClear;
    private JButton btnRandomGraph;
    final JFrame frame;
    private TextField s;
    private TextField t;
    private Population population;

    public VisualizationViewerGraph(TextField s, TextField t) {
        this.matrix = null;
        this.population = null;
        this.s = s;
        this.t = t;
        graph = new SparseMultigraph<>();
        frame = new JFrame("Draw graph");
        layout = new KKLayout(graph);
        layout.setSize(new Dimension(1000, 1000));
        vv = new VisualizationViewer<>(layout);

        settingsVisualizationGraph();

        EditingModalGraphMouse graphMouse = createGraphMouse();

        PopupVertexEdgeMenuMousePlugin myPlugin = new PopupVertexEdgeMenuMousePlugin();

        createPopupMenu(myPlugin);

        graphMouse.remove(graphMouse.getPopupEditingPlugin());// Removes the existing popup editing plugin

        graphMouse.add(myPlugin);

        vv.setGraphMouse(graphMouse);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);

        repaintAfterUpdates();

        JMenuBar menuBar = createMenuBar(graphMouse);

        createButtons(menuBar);

        btnRandomGraph.addActionListener((ActionEvent eventRandom) -> {
            clearGraph();

            JFrame frameCountVertex = new JFrame("Enter");
            JPanel panel = new JPanel(new FlowLayout());
            JLabel label = new JLabel("Enter count vertex: ");
            JTextField textCountVertex = new JTextField(10);
            JButton btnCountVertex = new JButton("Ok");
            panel.add(label);
            panel.add(textCountVertex);
            panel.add(btnCountVertex);
            frameCountVertex.getContentPane().add(panel);
            frameCountVertex.setPreferredSize(new Dimension(200, 100));
            frameCountVertex.setLocation(80, 100);
            frameCountVertex.pack();
            frameCountVertex.setVisible(true);

            btnCountVertex.addActionListener((ActionEvent e1) -> {
                String inputStr = "";
                inputStr = textCountVertex.getText();
                if (!isPositiveNumber(inputStr)) {
                    JOptionPane.showMessageDialog(panel, " You incorrectly input number vertex! It is  positive, integer number.");
                } else {
                    frameCountVertex.setVisible(false);
                    matrix = new GenerationMatrix(Integer.parseInt(inputStr), s.getText(), t.getText());
                    graph = matrix.getGraf();
                    layout.setGraph(graph);
                    matrix.printMatrix();
                    frame.repaint();
                }
            });
        });

        btnClear.addActionListener((ActionEvent eventClear) -> {
            clearGraph();
        });

        btnDeleteParalEdges.addActionListener((ActionEvent eventDeleteEdges) -> {
            if (graph != null) {
                deleteParalEdges();
                for (GraphElements.MyEdge e : graph.getEdges()) {
                    if (e.getFlagPaint() == 1) {
                        e.setFlagPaint(0);
                    }
                }
                chEdgeList = null;
                paintedEdgeslist = null;
                frame.repaint();
            }
        });

        frame.setJMenuBar(menuBar);
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING); // Start off in editing mode
        frame.pack();
        frame.setVisible(true);

    }

    // Add some popup menus for the edges and vertices to our mouse plugin.
    private void createPopupMenu(PopupVertexEdgeMenuMousePlugin myPlugin) {
        JPopupMenu edgeMenu = new MyMouseMenus.EdgeMenu(frame);
        JPopupMenu vertexMenu = new MyMouseMenus.VertexMenu();
        myPlugin.setEdgePopup(edgeMenu);
        myPlugin.setVertexPopup(vertexMenu);
    }

    private EditingModalGraphMouse createGraphMouse() {
        return new EditingModalGraphMouse(vv.getRenderContext(),
                GraphElements.MyVertexFactory.getInstance(),
                GraphElements.MyEdgeFactory.getInstance());
    }

    private void settingsVisualizationGraph() {
        vv.setBackground(Color.WHITE);
        vv.setPreferredSize(new Dimension(800, 650));
        // Show vertex and edge labels
        vv.getRenderContext().setLabelOffset(15);//смешение подписи ребра, чтобы не пересекались
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);//метку на вершине расположили по центру
        //Add function to update color and stroke size of edges on pressing the run algorithm button
        vv.getRenderContext().setEdgeDrawPaintTransformer(new MyEdgePaintFunction());
        vv.getRenderContext().setEdgeStrokeTransformer(new MyEdgeStrokeFunction());
        vv.getRenderContext().setVertexFillPaintTransformer(new MyVertexPaintFunction());
        vv.getRenderContext().setVertexStrokeTransformer(new MyVertexStrokeTransformer());
        vv.getRenderContext().setVertexFontTransformer(new MyVertexFont());
        vv.getRenderContext().setEdgeFontTransformer(new MyEdgesFont());
        vv.getRenderContext().setEdgeLabelTransformer(new EdgeLabelTransformer());
        vv.getRenderContext().setVertexShapeTransformer(new MySizeVertex());
        improvePerformance(vv);
    }

    private void repaintAfterUpdates() {
        vv.addPostRenderPaintable(new VisualizationViewer.Paintable() {

            @Override
            public boolean useTransform() {
                return true;
            }

            @Override
            public void paint(Graphics graph) {

                if (paintedEdgeslist == null) {
                    return;
                }

                numColor = 0;
                GraphElements.MyEdge edge;
                for (int j = 0; j < paintedEdgeslist.size(); j++) {
                    chEdgeList = new ArrayList<>(paintedEdgeslist.get(j));
                    for (int i = 0; i < chEdgeList.size(); i++) {
                        Iterator<GraphElements.MyEdge> iteratorEdge = chEdgeList.iterator();
                        while (iteratorEdge.hasNext()) {
                            edge = iteratorEdge.next();
                            Renderer<GraphElements.MyVertex, GraphElements.MyEdge> renderer = vv.getRenderer();
                            renderer.renderEdge(
                                    vv.getRenderContext(),
                                    layout,
                                    edge);
                        }
                    }
                    numColor++;
                }
            }
        });

    }

    // a menu for changing mouse modes
    private JMenuBar createMenuBar(EditingModalGraphMouse graphMouse) {
        JMenuBar menuBar = new JMenuBar();
        JMenu modeMenu = graphMouse.getModeMenu();
        modeMenu.setText("Mouse Mode");
        modeMenu.setIcon(null);
        modeMenu.setPreferredSize(new Dimension(80, 20));
        menuBar.add(modeMenu);
        return menuBar;
    }

    private void createButtons(JMenuBar menuBar) {
        btnRandomGraph = new JButton("Draw random graph");
        menuBar.add(btnRandomGraph);
        btnDeleteParalEdges = new JButton("Delete Paral Edge");
        menuBar.add(btnDeleteParalEdges);
        btnClear = new JButton("Clear");
        menuBar.add(btnClear);

    }

    private boolean isPositiveNumber(String text) {
        if (!text.matches("[\\+]?[1-9][0-9]*")) {
            return false;
        }
        return true;
    }

    public SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> getGraph() {
        return graph;
    }

    public Layout getLayout() {
        return layout;
    }

    public ArrayList<ArrayList<GraphElements.MyEdge>> getPaintedEdgeslist() {
        return paintedEdgeslist;
    }

    public ArrayList<MyEdge> getChEdgeList() {
        return chEdgeList;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public void setPaintedEdgeslist(ArrayList<ArrayList<GraphElements.MyEdge>> paintedEdgeslist) {
        this.paintedEdgeslist = paintedEdgeslist;
    }

    public void setChEdgeList(ArrayList<GraphElements.MyEdge> chEdgeList) {
        this.chEdgeList = chEdgeList;
    }

    public void repainFrame() {
        frame.repaint();
    }

    /**
     * *
     * isEdgecontainsInPath checks if edge is contained in chEdgesList
     *
     */
    public void addParalEdges() {
        chEdgeList = new ArrayList<>();
        int numEdge = 0;
        paintedEdgeslist = new ArrayList<>();//список списков для раскрашивания ребер каждого пути в определённый цвет
        //По всевозможным найденым путям пройтись
        ArrayList<GraphElements.MyEdge> copyLayout = new ArrayList<GraphElements.MyEdge>(layout.getGraph().getEdges());//скопировала массив вершин без добавленные ребер
        for (int i = 0; i < population.size(); i++) {
            ArrayList<GraphElements.MyEdge> path = new ArrayList<>();
            paintedEdgeslist.add(path);
            chEdgeList = population.getAtIndex(i).getEdgeList();//получить список ребер.
            // for all edges, paint edges that are in minimum path 
            for (GraphElements.MyEdge e : copyLayout) {

                if (isEdgecontainsInPath(e)) {//ребро присутствует в chEdgeList
                    if (e.getFlagPaint() == 0) {//ещё не проходили по ребру, добавить это ребо для окраски в массив
                        path.add(e);
                        e.setFlagPaint(1);
                    } else {
                        Collection<GraphElements.MyVertex> masVer = new ArrayList<>();
                        masVer = graph.getIncidentVertices(e);//массив вершин, содержащих ребро
                        GraphElements.MyEdge paralEdge = new MyEdge("ParalEdge" + numEdge++, e.getWeight());
                        graph.addEdge(paralEdge, masVer);//добавить параллельное ребро
                        path.add(paralEdge);
                    }
                }
            }

        }
    }

    public void deleteParalEdges() {
        ArrayList<GraphElements.MyEdge> copyLayout = new ArrayList<GraphElements.MyEdge>(layout.getGraph().getEdges());//скопировала массив со всеми ребрами
        for (GraphElements.MyEdge e : copyLayout) {
            if (e.getName().contains("ParalEdge")) {
                graph.removeEdge(e);
            }
        }
    }

    public void clearGraph() {
        GraphElements.MyVertexFactory.setNullNodeCount();
        GraphElements.MyEdgeFactory.setNullLinkCount();
        //удалить вершины старого графа
        new ArrayList<>(graph.getVertices()).stream()
                .forEach(graph::removeVertex);
        chEdgeList = null;
        paintedEdgeslist = null;
        frame.repaint();
    }

    public boolean isEdgecontainsInPath(GraphElements.MyEdge e) {
        return chEdgeList.contains(e);
    }

    public int getNumColor() {//для раскраски ребра
        return numColor;
    }

    /**
     * *
     * MyEdgePaintFunction for updating color of edges
     */
    public class MyEdgePaintFunction implements Transformer<GraphElements.MyEdge, Paint> {

        private final Color[] palette = {new Color(255, 99, 71), new Color(20, 144, 255),
            Color.MAGENTA, Color.GREEN, Color.YELLOW, new Color(148, 0, 212),
            new Color(0, 100, 0), Color.PINK, Color.DARK_GRAY, new Color(20, 144, 255), Color.RED, new Color(123, 104, 238), new Color(255, 100, 100), Color.ORANGE, Color.BLUE};

        @Override
        public Paint transform(GraphElements.MyEdge e) {
            if (chEdgeList == null || chEdgeList.isEmpty()) {
                return Color.BLACK;
            }
            if (numColor >= palette.length) {
                numColor = 0;
            }

            if (isEdgecontainsInPath(e)) {
                return palette[getNumColor()];
            } else {
                return Color.LIGHT_GRAY;
            }
        }
    }

    public class MyVertexPaintFunction implements Transformer<GraphElements.MyVertex, Paint> {

        @Override
        public Paint transform(GraphElements.MyVertex v) {
            String strV = v.getName();
            if (strV.equals(s.getText()) || strV.equals(t.getText())) {
                return new Color(255, 69, 0);
            }
            return new Color(0, 255, 127);
        }
    }

    /**
     * *
     * MyEdgeStrokeFunction class to update Edge stroke Толщина ребра
     */
    public class MyEdgeStrokeFunction implements Transformer<GraphElements.MyEdge, Stroke> {

        protected final Stroke THIN = new BasicStroke(1);//тонкое ребро
        protected final Stroke THICK = new BasicStroke(3);//толстое ребро

        @Override
        public Stroke transform(GraphElements.MyEdge e) {
            if (chEdgeList == null || chEdgeList.isEmpty()) {
                return THIN;
            }
            if (isEdgecontainsInPath(e)) {
                return THICK;
            } else {
                return THIN;
            }
        }
    }

    public static class MyVertexStrokeTransformer implements Transformer<GraphElements.MyVertex, Stroke> {

        @Override
        public Stroke transform(GraphElements.MyVertex i) {
            return new BasicStroke(3f);
        }
    }

    public static class MyVertexFont implements Transformer<GraphElements.MyVertex, Font> {

        @Override
        public Font transform(GraphElements.MyVertex i) {
            return new java.awt.Font("Arial", Font.BOLD, 15);
        }
    }

    public static class MyEdgesFont implements Transformer<GraphElements.MyEdge, Font> {

        @Override
        public Font transform(GraphElements.MyEdge i) {
            return new java.awt.Font("Arial", Font.BOLD, 16);
        }
    }

    public class EdgeLabelTransformer implements Transformer<GraphElements.MyEdge, String> {

        @Override
        public String transform(GraphElements.MyEdge e) {
            if (e.getName().contains("ParalEdge")) {
                return "";
            } else {
                return "Weight " + e.getWeight();
            }

        }

    }

    //Изменить размер вершины
    public class MySizeVertex implements Transformer<GraphElements.MyVertex, Shape> {

        @Override
        public Shape transform(GraphElements.MyVertex i) {
            Ellipse2D circle = new Ellipse2D.Double(-14, -14, 28, 28);
            // in this case, the vertex is twice as large//в 1.5 раза больше
            if (Integer.parseInt(i.getName()) == Integer.parseInt(s.getText()) || Integer.parseInt(i.getName()) == Integer.parseInt(t.getText())) {
                return AffineTransform.getScaleInstance(1.5, 1.5).createTransformedShape(circle);
            } else {
                return circle;
            }
        }
    }

    private static <V, E> void improvePerformance(VisualizationViewer<GraphElements.MyVertex, GraphElements.MyEdge> vv) {
        // Disable anti-aliasing
        vv.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING);
        // Skip vertices that are not inside the visible area. 
        doNotPaintInvisibleVertices(vv);
    }

    // Skip all vertices that are not in the visible area. 
    // NOTE: See notes at the end of this method!
    private static <V, E> void doNotPaintInvisibleVertices(
            VisualizationViewer<V, E> vv) {
        Predicate<Context<Graph<V, E>, V>> vertexIncludePredicate
                = new Predicate<Context<Graph<V, E>, V>>() {
            Dimension size = new Dimension();

            @Override
            public boolean evaluate(Context<Graph<V, E>, V> c) {
                vv.getSize(size);
                Point2D point = vv.getGraphLayout().transform(c.element);
                Point2D transformed
                        = vv.getRenderContext().getMultiLayerTransformer()
                                .transform(point);
                if (transformed.getX() < 0 || transformed.getX() > size.width) {
                    return false;
                }
                return !(transformed.getY() < 0 || transformed.getY() > size.height);
            }
        };
        vv.getRenderContext().setVertexIncludePredicate(vertexIncludePredicate);

        // NOTE: By default, edges will NOT be included in the visualization
        // when ONE of their vertices is NOT included in the visualization.
        // This may look a bit odd when zooming and panning over the graph.
        // Calling the following method will cause the edges to be skipped
        // ONLY when BOTH their vertices are NOT included in the visualization,
        // which may look nicer and more intuitive
        doPaintEdgesAtLeastOneVertexIsVisible(vv);
    }

    // See note at end of "doNotPaintInvisibleVertices"
    private static <V, E> void doPaintEdgesAtLeastOneVertexIsVisible(
            VisualizationViewer<V, E> vv) {
        vv.getRenderer().setEdgeRenderer(new BasicEdgeRenderer<V, E>() {
            @Override
            public void paintEdge(RenderContext<V, E> rc, Layout<V, E> layout, E e) {
                GraphicsDecorator g2d = rc.getGraphicsContext();
                Graph<V, E> graph = layout.getGraph();
                if (!rc.getEdgeIncludePredicate().evaluate(
                        Context.<Graph<V, E>, E>getInstance(graph, e))) {
                    return;
                }

                Pair<V> endpoints = graph.getEndpoints(e);
                V v1 = endpoints.getFirst();
                V v2 = endpoints.getSecond();
                if (!rc.getVertexIncludePredicate().evaluate(
                        Context.<Graph<V, E>, V>getInstance(graph, v1))
                        && !rc.getVertexIncludePredicate().evaluate(
                                Context.<Graph<V, E>, V>getInstance(graph, v2))) {
                    return;
                }

                Stroke new_stroke = rc.getEdgeStrokeTransformer().transform(e);
                Stroke old_stroke = g2d.getStroke();
                if (new_stroke != null) {
                    g2d.setStroke(new_stroke);
                }

                drawSimpleEdge(rc, layout, e);

                // restore paint and stroke
                if (new_stroke != null) {
                    g2d.setStroke(old_stroke);
                }
            }
        });
    }
}

