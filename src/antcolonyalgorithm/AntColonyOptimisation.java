
package antcolonyalgorithm;

import grapheditor.GenerationMatrix;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Ilona
 */
public class AntColonyOptimisation {
    private double initialPheromone = 1.0;
    private int alpha; 
    private int betta;
    private double evaporation;
    private double Q = 500;

    private int maxIterations;

    private int numberVertex;
    private int colonySize;
    private GenerationMatrix matrix;
    private double pheromoneMatrix[][];
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private HashMap<Integer, Double> probabilities;

    private LinkedList<Ant> antsColonyBest;
    private int b;
    private long startTime;
    private float durationAlg;
    
    //констуртор для запонения полей диалогового окна по умолчанию.
    public AntColonyOptimisation() {
        this.colonySize = 10;
        this.maxIterations = 5; 
        this.alpha = 1;
        this.betta = 3;
        this.evaporation = 0.1;
        this.b = 5;
    }
    
    public AntColonyOptimisation(int colonySize, int alfa, int betta, double evaporation, GenerationMatrix matrix, int maxIterations, int b) {
        this.numberVertex = matrix.getCountVerteces();
        this.colonySize = colonySize;
        this.maxIterations = maxIterations; 
        this.alpha = alfa;
        this.betta = betta;
        this.evaporation = evaporation;
        this.matrix = matrix;
        this.b = b;
        pheromoneMatrix = new double[numberVertex][numberVertex];
        probabilities = new HashMap();
        IntStream.range(0, colonySize)
            .forEach(i -> ants.add(new Ant(numberVertex, matrix)));
                antsColonyBest = new LinkedList();
    }

    public void startAntOptimization() {
            startTime = System.currentTimeMillis(); // time
                IntStream.rangeClosed(1, maxIterations)
            .forEach(i -> {
                //System.out.println("Attempt #" + i);
               run();
            });
         durationAlg = ((System.currentTimeMillis() - startTime)/ 1000F);
//                System.out.println("Result  founded "+antsColonyBest.size()+" routes.");
//                antsColonyBest.sort(new RouteComparator());
//                for(int i=0; i< antsColonyBest.size(); i++)
//                    System.out.println(antsColonyBest.get(i).printRoute(matrix)+" lenght "+antsColonyBest.get(i).getRouteLenght());          
                    
    }
    
    private void run(){
        setupAnts();
        clearTrails();
                moveAnts();
                  updateTrails();
                updateListBest();
    }
    
     private void setupAnts() {
        IntStream.range(0, colonySize)
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

            if(!buildRouteForAnt(ant)){
                antsIterator.remove(); 
                
            }
        }
    }
     
    private boolean buildRouteForAnt(Ant ant) {
        while (ant.getRoute().getLast() != matrix.getT()) {
            try {
                ant.visitVertex((Integer) selectNextVertex(ant));
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
            

            //сортировка  по возрастанию хэша вероятностей попадания в следующую вершину
            probabilities.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue());
                    


            if (probabilities.isEmpty() && (ant.getCurrentVertex() != matrix.getS())) {
                    ant.oneStepVertexBack();
                    probabilities.clear(); 
            } else {
                break;
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
    
    public static <K, V extends Comparable<? super V>> Map<K, V>  sortByValue( Map<K, V> map )
    {
          Map<K,V> result = new LinkedHashMap<>();
         Stream <Entry<K,V>> st = map.entrySet().stream();

         st.sorted(Comparator.comparing(e -> e.getValue()))
              .forEach(e ->result.put(e.getKey(),e.getValue()));

         return result;
    }
    
    public void calculateProbabilities(Ant ant) {
        int i = ant.getCurrentVertex();
        double amountPheromone = 0.0;
        
        for (int v: ant.getNeighborsVertexForLastVertex()) {
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
                pheromoneMatrix[i][j] *= (1 - evaporation)*pheromoneMatrix[i][j];
            }
        }
        for (Ant ant : ants) {
            double contribution = 500/ ant.getRouteLenght();
            for (int i = 0; i < ant.getRoute().size() - 1; i++) {
                pheromoneMatrix[ant.getAtIndexVertex(i)][ant.getAtIndexVertex(i+1)] += contribution;
            }
           // pheromoneMatrix[a.getAtIndexVertex(numberVertex - 1)][a.getAtIndexVertex(0)] += contribution;
        }
//        
//        System.out.println("Matrix pheromone");
//        for(int i = 0 ; i < pheromoneMatrix.length; i++ ) {
//                    for(int j = 0 ; j < pheromoneMatrix.length; j++ ) {
//                        System.out.print(pheromoneMatrix[i][j]+" \t");
//        }
//                   System.out.println();
//        }
    }
        
    private void updateListBest() {
     
        for (Ant a : ants) {
            if(a.fitnessFunctionWeight(b) && !existInColonyBest(a)) {
                antsColonyBest.add(new Ant(a));
            }
        }
    }
        
        
    private void clearTrails() {
        IntStream.range(0, numberVertex)
            .forEach(i -> {
                IntStream.range(0, numberVertex)
                    .forEach(j -> pheromoneMatrix[i][j] = initialPheromone);
            });
    }
    
    
    public boolean existInColonyBest(Ant newAnt) {
        for (int i = 0; i < antsColonyBest.size(); i++) {
            if (antsColonyBest.get(i).equalsRoute(newAnt)) {
                return true;
            }
        }
        return false;
    }
    
    public LinkedList<Ant> getAntsColonyBest(){
        return antsColonyBest;
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
    
    public int getMaxIterations(){
        return maxIterations;
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
    
    public String convertRoutesToString(GenerationMatrix m) {
        String str = "";
        for (int i = 0; i < antsColonyBest.size(); i++) {
            str += i+")"+ "Route == " + antsColonyBest.get(i).getRouteLenght() + ";\t" + antsColonyBest.get(i).printRoute(m) + "\n";
        }
        return str;
    }
}
