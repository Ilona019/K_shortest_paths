package antcolonyalgorithm;

import grapheditor.GenerationMatrix;
import main.ConvertRouteToString;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Ilona
 */
public class Ant extends ConvertRouteToString {

    private LinkedList<Integer> route;
    private boolean[] visitedVertex;
    private LinkedList<Integer> neighborsVertexForLastVertex;
    private GenerationMatrix matrix;
    private int routeLength;
    private int indexNewPath;

    public Ant(Ant cloneAnt, LinkedList<Integer> path) {
        this.matrix = cloneAnt.matrix;
        route = new LinkedList<>(path);
        visitedVertex = cloneAnt.visitedVertex;
        routeLength = calculateRouteAllLength(matrix);
        indexNewPath = cloneAnt.indexNewPath;
    }

    public Ant(int size, GenerationMatrix matrix) {
        this.matrix = matrix;
        route = new LinkedList<>();
        visitedVertex = new boolean[size];
        routeLength = 0;
        indexNewPath = 0;
    }

    public void setIndexNewPath() {
        indexNewPath = route.size() - 1;
    }

    public Integer getCurrentVertex() {
        return route.getLast();
    }

    public int getIndexNewPath() {
        return indexNewPath;
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
            routeLength += matrix.getWeight(route.getLast(), vertex);
        }
        route.add(vertex);
        visitedVertex[vertex] = true;
        createListOfNeighboringVertices();
    }

    public Boolean visited(int index) {
        return visitedVertex[index];
    }

    public int calculateRouteLengthToT(GenerationMatrix matrix) {
        int length = 0;
        for (int i = 0; i < indexNewPath; i++) {
            length += matrix.getWeight(route.get(i), route.get(i + 1));
        }
        return length;
    }

    public int calculateRouteLengthToS(GenerationMatrix matrix) {
        int length = 0;
        for (int i = indexNewPath; i < route.size() - 1; i++) {
            length += matrix.getWeight(route.get(i), route.get(i + 1));
        }
        return length;
    }

    public int calculateRouteAllLength(GenerationMatrix matrix) {
        int length = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            length += matrix.getWeight(route.get(i), route.get(i + 1));
        }
        return length;
    }

    public void deleteBadPathTS() {
        int sizeRoute = route.size();
        route.subList(indexNewPath + 1, sizeRoute).clear();
    }

    public int getRouteLength() {
        return routeLength;
    }

    public LinkedList<Integer> getRouteST() {
        return new LinkedList<>(route.subList(0, indexNewPath + 1));
    }

    public LinkedList<Integer> getRouteTS() {
        return new LinkedList<>(route.subList(indexNewPath, route.size()));
    }

    //Вес пути не превосходит В?
    public boolean fitnessFunctionWeightST(int b) {
        return calculateRouteLengthToT(matrix) <= b;
    }

    public boolean fitnessFunctionWeightTS(int b) {
        return calculateRouteLengthToS(matrix) <= b;
    }

    public void clearVisited() {
        Arrays.fill(visitedVertex, false);
        route.clear();
        routeLength = 0;
    }

    public void cancelVisitVertex(int index) {
        visitedVertex[index] = false;
    }

    public void oneStepVertexBack() {
        int lastVertex = route.removeLast();
        visitedVertex[lastVertex] = true;

        if (route.size() > 1) {
            routeLength -= matrix.getWeight(route.getLast(), lastVertex);
        }
        createListOfNeighboringVertices();
    }

    public boolean maybyVisitVertex(int vertex) {
        return neighborsVertexForLastVertex.contains(vertex);
    }

    @Override
    public String toString() {
        StringBuilder strRoute = new StringBuilder();
        for (Integer element : route) {
            strRoute.append(element).append(" ");
        }
        return strRoute.toString();
    }

    private void createListOfNeighboringVertices() {
        neighborsVertexForLastVertex = matrix.getNeighbors(route.getLast());
    }

    public boolean equalsRoute(Ant ant) {
        String route1 = this.getRoute().toString();
        String route2 = ant.getRoute().toString();
        return route1.equals(route2);
    }

}
