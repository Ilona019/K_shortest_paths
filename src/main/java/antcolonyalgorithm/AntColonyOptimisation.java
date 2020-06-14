
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
    private double initialPheromone = 1;
    private int alpha;
    private int betta;
    private double evaporation;
    private int Q;

    private int maxIterations;

    private int numberVertex;
    private int colonySize;
    private GenerationMatrix matrix;
    private double[][] pheromoneMatrix;
    private double[][] visionOfAnts;
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private HashMap<Integer, Double> probabilities;

    private LinkedList<Ant> antsColonyBest;
    private int b;
    private int k;
    private long startTime;
    private float durationAlg;
    private int countIterations;

    //констуртор для запонения полей диалогового окна по умолчанию.
    public AntColonyOptimisation(int k) {
        this.colonySize = 2 * k;
        this.maxIterations = 100;
        this.alpha = 0;
        this.betta = 2;
        this.evaporation = 0.09;
        this.b = 5;
        this.k = k;
        this.Q = 500;
        countIterations = 0;
    }

    public AntColonyOptimisation(int colonySize, int alpha, int betta, double evaporation, GenerationMatrix matrix, int maxIterations, int b, int k, int Q) {
        this.numberVertex = matrix.getCountVerteces();
        this.colonySize = colonySize;
        this.maxIterations = maxIterations;
        this.alpha = alpha;
        this.betta = betta;
        this.evaporation = evaporation;
        this.matrix = matrix;
        this.b = b;
        this.k = k;
        this.Q = Q;
        pheromoneMatrix = new double[numberVertex][numberVertex];
        visionOfAnts = new double[numberVertex][];
        setupVisionAnts();
        setupTrails();
        probabilities = new HashMap<>();
        IntStream.range(0, colonySize)
                .forEach(i -> {
                    ants.add(new Ant(numberVertex, matrix));
                });
        antsColonyBest = new LinkedList<>();
        countIterations = 0;
    }

    public void setupVisionAnts() {
        for (int i = 1, l = 1; i < matrix.getCountVerteces(); i++, l++) {
            visionOfAnts[i] = new double[l];

            for (int j = 0; j < l; j++) {
                if (matrix.getWeight(i, j) != 0)
                    visionOfAnts[i][j] = Math.pow(1.0 / matrix.getWeight(i, j), betta);
                else visionOfAnts[i][j] = 0;
            }
        }
    }

    //получить элемент из треугольного массива по индексам строки и столбца;
    public double getVision(int i, int j) {
        if (i < j) {
            return visionOfAnts[j][i];
        } else if (i == j) {
            return 0;
        }
        return visionOfAnts[i][j];
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
            if (!ant.visited(v) && matrix.getWeight(ant.getRoute().getLast(), v) <= b) {
                amountPheromone += Math.pow(pheromoneMatrix[i][v], alpha) * getVision(i, v);
            }
        }


        for (int v : ant.getNeighborsVertexForLastVertex()) {
            if (!ant.visited(v) && matrix.getWeight(ant.getRoute().getLast(), v) <= b) {//если вершину не посещали ранее => можно переходить в неё.
                double numerator = Math.pow(pheromoneMatrix[i][v], alpha) * getVision(i, v);
                probabilities.put(v, numerator / amountPheromone);
            }
        }
    }

    private void updateTrails() {
        for (int i = 0; i < numberVertex; i++) {
            for (int j = 0; j < numberVertex; j++) {
                pheromoneMatrix[i][j] *= (1 - evaporation);
            }
        }

        for (Ant ant : ants) {
            if (ant.fitnessFunctionWeightST(b)) {
                double contribution = (double) Q / ant.calculateRouteLengthToT(matrix);
                for (int i = 0; i < ant.getIndexNewPath() - 1; i++) {
                    pheromoneMatrix[ant.getAtIndexVertex(i)][ant.getAtIndexVertex(i + 1)] += contribution;
                }
            }
        }


        for (Ant ant : ants) {
            if (ant.fitnessFunctionWeightTS(b)) {
                double contribution = (double) Q / ant.calculateRouteLengthToS(matrix);
                for (int i = ant.getIndexNewPath(); i < ant.getRoute().size() - 1; i++) {
                    pheromoneMatrix[ant.getAtIndexVertex(i)][ant.getAtIndexVertex(i + 1)] += contribution;
                }
            }
        }
    }

    private void updateListBest() {
        Ant cloneAnt;
        for (Ant a : ants) {


            if (a.fitnessFunctionWeightST(b)) {
                cloneAnt = new Ant(a, a.getRouteST());
                if (!existInColonyBest(cloneAnt)) {
                    antsColonyBest.add(cloneAnt);
                }
            }

            if (a.fitnessFunctionWeightTS(b) && a.getRouteTS().size() > 1) {
                LinkedList<Integer> pathTS = a.getRouteTS();
                Collections.reverse(pathTS);

                cloneAnt = new Ant(a, pathTS);
                if (!existInColonyBest(cloneAnt)) {
                    antsColonyBest.add(cloneAnt);
                }
            }
        }
    }

    private void sortBestColony() {
        RouteComparator myComparator = new RouteComparator();
        antsColonyBest.sort(myComparator);
    }


    private void setupTrails() {
        IntStream.range(0, numberVertex)
                .forEach(i -> {
                    IntStream.range(0, numberVertex)
                            .forEach(j ->
                                    pheromoneMatrix[i][j] = initialPheromone
                            );
                });
    }


    public boolean existInColonyBest(Ant newAnt) {
        for (Ant ant : antsColonyBest) {
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
        return countIterations;
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
