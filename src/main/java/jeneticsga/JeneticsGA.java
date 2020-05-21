package jeneticsga;

import grapheditor.GenerationMatrix;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import main.ConvertRouteToString;

import java.util.ArrayList;
import java.util.LinkedList;


public class JeneticsGA extends ConvertRouteToString {
    public static GenerationMatrix matrix;
    public static ArrayList<LinkedList<Integer>> populationChromosomes;
    private static JeneticsGA.CrossingType crossingType;//Тип скрещивания.
    private static JeneticsGA.MutationType mutationType;//Тип скрещивания.
    private static JeneticsGA.SelectionType selectionType;//Тип селекции.
    private static Mutator<IntegerGene, Integer> selectedMutation;
    private static Crossover<IntegerGene, Integer> selectedCrossover;
    private static Selector<IntegerGene, Integer> selectedSurvivorsSelector;
    private static int n;
    private static int b;
    private static int k;
    private static float durationAlg;
    private static long startTime;
    private static int numberGeneration;
    private static double mutationProbability;
    ;

    private static int evaluate(final Genotype<IntegerGene> gt) {

        LinkedList<Integer> routeLength = new LinkedList<>();
        for (IntegerGene currentGene : gt.chromosome()) {
            routeLength.add(currentGene.allele());
        }
        return matrix.calculateRouteLength(routeLength);
    }

    public JeneticsGA() {
        crossingType = JeneticsGA.CrossingType.valueOf("UNIFORM");
        mutationType = JeneticsGA.MutationType.valueOf("SWAP");
        selectionType = JeneticsGA.SelectionType.valueOf("TOURNAMENT");
        n = 20;
        numberGeneration = 0;
        mutationProbability = 0.5;
    }

    public JeneticsGA(GenerationMatrix matrix, int n, int k, int b, String crossingType, String mutation, String selectionType, double mutationProbability) {
        startTime = System.currentTimeMillis(); // time
        JeneticsGA.matrix = matrix;
        populationChromosomes = new ArrayList<>();
        JeneticsGA.crossingType = JeneticsGA.CrossingType.valueOf(crossingType);
        JeneticsGA.mutationType = JeneticsGA.MutationType.valueOf(mutation);
        JeneticsGA.selectionType = JeneticsGA.SelectionType.valueOf(selectionType.replace(' ', '_'));
        JeneticsGA.n = n;
        JeneticsGA.k = k;
        JeneticsGA.b = b;
        JeneticsGA.mutationProbability = mutationProbability;
        initOperators();
    }

    public void setMutationProbability(double probability) {
        JeneticsGA.mutationProbability = probability;
    }

    public enum CrossingType {
        UNIFORM, LINE
    }

    public enum MutationType {
        SWAP, GAUSSIAN
    }

    public enum SelectionType {
        ROULETTE_WHEEL, TOURNAMENT
    }

    public static void initOperators() {

        switch (crossingType) {
            case UNIFORM:
                selectedCrossover = new UniformCrossover<>(0.7);
                break;
            case LINE:
                selectedCrossover = new LineCrossover<>(0.7);
                break;
        }

        switch (mutationType) {
            case SWAP:
                selectedMutation = new SwapMutator<>(mutationProbability);
                break;
            case GAUSSIAN:
                selectedMutation = new GaussianMutator<>(mutationProbability);
                break;
        }

        switch (selectionType) {
            case ROULETTE_WHEEL:
                selectedSurvivorsSelector = new RouletteWheelSelector<>();
                break;
            case TOURNAMENT:
                selectedSurvivorsSelector = new TournamentSelector<>();
                break;
        }

    }

    public static void runAlgorithm() {
        Factory<Genotype<IntegerGene>> gtf = Genotype.of(
                CustomChromosome.of(ISeq.of(IntegerGene.of(0, matrix.getCountVerteces() - 1)), matrix)
        );
        LinkedList<Integer> curChromosome = new LinkedList<>();

        final Engine<IntegerGene, Integer> engine = Engine.builder(
                JeneticsGA::evaluate,
                gtf)
                .optimize(Optimize.MINIMUM)
                .populationSize(n)
                .mapping(EvolutionResult.toUniquePopulation(gtf))
                .selector(selectedSurvivorsSelector)//отбор сильшейших, выбор выживших и потомков.
                .alterers(
                        selectedCrossover,
                        selectedMutation)
                .build();

        ISeq<EvolutionResult<IntegerGene, Integer>> genotypes = engine.stream()
                .limit(1000)
                .collect(ISeq.toISeq());

        populationChromosomes.clear();
        for (EvolutionResult<IntegerGene, Integer> ch : genotypes) {
            numberGeneration = (int) ch.generation();
            for (Genotype<IntegerGene> genes : ch.genotypes()) {
                for (IntegerGene intGene : genes.chromosome()) {
                    curChromosome.add(intGene.allele());
                }

                if (evaluate(genes) < b && !existInReserve(curChromosome)) {
                    populationChromosomes.add(new LinkedList<>(curChromosome));
                }
                curChromosome.clear();
            }
            if (populationChromosomes.size() >= k) {
                durationAlg = ((System.currentTimeMillis() - startTime) / 1000F);
                return;
            }
        }


    }

    public void setN(int n) {
        JeneticsGA.n = n;
    }


    public void setСrossingType(String crossingType) {
        JeneticsGA.crossingType = JeneticsGA.CrossingType.valueOf(crossingType);
    }

    public void setSelectionType(String selectionType) {
        JeneticsGA.selectionType = JeneticsGA.SelectionType.valueOf(selectionType.replace(' ', '_'));
    }

    public void setMutationType(String mutationType) {
        JeneticsGA.mutationType = JeneticsGA.MutationType.valueOf(mutationType);
    }

    public String getСrossingType() {
        return crossingType.toString();
    }

    public String getMutationType() {
        return mutationType.toString();
    }

    public String getSelectionType() {
        return selectionType.toString().replace('_', ' ');
    }

    public ArrayList<LinkedList<Integer>> getPopulationChromosomes() {
        return populationChromosomes;
    }

    public float getDurationAlg() {
        return durationAlg;
    }

    public int numberGeneration() {
        return numberGeneration;
    }

    public static boolean existInReserve(LinkedList<Integer> list) {
        for (int i = 0; i < populationChromosomes.size(); i++) {
            if (populationChromosomes.get(i).equals(list)) {
                return true;
            }
        }
        return false;
    }

    public String convertRoutesToString(GenerationMatrix m) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < populationChromosomes.size(); i++) {
            str.append(i).append(")").append("Route == ").append(matrix.calculateRouteLength(populationChromosomes.get(i))).append(";\t").append(routeToString(m, populationChromosomes.get(i))).append("\n");
        }
        return str.toString();
    }

}
