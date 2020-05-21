package grapheditor;

import edu.uci.ics.jung.graph.SparseMultigraph;
import grapheditor.GraphElements.MyEdgeFactory;
import grapheditor.GraphElements.MyVertexFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Илона
 */
public class GenerationMatrix {

    private final int[][] matrix;
    private SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> graph;
    private HashMap<Integer, GraphElements.MyVertex> renameV;
    private int s;
    private int t;

    public GenerationMatrix(SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> graph, String s1, String t1) {
        this.graph = graph;
        matrix = new int[graph.getVertexCount()][];
        renameV = new HashMap<>();
        this.initializeVertex(s1, t1);
        for (int i = 1, l = 1; i < graph.getVertexCount(); i++, l++) {
            matrix[i] = new int[l];

            for (int j = 0; j < l; j++) {
                if (graph.isNeighbor(renameV.get(i), renameV.get(j))) {
                    matrix[i][j] = graph.findEdge(renameV.get(i), renameV.get(j)).getWeight();
                } else {
                    matrix[i][j] = 0;
                }
            }
        }
    }

    public GenerationMatrix(int countVertex, int from, int before, int edgePercent, String s, String t) {
        matrix = new int[countVertex][];
        renameV = new HashMap<>();
        int willEdges = countVertex * (countVertex - 1) * edgePercent / 200;
        int edgesFullGraph = countVertex * (countVertex - 1) / 2;

        LinkedList<Integer> listWeight = new LinkedList<>();

        for (int i = 0; i < willEdges; i++) {
            listWeight.add((int) (Math.random() * (before - from + 1) + from));
        }
        if (willEdges < edgesFullGraph) {
            for (int i = willEdges; i < edgesFullGraph; i++) {
                listWeight.add(0);
            }
        }

        Collections.shuffle(listWeight);

        Iterator<Integer> iteratorListWeight = listWeight.iterator();
        for (int i = 1, l = 1; i < countVertex; i++, l++) {
            matrix[i] = new int[l];

            for (int j = 0; j < l; j++) {
                matrix[i][j] = (int) iteratorListWeight.next();
            }
        }

        createRandomGraph(countVertex, s, t);
    }

    private  void createRandomGraph(int countVertex, String s, String t){
        graph = new SparseMultigraph<>();

        //добавление всех вершин
        MyVertexFactory myVertexFactory = new MyVertexFactory();
        GraphElements.MyVertex newVertex;
        for (int i = 0; i < countVertex; i++) {
            newVertex = myVertexFactory.create();
            graph.addVertex(newVertex);
        }

        this.initializeVertex(s, t);

        //добавление ребра
        MyEdgeFactory myEdgeFactory = new MyEdgeFactory();
        for (int i = 1, l = 1; i < countVertex; i++, l++) {
            for (int j = 0; j < l; j++) {

                if (matrix[i][j] != 0) {
                    graph.addEdge(myEdgeFactory.create(), renameV.get(i), renameV.get(j));
                    myEdgeFactory.setDefaultWeight(matrix[i][j]);
                }
            }
        }
    }

    private void initializeVertex(String s1, String t1) {
        int k = 0;
        for (GraphElements.MyVertex ver : graph.getVertices()) {
            if (ver.getName().equals(s1)) {
                s = k;
            }
            if (ver.getName().equals(t1)) {
                t = k;
            }
            renameV.put(k++, ver);
        }

    }

    //получить элемент из треугольного массива по индексам строки и столбца;
    public int getWeight(int i, int j) {
        if (i < j) {
            return matrix[j][i];
        } else if (i == j) {
            return 0;
        }
        return matrix[i][j];
    }

    public int calculateRouteLength(LinkedList<Integer> route){
        int length = 0;

        for(int index = 0; index < route.size() - 1; index ++ ){
            length += getWeight(route.get(index), route.get(index + 1));
        }

        return length;
    }

    //вернуть перенумерованную вершину начала
    public int getS() {
        return s;
    }

    //вернуть перенумерованную вершину конца
    public int getT() {
        return t;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    //вернуть число вершин
    public int getCountVerteces() {
        return graph.getVertexCount();
    }

    //вернуть число ребер
    public int getCountEdges() {
        return graph.getEdgeCount();
    }

    public int getDegreeVertex(int indexVertex) {
        return graph.getNeighborCount(renameV.get(indexVertex));
    }

    //вернуть по индексу перенумерованную вершину
    public GraphElements.MyVertex getVertexOfIndex(int vertex) {
        return renameV.get(vertex);
    }

    //записать элемент в треугольный массив;
    public void setWeight(int i, int j, int c) {
        matrix[i][j] = c;
    }

    //вернуть объект граф
    public SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> getGraf() {
        return graph;
    }

    //отобрать номера в матрице смежных вершин с данной.
    public LinkedList<Integer> getNeighbors(int renameVertex) {
        LinkedList<Integer> list = new LinkedList<>();

        Collection<GraphElements.MyVertex> neighbors = graph.getNeighbors(renameV.get(renameVertex));

        for (GraphElements.MyVertex v : neighbors) {
            list.add(getKey(renameV, v));
        }

        return list;
    }

    public <K, V> K getKey(Map<K, V> map, V value) {
        for (Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    //вывод матрицы в консоль;
    public void printMatrix() {
        for (int i = 1, l = 1; i < matrix.length; i++, l++) {
            System.out.print(i + ")\t");
            for (int j = 0; j < l; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.print("\t");
        for (int i = 0; i < matrix.length - 1; i++) {
            System.out.print(i + ")\t");
        }
        System.out.println();
    }

}
