package antcolonyalgorithm;

import grapheditor.GenerationMatrix;
import java.util.LinkedList;

/**
 *
 * @author Ilona
 */
public class Ant {

    private LinkedList<Integer> route;
    private boolean visitedVertex[];
    private LinkedList<Integer> neighborsVertexForLastVertex;
    private GenerationMatrix matrix;
    private int routeLenght;

    public Ant(Ant cloneAnt) {
        this.matrix = cloneAnt.matrix;
        route = (LinkedList<Integer>) cloneAnt.route.clone();
        visitedVertex = cloneAnt.visitedVertex;
        routeLenght = cloneAnt.routeLenght;
    }

    public Ant(int size, GenerationMatrix matrix) {
        this.matrix = matrix;
        route = new LinkedList<>();
        visitedVertex = new boolean[size];
        routeLenght = 0;
    }

    public Integer getCurrentVertex() {
        return route.getLast();
    }

    public Integer getAtIndexVertex(int index) {
        return route.get(index);
    }

    public LinkedList<Integer> getRoute() {
        return route;
    }

    public LinkedList<Integer> getNeighborsVertexForLastVertex() {
        return neighborsVertexForLastVertex;
    }

    public void visitVertex(Integer vertex) {
        if (!route.isEmpty()) {
            routeLenght += matrix.getWeight(route.getLast(), vertex);
        }
        route.add(vertex);
        visitedVertex[vertex] = true;
        createListOfNeighboringVertices();
    }

    public Boolean visited(int index) {
        return visitedVertex[index];
    }

    public void calculateRouteLength(GenerationMatrix matrix) {
        int lenght = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            lenght += matrix.getWeight(route.get(i), route.get(i + 1));
        }
        this.routeLenght = lenght;
    }

    public int getRouteLenght() {
        return routeLenght;
    }

    //Вес пути не превосходит В?
    public boolean fitnessFunctionWeight(int b) {
        return routeLenght <= b;
    }

    public void clearVisited() {
        for (int i = 0; i < visitedVertex.length; i++) {
            visitedVertex[i] = false;
        }
        route.clear();
        routeLenght = 0;
    }

    public void oneStepVertexBack() {
        int lastVertex = route.removeLast();
        visitedVertex[lastVertex] = true;
        if (route.size() > 1) {
            routeLenght -= matrix.getWeight(route.getLast(), lastVertex);
        }

        createListOfNeighboringVertices();
    }

    public boolean maybyVisitVertex(int vertex) {
        if (neighborsVertexForLastVertex.contains(vertex)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String strRoute = "";
        for (Integer element : route) {
            strRoute += element + " ";
        }
        return strRoute;
    }

    public String printRoute(GenerationMatrix m) {
        String str = "";
        for (int i = 0; i < route.size(); i++) {
            str += m.getVertexOfIndex(route.get(i)).getNumberVertex() + "->";
        }
        return str;
    }

    private void createListOfNeighboringVertices() {
        neighborsVertexForLastVertex = matrix.getNeighbors(route.getLast());
    }

    public boolean equalsRoute(Ant ant) {
        String str1 = this.getRoute().toString();
        String str2 = ant.getRoute().toString();
        return str1.equals(str2);
    }

}
