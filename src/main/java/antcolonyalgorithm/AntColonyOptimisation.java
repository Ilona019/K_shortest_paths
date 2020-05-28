
package antcolonyalgorithm;

import grapheditor.GenerationMatrix;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Ilona
 */
public class AntColonyOptimisation {
    private double initialPheromone = 1.0;
    private int alpha;
    private int betta;
    private double evaporation;
    private int Q;

    private int maxIterations;

    private int numberVertex;
    private int colonySize;
    private GenerationMatrix matrix;
    private double[][] pheromoneMatrix;
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private HashMap<Integer, Double> probabilities;

    private LinkedList<Ant> antsColonyBest;
    private LinkedList<Ant> currentColonyBest;
    private int b;
    private int k;
    private long startTime;
    private float durationAlg;
    private int countIterations;
    private int countIterationsOnBestColony;

    //констуртор для запонения полей диалогового окна по умолчанию.
    public AntColonyOptimisation() {
        this.colonySize = 20;
        this.maxIterations = 100;
        this.alpha = 1;
        this.betta = 3;
        this.evaporation = 0.4;
        this.b = 5;
        this.k = 5;
        this.Q = 500;
        countIterations = 0;
    }

    public AntColonyOptimisation(int colonySize, int alfa, int betta, double evaporation, GenerationMatrix matrix, int maxIterations, int b, int k, int Q) {
        this.numberVertex = matrix.getCountVerteces();
        this.colonySize = colonySize;
        this.maxIterations = maxIterations;
        this.alpha = alfa;
        this.betta = betta;
        this.evaporation = evaporation;
        this.matrix = matrix;
        this.b = b;
        this.k = k;
        this.Q = Q;
        pheromoneMatrix = new double[numberVertex][numberVertex];
        probabilities = new HashMap<>();
        IntStream.range(0, colonySize)
                .forEach(i -> {
                    ants.add(new Ant(numberVertex, matrix));
                });
        antsColonyBest = new LinkedList<>();
        currentColonyBest = new LinkedList<>();
        countIterations = 0;
    }

    public void startAntOptimization() {
        startTime = System.currentTimeMillis();
        while (countIterations != maxIterations) {
            run();
            if (antsColonyBest.size() >= k) {
                sortBestColony();
                countIterations++;
                durationAlg = ((System.currentTimeMillis() - startTime) / 1000F);
                break;
            }
            countIterations++;
        }
        sortBestColony();
        durationAlg = ((System.currentTimeMillis() - startTime) / 1000F);
    }

    private void run() {
        setupAnts();
        clearTrails();
        moveAnts();
        updateTrails();
        updateListBest();
    }

    private void setupAnts() {
        //Если на предыдущей итерации удалялись муравьи, восстановить недостающих.
        IntStream.range(ants.size(), colonySize)
                .forEach(i -> {
                    ants.add(new Ant(numberVertex, matrix));
                });
        IntStream.range(0, ants.size())
                .forEach(i -> {
                    ants.forEach(ant -> {
                        ant.clearVisited();
                        ant.visitVertex(matrix.getS());//точка начала пути муравья выбирается вершина s.
                    });
                });
    }

    private void moveAnts() {
        Iterator<Ant> antsIterator = ants.iterator();
        while (antsIterator.hasNext()) {
            Ant ant = antsIterator.next();

            if (!buildRouteForAnt(ant)) {
                antsIterator.remove();
            } else {
                ant.setIndexNewPath();
                ant.cancelVisitVertex(matrix.getS());
                ant.cancelVisitVertex(ant.getAtIndexVertex(ant.getRoute().size() - 2));
                if (!buildRouteBack(ant)) {
                    ant.deleteBadPathTS();
                }
            }
        }
    }

    private boolean buildRouteForAnt(Ant ant) {
        while (ant.getRoute().getLast() != matrix.getT()) {
            try {
                ant.visitVertex(selectNextVertex(ant));
                probabilities.clear();
            } catch (Throwable e) {
                return false;
            }
        }

        return true;
    }

    private boolean buildRouteBack(Ant ant) {
        while (ant.getRoute().getLast() != matrix.getS()) {
            try {
                ant.visitVertex(selectNextVertex(ant));
                probabilities.clear();
            } catch (Throwable e) {
                return false;
            }
        }
        return true;
    }

    private int selectNextVertex(Ant ant) {

        double r = random.nextDouble();
        double total = 0;

        while (true) {
            calculateProbabilities(ant);

            //сортировка по возрастанию хэша вероятностей попадания в следующую вершину
            probabilities = probabilities.entrySet()
                    .stream()
                    .sorted(Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            if (ant.getIndexNewPath() == 0) {
                if (probabilities.isEmpty() && (ant.getCurrentVertex() != matrix.getS())) {
                    ant.oneStepVertexBack();
                    probabilities.clear();
                } else {
                    break;
                }
            } else {
                if (probabilities.isEmpty() && (ant.getCurrentVertex() != matrix.getT())) {
                    ant.oneStepVertexBack();
                    probabilities.clear();
                } else {
                    break;
                }
            }
        }


        for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
            total += entry.getValue();
            if (r <= total) {
                return entry.getKey();
            }

        }
        throw new RuntimeException("There are no other vertex");

    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Entry<K, V>> st = map.entrySet().stream();

        st.sorted(Entry.comparingByValue())
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    public void calculateProbabilities(Ant ant) {
        int i = ant.getCurrentVertex();
        double amountPheromone = 0.0;

        for (int v : ant.getNeighborsVertexForLastVertex()) {
            if (!ant.visited(v)) {
                amountPheromone += Math.pow(pheromoneMatrix[i][v], alpha) * Math.pow(1.0 / matrix.getWeight(i, v), betta);
            }
        }


        for (int v : ant.getNeighborsVertexForLastVertex()) {
            if (!ant.visited(v)) {//если вершину не посещали ранее => можно переходить в неё.
                double numerator = Math.pow(pheromoneMatrix[i][v], alpha) * Math.pow(1.0 / matrix.getWeight(i, v), betta);
                probabilities.put(v, numerator / amountPheromone);
            }
        }
    }

    private void updateTrails() {
        for (int i = 0; i < numberVertex; i++) {
            for (int j = 0; j < numberVertex; j++) {
                pheromoneMatrix[i][j] *= (1 - evaporation) * pheromoneMatrix[i][j];
            }
        }
        for (Ant ant : ants) {
            double contribution = (double) Q / ant.getRouteLength();
            for (int i = 0; i < ant.getRoute().size() - 1; i++) {
                pheromoneMatrix[ant.getAtIndexVertex(i)][ant.getAtIndexVertex(i + 1)] += contribution;
            }

        }
    }

    private void updateListBest() {
        Ant cloneAnt;
        currentColonyBest.clear();
        for (Ant a : ants) {
            if (a.fitnessFunctionWeightST(b)) {
                cloneAnt = new Ant(a, a.getRouteST());
                if (!existInColonyBest(cloneAnt)) {
                    currentColonyBest.add(cloneAnt);
                }
            }

            if (a.fitnessFunctionWeightTS(b) && a.getRouteTS().size() > 1) {
                LinkedList<Integer> pathTS = a.getRouteTS();
                Collections.reverse(pathTS);

                cloneAnt = new Ant(a, pathTS);
                if (!existInColonyBest(cloneAnt)) {
                    currentColonyBest.add(cloneAnt);
                }
            }
        }
        if (antsColonyBest.size() <= currentColonyBest.size()) {
            antsColonyBest.clear();
            antsColonyBest = new LinkedList<>(currentColonyBest);
            countIterationsOnBestColony = countIterations + 1;
        }

    }

    private void sortBestColony() {
        RouteComparator myComparator = new RouteComparator();
        antsColonyBest.sort(myComparator);
    }


    private void clearTrails() {
        IntStream.range(0, numberVertex)
                .forEach(i -> {
                    IntStream.range(0, numberVertex)
                            .forEach(j -> pheromoneMatrix[i][j] = initialPheromone);
                });
    }


    public boolean existInColonyBest(Ant newAnt) {
        for (Ant ant : currentColonyBest) {
            if (ant.equalsRoute(newAnt)) {
                return true;
            }
        }
        return false;
    }

    public LinkedList<Ant> getAntsColonyBest() {
        return antsColonyBest;
    }

    public int getCountIterationsOnBestColony() {
        return countIterationsOnBestColony;
    }

    public float getDuration() {
        return durationAlg;
    }

    public int getColonySize() {
        return colonySize;
    }

    public double getEvaporation() {
        return evaporation;
    }

    public int getAlpha() {
        return alpha;
    }

    public int getBetta() {
        return betta;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getQ() {
        return Q;
    }

    public int getCountIterations() {
        return countIterations;
    }

    void setColonySize(int colonySize) {
        this.colonySize = colonySize;
    }

    void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    void setBetta(int betta) {
        this.betta = betta;
    }

    void setEvaporation(double evaporation) {
        this.evaporation = evaporation;
    }

    void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setQ(int q) {
        Q = q;
    }

    public String convertRoutesToString(GenerationMatrix m) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < antsColonyBest.size(); i++) {
            str.append(i).append(")").append("Route == ").append(antsColonyBest.get(i).getRouteLength()).append(";\t").append(antsColonyBest.get(i).routeToString(m, antsColonyBest.get(i).getRoute())).append("\n");
        }
        return str.toString();
    }

}
