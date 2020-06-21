package grapheditor;


import edu.uci.ics.jung.algorithms.layout.KKLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Font;
import java.io.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.*;
import edu.uci.ics.jung.visualization.*;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeRenderer;

import javax.swing.*;

import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import grapheditor.GraphElements.MyEdge;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javafx.scene.control.TextField;

import org.apache.commons.collections15.Predicate;
import main.ValidateInput;
import org.freehep.graphicsbase.util.export.ExportDialog;


/**
 * @author Илона
 */
public class VisualizationViewerGraph {

    private SparseMultigraph<GraphElements.MyVertex, MyEdge> graph;
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
    private ArrayList<LinkedList<Integer>> listOfShortcut;
    private ValidateInput validationInput;
    private RandomGraphEditDialog currentRandomGraphEditDialog;

    public VisualizationViewerGraph(TextField s, TextField t) {
        this.matrix = null;
        listOfShortcut = new ArrayList<>();
        this.s = s;
        this.t = t;
        graph = new SparseMultigraph<>();
        frame = new JFrame("Draw graph");
        layout = new KKLayout<>(graph);
        layout.setSize(new Dimension(1800, 1000));
        vv = new VisualizationViewer<>(layout);

        settingsVisualizationGraph();
        improvePerformance(vv);

        EditingModalGraphMouse<GraphElements.MyVertex, GraphElements.MyEdge> graphMouse = createGraphMouse();

        PopupVertexEdgeMenuMousePlugin<GraphElements.MyVertex, GraphElements.MyEdge> myPlugin = new PopupVertexEdgeMenuMousePlugin<>();

        createPopupMenu(myPlugin);

        graphMouse.remove(graphMouse.getPopupEditingPlugin());// Removes the existing popup editing plugin

        graphMouse.add(myPlugin);

        vv.setGraphMouse(graphMouse);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(new GraphZoomScrollPane(vv));

        repaintAfterUpdates();

        JMenuBar menuBar = createMenuBar(graphMouse);

        createButtons(menuBar);

        currentRandomGraphEditDialog = new RandomGraphEditDialog();
        btnRandomGraph.addActionListener((ActionEvent eventRandom) -> {
            RandomGraphEditDialog randomGraphEditDialog = new RandomGraphEditDialog(this, currentRandomGraphEditDialog);
            currentRandomGraphEditDialog = randomGraphEditDialog.getrandomGraphEditDialog();
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

        //export image graph
        menuBar.getMenu(0).getItem(2).addActionListener((ActionEvent eventDeleteEdges) -> {

            ExportDialog export = new ExportDialog();
            export.showExportDialog(frame, "Export view as ...", vv, "Graph");

        });

        final JFileChooser fileChooser = new JFileChooser();
        //File -> save graph
        menuBar.getMenu(0).getItem(0).addActionListener((ActionEvent eventSaveGraph) -> {
            if (eventSaveGraph.getSource() == menuBar.getMenu(0).getItem(0)) {
                int returnVal = fileChooser.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    StaticLayout<GraphElements.MyVertex, GraphElements.MyEdge> staticLayout = new StaticLayout<>(graph, layout, new Dimension(1800, 1000));

                    GraphMLWriter<GraphElements.MyVertex, GraphElements.MyEdge> graphWriter = new GraphMLWriter<GraphElements.MyVertex, MyEdge>();

                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    graphWriter.addEdgeData("weight", null, "0", myEdge -> String.valueOf(myEdge.getWeight()));
                    graphWriter.addEdgeData("nameLink", null, "0", myEdge -> String.valueOf(myEdge.getName()));
                    graphWriter.addVertexData("x", null, "0", myVertex -> Double.toString(staticLayout.getX(myVertex)));
                    graphWriter.addVertexData("y", null, "0", myVertex -> Double.toString(staticLayout.getY(myVertex)));
                    try {
                        assert out != null;
                        graphWriter.save(graph, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //loading graph from GraphML
        menuBar.getMenu(0).getItem(1).addActionListener((ActionEvent eventLoadGraph) -> {
            if (eventLoadGraph.getSource() == menuBar.getMenu(0).getItem(1)) {
                int returnVal = fileChooser.showOpenDialog(frame);
                String TITLE_message = "Error message";
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    String fileName = file.toString();
                    BufferedReader fileReader = null;
                    try {
                        fileReader = new BufferedReader(
                                new FileReader(fileName));
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(vv,
                                new String[]{fileName + "\n" + "File not found!"},
                                TITLE_message,
                                JOptionPane.ERROR_MESSAGE);
                    }
                    Transformer<GraphMetadata, Graph<GraphElements.MyVertex, GraphElements.MyEdge>>
                            graphTransformer = metadata -> new
                            SparseMultigraph<>();
                    /* Create the Vertex Transformer */
                    Transformer<NodeMetadata, GraphElements.MyVertex> vertexTransformer
                            = metadata -> {
                        GraphElements.MyVertex v =
                                GraphElements.MyVertexFactory.getInstance().create(Integer.parseInt(metadata.getId()));

                        layout.setLocation(v, new Point2D() {
                            @Override
                            public double getX() {
                                return java.lang.Double.parseDouble(metadata.getProperty("x"));
                            }

                            @Override
                            public double getY() {
                                return java.lang.Double.parseDouble(metadata.getProperty("y"));
                            }

                            @Override
                            public void setLocation(double x, double y) {
                                layout.transform(v).setLocation(x, y);
                            }
                        });
                        return v;
                    };
                    /* Create the Edge Transformer */
                    Transformer<EdgeMetadata, MyEdge> edgeTransformer =
                            metadata -> GraphElements.MyEdgeFactory.getInstance().create(metadata.getProperty("nameLink"), Integer.parseInt(metadata.getProperties().get("weight")));

                    /* Create the Hyperedge Transformer */
                    Transformer<HyperEdgeMetadata, MyEdge> hyperEdgeTransformer
                            = metadata -> GraphElements.MyEdgeFactory.getInstance().create();

                    /* Create the graphMLReader2 */
                    if (fileReader != null) {
                        GraphMLReader2<Graph<GraphElements.MyVertex, MyEdge>, GraphElements.MyVertex, MyEdge>
                                graphReader = new
                                GraphMLReader2<>
                                (fileReader, graphTransformer, vertexTransformer,
                                        edgeTransformer, hyperEdgeTransformer);

                        try {
                            /* Get the new graph object from the GraphML file */
                            graph = (SparseMultigraph<GraphElements.MyVertex, MyEdge>) graphReader.readGraph();
                        } catch (GraphIOException ignored) {
                        }
                        layout.setGraph(graph);
                        frame.repaint();
                    }
                }
            }
        });

        frame.setJMenuBar(menuBar);
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING); // Start off in editing mode
        frame.pack();
        frame.setVisible(true);
    }

    // Add some popup menus for the edges and vertices to our mouse plugin.
    private void createPopupMenu(PopupVertexEdgeMenuMousePlugin<GraphElements.MyVertex, GraphElements.MyEdge> myPlugin) {
        JPopupMenu edgeMenu = new MyMouseMenus.EdgeMenu(frame);
        JPopupMenu vertexMenu = new MyMouseMenus.VertexMenu();
        myPlugin.setEdgePopup(edgeMenu);
        myPlugin.setVertexPopup(vertexMenu);
    }

    private EditingModalGraphMouse<GraphElements.MyVertex, GraphElements.MyEdge> createGraphMouse() {
        return new EditingModalGraphMouse<>(vv.getRenderContext(),
                GraphElements.MyVertexFactory.getInstance(),
                GraphElements.MyEdgeFactory.getInstance());
    }

    private void settingsVisualizationGraph() {
        vv.setBackground(Color.WHITE);
        vv.setPreferredSize(new Dimension(800, 650));
        // Show vertex and edge labels
        vv.getRenderContext().setLabelOffset(15);//смешение подписи ребра, чтобы не пересекались
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<>());
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
                for (ArrayList<MyEdge> curRouteEdge : paintedEdgeslist) {
                    chEdgeList = new ArrayList<>(curRouteEdge);

                    for (MyEdge e : layout.getGraph().getEdges()) {
                        if (chEdgeList.contains(e)) {
                            Renderer<GraphElements.MyVertex, MyEdge> renderer = vv.getRenderer();
                            renderer.renderEdge(
                                    vv.getRenderContext(),
                                    layout,
                                    e);
                        }
                    }
                    numColor++;
                }
            }
        });

    }

    // a menu for changing mouse modes
    private JMenuBar createMenuBar(EditingModalGraphMouse<GraphElements.MyVertex, GraphElements.MyEdge> graphMouse) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setPreferredSize(new Dimension(80, 20));
        JMenuItem save = new JMenuItem("Save graph");
        menuFile.add(save);

        JMenuItem loadGraph = new JMenuItem("Load graph from GraphML");
        menuFile.add(loadGraph);

        JMenuItem exportImgGraph = new JMenuItem("Print Screen");
        menuFile.add(exportImgGraph);
        menuBar.add(menuFile);

        JMenu modeMenu = graphMouse.getModeMenu();
        modeMenu.setText("Mouse mode");
        modeMenu.setIcon(null);
        modeMenu.setPreferredSize(new Dimension(80, 20));
        menuBar.add(modeMenu);

        return menuBar;
    }

    private void createButtons(JMenuBar menuBar) {
        btnRandomGraph = new JButton("Draw random graph");
        menuBar.add(btnRandomGraph);
        btnDeleteParalEdges = new JButton("Delete color edges");
        menuBar.add(btnDeleteParalEdges);
        btnClear = new JButton("Clear");
        menuBar.add(btnClear);
    }

    public SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> getGraph() {
        return graph;
    }

    public Layout<GraphElements.MyVertex, GraphElements.MyEdge> getLayout() {
        return layout;
    }

    public void setValidationInput(ValidateInput validationInput) {
        this.validationInput = validationInput;
    }


    public void setListOfShortcut(ArrayList<LinkedList<Integer>> list) {
        this.listOfShortcut = list;
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
     * @param countRoutes
     */
    public void addParalEdges(int countRoutes) {
        chEdgeList = new ArrayList<>();
        int numEdge = 0;
        paintedEdgeslist = new ArrayList<>();//список списков для раскрашивания ребер каждого пути в определённый цвет
        if (listOfShortcut.size() < countRoutes)
            countRoutes = listOfShortcut.size();

        //По всевозможным найденым путям пройтись
        for (int i = 0; i < countRoutes; i++) {
            ArrayList<GraphElements.MyEdge> path = new ArrayList<>();
            paintedEdgeslist.add(path);
            chEdgeList = formEdgesList(listOfShortcut.get(i));//получить список ребер.
            // for all edges, paint edges that are in minimum path
            for (GraphElements.MyEdge e : chEdgeList) {
                if (e.getFlagPaint() == 0) {//ещё не проходили по ребру, добавить это ребро для окраски в массив
                    path.add(e);
                    e.setFlagPaint(1);
                } else {
                    Collection<GraphElements.MyVertex> masVer;
                    masVer = graph.getIncidentVertices(e);//массив вершин, содержащих ребро
                    GraphElements.MyEdge paralEdge = new MyEdge("ParalEdge" + numEdge++, e.getWeight());
                    paralEdge.setFlagPaint(1);
                    graph.addEdge(paralEdge, masVer);//добавить параллельное ребро
                    path.add(paralEdge);
                }
            }
        }
    }


    //Преобразут список вершин хромосомы в список ребер для добавления параллельных ребер(без дубликатов ребер)
    public ArrayList<MyEdge> formEdgesList(LinkedList<Integer> listVerteces) {
        ArrayList<MyEdge> edgesList = new ArrayList<>();
        GraphElements.MyEdge e;

        for (int i = 0; i < listVerteces.size() - 1; i++) {
            e = graph.findEdge(matrix.getVertexOfIndex(listVerteces.get(i)), matrix.getVertexOfIndex(listVerteces.get(i + 1)));
            edgesList.add(e);
        }
        return edgesList;
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
        new ArrayList<>(graph.getVertices())
                .forEach(graph::removeVertex);
        chEdgeList = null;
        paintedEdgeslist = null;
        listOfShortcut = null;

        frame.repaint();
    }

    public boolean isEdgecontainsInPath(GraphElements.MyEdge e) {
        return chEdgeList.contains(e);
    }

    public int getNumColor() {//для раскраски ребра
        return numColor;
    }

    void setNewRandomGraphMatrix(JTextField textCountVertex, JTextField textFrom, JTextField textBefore, int percentOfEdges) {
        matrix = new GenerationMatrix(Integer.parseInt(textCountVertex.getText()), Integer.parseInt(textFrom.getText()), Integer.parseInt(textBefore.getText()), percentOfEdges, "0", "4");
        graph = matrix.getGraf();
        layout.setGraph(graph);
        frame.repaint();
    }

    public void setMatrix(GenerationMatrix matrix) {
        this.matrix = matrix;
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
        protected final Stroke THICK = new BasicStroke(2);//толстое ребро

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

    public static class EdgeLabelTransformer implements Transformer<MyEdge, String> {

        @Override
        public String transform(GraphElements.MyEdge e) {
            if (e.getName().contains("ParalEdge")) {
                return "";
            } else {
                return String.valueOf(e.getWeight());
            }

        }

    }

    //Изменить размер вершины
    public class MySizeVertex implements Transformer<GraphElements.MyVertex, Shape> {

        @Override
        public Shape transform(GraphElements.MyVertex i) {
            Ellipse2D circle = new Ellipse2D.Double(-14, -14, 28, 28);

            // in this case, the vertex is twice as large//в 1.5 раза больше
            if (validationInput.isNonNegativeNumber(s.getText()) && (Integer.parseInt(i.getName()) == Integer.parseInt(s.getText()))) {
                return AffineTransform.getScaleInstance(1.5, 1.5).createTransformedShape(circle);
            } else if (validationInput.isNonNegativeNumber(t.getText()) && (Integer.parseInt(i.getName()) == Integer.parseInt(t.getText()))) {
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
