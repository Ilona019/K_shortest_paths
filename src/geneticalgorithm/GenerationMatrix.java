package geneticalgorithm;

import edu.uci.ics.jung.graph.SparseMultigraph;
import grapheditor.GraphElements;
import grapheditor.GraphElements.MyEdgeFactory;
import grapheditor.GraphElements.MyVertexFactory;
import java.util.HashMap;

/**
 *
 * @author Илона
 */
public class GenerationMatrix {

    private final int matrix[][];
    private SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> graph;
    private HashMap<Integer, GraphElements.MyVertex> renameV;
    private int s;
    private int t;

  public  GenerationMatrix(SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> graph, String s1, String t1) {
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
    
    public GenerationMatrix(int countVertex, String s1, String t1) {
         matrix = new int[countVertex][];
         renameV = new HashMap<>();
         for (int i = 1, l = 1; i < countVertex; i++, l++) {
            matrix[i] = new int[l];

            for (int j = 0; j < l; j++) {
                matrix[i][j] = (int) (Math.random() * 100);
            }          
        }

        graph = new SparseMultigraph<>();
                     
        //добавление всех вершин
        MyVertexFactory myVertexFactory = new MyVertexFactory();
           GraphElements.MyVertex newVertex;
            for(int i = 0; i < countVertex; i++) {
                newVertex = myVertexFactory.create();
                graph.addVertex(newVertex);
            }
            
            this.initializeVertex(s1, t1);
            System.out.println("count ver = "+graph.getVertexCount());
            
          //добавление ребра
          MyEdgeFactory myEdgeFactory = new MyEdgeFactory();
          for (int i = 1, l = 1; i < countVertex; i++, l++) {
            for (int j = 0; j < l; j++) {
                
                if(matrix[i][j] != 0) {
                    graph.addEdge(myEdgeFactory.create(), renameV.get(i),renameV.get(j));
                    myEdgeFactory.setDefaultWeight(matrix[i][j]);
                }                   
            }
        }
          System.out.println("count edge = "+graph.getEdgeCount());
     }

    private void initializeVertex(String s1, String t1){
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

    //вернуть перенумерованную вершину начала
    public int getS() {
        return s;
    }

    //вернуть перенумерованную вершину конца
    public int getT() {
        return t;
    }

    //вернуть число вершин
    public int getCountVerteces() {
        return graph.getVertexCount();
    }

    //вернуть число ребер
    public int getCountEdges() {
        return graph.getEdgeCount();
    }
    
    public int getDegreeVertex(int indexVertex){
            return graph.getNeighborCount(renameV.get(indexVertex));
    }

    //вернуть по индексу перенумерованную вершину
    public GraphElements.MyVertex getVertexOfIndex(int ind) {
        return renameV.get(ind);
    }

    //записать элемент в треугольный массив;
    public void setWeight(int i, int j, int c) {
        matrix[i][j] = c;
    }

    //вернуть объект граф
    public SparseMultigraph<GraphElements.MyVertex, GraphElements.MyEdge> getGraf() {
        return graph;
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
