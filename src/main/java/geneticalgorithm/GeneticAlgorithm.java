package geneticalgorithm;

import grapheditor.GenerationMatrix;
import io.jenetics.Chromosome;
import main.ConvertRouteToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author Илона
 */
public class GeneticAlgorithm extends ConvertRouteToString {

    private Population population;
    private int[] masPair;
    private ChoiceOfParents choiceParents;//Тип оператора отбора родителей.
    private CrossingType crossingType;//Тип скрещивания.
    private MutationType mutationType;
    private SelectionType selectionType;//Тип селекции.
    private GenerationMatrix matrix;
    private int b;
    private int n;
    private LinkedList<Individual> reserveChromosomes;
    private double mutationProbability;

    public GeneticAlgorithm() {
        this.choiceParents = ChoiceOfParents.PANMIXIA;
        this.crossingType = CrossingType.SINGLE_POINT;
        this.mutationType = MutationType.UNIFORM;
        this.selectionType = SelectionType.ELITE;
        this.n = 20;
        mutationProbability = 0.5;
    }

    public GeneticAlgorithm(GenerationMatrix matrix, Population population, String choiceParents, String crossingType, String mutationType, String selectionType, int b, int n, double mutationProbability) {
        this.matrix = matrix;

        this.population = population;
        masPair = new int[2 * population.size()];
        this.choiceParents = ChoiceOfParents.valueOf(choiceParents.replace(' ', '_'));
        this.crossingType = CrossingType.valueOf(crossingType.replace(' ', '_'));
        this.mutationType = MutationType.valueOf(mutationType.replace(' ', '_'));
        this.selectionType = SelectionType.valueOf(selectionType.replace(' ', '_'));
        this.mutationProbability = mutationProbability;
        this.b = b;
        this.n = n;
        reserveChromosomes = new LinkedList<>();
    }

    public enum SelectionType {
        ELITE
    }

    //Оператор селекции, отбор в новую популяцию.
    public void selection() {
        switch (selectionType) {
            case ELITE:
                RouteComparator myRouteComparator = new RouteComparator();
                population.getPopulation().sort(myRouteComparator);//сортировка возрастанию длины пути
                int needDelete = population.getPopulation().size() - n;//надо удалить
                int deleted = 0;//кол-во удалённых хромосом
                for (int i = 0, size = population.getPopulation().size(); i < size; i++) {
                    if (needDelete == deleted) {
                        break;
                    }
                    if (!population.getPopulation().getLast().fitnessFunctionWeight(b)) {
                        population.getPopulation().removeLast();//удаляем хромосомы не удовлетворяющие условию путь <= B.
                        deleted++;//cчётчик сколько удалили хромосом.
                    }

                }
                Individual ind;
                if (needDelete > deleted) {//если не достаточно хромосом удалили, то доудалим наиболее длинные по длине пути.
                    while (needDelete != deleted) {
                        ind = population.getPopulation().removeLast();
                        if (!existInReserve(ind))//Если такой хромосомы нет в cписке резерва -> добавим
                        {
                            this.addReserveChromosome(ind);
                        }
                        deleted++;
                    }
                }
                break;
        }
    }

    public enum ChoiceOfParents {
        PANMIXIA, INBREEDING, OUTBREEDING
    }

    //Выбор родителей, разбиение на пары.
    public void choiceParents() {
        switch (choiceParents) {
            case PANMIXIA://Оба родителя выбираются случайно.
                for (int i = 0; i < (2 * population.size()); i++) {
                    masPair[i] = (int) (Math.random() * (population.size() - 1));
                }
                break;
            case INBREEDING://Первая особь в паре выбирается случайно, вторая особь - ближайщая по длине маршрута.
                for (int i = 0; i < (2 * population.size()); i += 2) {
                    masPair[i] = (int) (Math.random() * (population.size() - 1));
                    masPair[i + 1] = population.indexNearbyRoute(masPair[i]);
                }
                break;
            case OUTBREEDING://Первая особь в паре выбирается случайно, вторая особь - c дальним по длине маршрута.
                for (int i = 0; i < (2 * population.size() - 1); i += 2) {
                    masPair[i] = (int) (Math.random() * (population.size() - 1));
                    masPair[i + 1] = population.indexMaxRoute();
                }
                break;
        }
    }

    public enum CrossingType {
        SINGLE_POINT, TWO_POINT
    }

    //Оператор скрещивания родителей.
    public void crossing() {
        Individual indMin, indMax;//по длине хромосомы
        ArrayList<Individual> arr;
        switch (crossingType) {
            case SINGLE_POINT://Если  в хромосоме есть две одинаковые вершины, отличные от начала и конца и вершин соседних с ними, то померять местами их концы.
                for (int i = 1; i < masPair.length - 1; i += 2) {
                    if (masPair[i] != masPair[i - 1]) {//если пара не образуется сама с собой
                        arr = population.getAtIndex(masPair[i]).maxLengthInd1AndInd2(population.getAtIndex(masPair[i - 1]));
                        indMax = arr.get(0);//Наибольшая по длине хромосома
                        indMin = arr.get(1);//Наименьшая по длине хромосома

                        singlePointCrossover(indMax, indMin);
                    }
                }
                break;
            case TWO_POINT:
                for (int i = 1; i < masPair.length - 1; i += 2) {
                    if (masPair[i] != masPair[i - 1]) {//если пара не образуется сама с собой
                        arr = population.getAtIndex(masPair[i]).maxLengthInd1AndInd2(population.getAtIndex(masPair[i - 1]));
                        indMax = arr.get(0);//Наибольшая по длине хромосома
                        indMin = arr.get(1);//Наименьшая по длине хромосома
                        twoPointCrossover(indMax, indMin);
                    }
                }
                break;
        }
    }

    enum MutationType {
        UNIFORM
    }

    //Оператор мутации потомков.
    public void mutation() {
        switch (mutationType) {
            case UNIFORM:
                Individual currentChromosome;

                for (int i = n; i < population.size(); i++) {
                    currentChromosome = population.getAtIndex(i);

                    Individual chromosomeAfterMutation;
                    int j = 0;
                    double randomXi1 = Math.random();
                    if (randomXi1 > 0.2) {
                        while (j++ < 3) {
                            chromosomeAfterMutation = new Individual(currentChromosome);
                            if (chromosomeAfterMutation.mutation(matrix, b, mutationProbability)) {
                                shortensChromosome(chromosomeAfterMutation);
                                population.replaceChromosomeAtIndex(i, chromosomeAfterMutation);
                                break;
                            }
                        }
                    }
                }
        }

    }

    public void shortensChromosome(Individual ind) {
        int point2;//индекс эквивалентной вершины
        int flag;
        if (reserveChromosomes.isEmpty()) {
            flag = 0;//сокращаем хромосому до конца
        } else {
            flag = 1;
        }
        for (int point1 = 1; point1 < ind.getChromomeStructure().size() - 1; point1++) {

            if (ind.isNumberVertex(ind.getChromomeStructure().get(point1), point1 + 1) != -1) {//есть ли начиная с индекса point1, вершина ch.get_list_chromosome().get(point1)
                point2 = ind.isNumberVertex(ind.getChromomeStructure().get(point1), point1 + 1);
                if (!existInReserve(ind) && ind.getFitnessF())//сохраним в резерв хорошую хромосому по приспособленности до мутации, если её нет в резерве
                {
                    this.addReserveChromosome(new Individual(ind));
                }

                ind.cutPartChromosome(point1, point2);
                ind.recalculateFitnessFunc(matrix, b);//изменилась длина маршрута и надо пересчитать фитнесс функцию
                if (flag == 1) {
                    break;
                }
            }
        }

    }

    public LinkedList<Individual> getReserveChromosomes() {
        return reserveChromosomes;
    }

    //Существует ли в резервном списке такой индивид ind с аналогичным маршрутом?
    public boolean existInReserve(Individual ind) {
        for (Individual reserveChromosome : reserveChromosomes) {
            if (reserveChromosome.equalsChromosome(ind)) {
                return true;
            }
        }
        return false;
    }

    //Вернуть индивид из резерва, который отсутствует в популяции.
    public Individual returnItemDifferentOthers(Population population) {
        for (Individual reserveChromosome : reserveChromosomes) {
            if (!population.existInPopulation(reserveChromosome)) {
                return reserveChromosome;
            }
        }
        return null;
    }

    // Добавить в резервный список хромосому, не ссылку на её, а копию.
    public void addReserveChromosome(Individual ind) {
        reserveChromosomes.add(new Individual(ind));
    }

    public void printReserveList() {
        for (Individual reserveChromosome : reserveChromosomes) {
            System.out.println(routeToString(matrix, reserveChromosome.getChromomeStructure()) + " ");
        }
    }

    public void singlePointCrossover(Individual parentFirst, Individual parentSecond) {
        int point1;
        int point2;//точки разрыва
        point1 = (int) (Math.random() * (parentFirst.getSizeChromosome() - 2)) + 1;
        point2 = (int) (Math.random() * (parentSecond.getSizeChromosome() - 2)) + 1;

        Individual descendantChromosome1 = new Individual(parentFirst.getDescendantChromosome(point1, point2, parentSecond), matrix, b);
        Individual descendantChromosome2 = new Individual(parentSecond.getDescendantChromosome(point2, point1, parentFirst), matrix, b);

        if (descendantChromosome1.isPath(matrix)) {
            population.addChomosome(descendantChromosome1);
        }
        if (descendantChromosome2.isPath(matrix)) {
            population.addChomosome(descendantChromosome2);
        }
    }

    public void twoPointCrossover(Individual parentFirst, Individual parentSecond) {
        int indexBeginFirst;
        int indexEndFirst;
        int indexBeginSecond;
        int indexEndSecond;

        //определила точки скрещивания для первого родителя.
        int randomFirstPointCrossing = (int) (Math.random() * (parentFirst.getSizeChromosome() - 2)) + 1;
        int randomSecondPointCrossing = (int) (Math.random() * (parentFirst.getSizeChromosome() - 2)) + 1;

        if (randomFirstPointCrossing < randomSecondPointCrossing) {
            indexBeginFirst = randomFirstPointCrossing;
            indexEndFirst = randomSecondPointCrossing;
        } else {
            indexBeginFirst = randomSecondPointCrossing;
            indexEndFirst = randomFirstPointCrossing;
        }

        //определила точки скрещивания для второго родителя.
        randomFirstPointCrossing = (int) (Math.random() * (parentSecond.getSizeChromosome() - 2)) + 1;
        randomSecondPointCrossing = (int) (Math.random() * (parentSecond.getSizeChromosome() - 2)) + 1;

        if (randomFirstPointCrossing < randomSecondPointCrossing) {
            indexBeginSecond = randomFirstPointCrossing;
            indexEndSecond = randomSecondPointCrossing;
        } else {
            indexBeginSecond = randomSecondPointCrossing;
            indexEndSecond = randomFirstPointCrossing;
        }

        Individual descendantChromosome1 = new Individual(parentFirst);
        Individual descendantChromosome2 = new Individual(parentSecond);
        descendantChromosome2.changeChromosome(parentFirst.getChromomeStructure().subList(indexBeginFirst, indexEndFirst + 1), indexBeginSecond, indexEndSecond);
        descendantChromosome1.changeChromosome(parentSecond.getChromomeStructure().subList(indexBeginSecond, indexEndSecond + 1), indexBeginFirst, indexEndFirst);

        descendantChromosome1.removeDublicatesVertex();
        descendantChromosome2.removeDublicatesVertex();

        if (descendantChromosome1.isPath(matrix)) {
            population.addChomosome(descendantChromosome1);
        }
        if (descendantChromosome2.isPath(matrix)) {
            population.addChomosome(descendantChromosome2);
        }
    }

    public int getN() {
        return n;
    }

    public String getSelectionType() {
        return selectionType.toString().replace('_', ' ');
    }

    public String getСrossingType() {
        return crossingType.toString().replace('_', ' ');
    }

    public String getMutationType() {
        return mutationType.toString().replace('_', ' ');
    }

    public String getChoiceParents() {
        return choiceParents.toString().replace('_', ' ');
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setMutationProbability(double probability) {
        mutationProbability = probability;
    }

    public void setSelectionType(String selectionType) {
        this.selectionType = SelectionType.valueOf(selectionType.replace(' ', '_'));
    }

    public void setСrossingType(String crossingType) {
        this.crossingType = CrossingType.valueOf(crossingType.toUpperCase().replace(' ', '_'));
    }

    public void setMutationType(String mutationType) {
        this.mutationType = MutationType.valueOf(mutationType.toUpperCase().replace(' ', '_'));
    }

    public void setChoiceParents(String choiceParents) {
        this.choiceParents = ChoiceOfParents.valueOf(choiceParents.toUpperCase().replace(' ', '_'));
    }
}
