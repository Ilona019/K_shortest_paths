package geneticalgorithm;

import grapheditor.GenerationMatrix;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Илона
 */
public class GeneticAlgorithm {

    private Population population;
    private int[] masPair;
    private ChoiceOfParents choiceParents;//Тип оператора отбора родителей.
    private CrossingType crossingType;//Тип скрещивания.
    private SelectionType selectionType;//Тип селекции.
    private GenerationMatrix matrix;
    private int b;
    private int n;
    private LinkedList<Individual> reserveChromosomes;
    
    public GeneticAlgorithm() {
        this.choiceParents = ChoiceOfParents.valueOf("Panmixia".toUpperCase().replace(' ', '_'));
        this.crossingType = CrossingType.valueOf("Single point crossover".toUpperCase().replace(' ', '_'));
        this.selectionType = SelectionType.valueOf("Elite".toUpperCase().replace(' ', '_'));
        this.n = 20;
    }

    public GeneticAlgorithm(GenerationMatrix matrix, Population population, String choiceParents, String crossingType, String selectionType, int b, int n) {
        this.matrix = matrix;
 
        this.population = population;
        masPair = new int[2 * population.size()];
        this.choiceParents = ChoiceOfParents.valueOf(choiceParents.replace(' ', '_'));
        this.crossingType = CrossingType.valueOf(crossingType.replace(' ', '_'));
        this.selectionType = SelectionType.valueOf(selectionType.replace(' ', '_'));
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
                    if (population.getPopulation().getLast().fitnessFunctionWeight(b) == false) {
                        population.getPopulation().removeLast();//удаляем хромосомы не удовлетворяющие условию путь < B.
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
        SINGLE_POINT_CROSSOVER, TWO_POINT_CROSSOVER
    }
    //Оператор скрещивания родителей.

    public void crossing() {
        Individual indMin, indMax;//по длине хромосомы
        ArrayList<Individual> arr = new ArrayList<>();
        int point1 = -2;
        int point2 = -2;//точки разрыва
        switch (crossingType) {
            case SINGLE_POINT_CROSSOVER://Если  в хромосоме есть две одинаковые вершины, отличные от начала и конца и вершин соседних с ними, то померять местами их концы. 
                for (int i = 1; i < masPair.length - 1; i += 2) {
                    if (masPair[i] != masPair[i - 1]) {//если пара не образуется сама с собой
                        arr = population.getAtIndex(masPair[i]).maxLengthInd1AndInd2(population.getAtIndex(masPair[i - 1]));
                        indMax = arr.get(0);//Наибольшая по длине хромосома
                        indMin = arr.get(1);//Наименьшая по длине хромосома
                        for (int j = 2; j < indMax.getChromomeStructure().size() - 2; j++) {
                            point1 = j;
                            if (indMin.isNumberVertex(indMax.getChromomeStructure().get(point1), 1) != -1) {
                                point2 = indMin.isNumberVertex(indMax.getChromomeStructure().get(point1), 1);
                                //формируем 2 - х потомков и добавляем в популяцию
                                population.addChomosome(new Individual(indMax.getDescendantChromosome(point1, indMax), matrix, b));
                                population.addChomosome(new Individual(indMin.getDescendantChromosome(point2, indMin), matrix, b));
                                break;
                            }
                        }
                    }
                }
                break;
            case TWO_POINT_CROSSOVER:
                for (int i = 1; i < masPair.length - 1; i += 2) {
                    if (masPair[i] != masPair[i - 1]) {//если пара не образуется сама с собой      
                        twoPointCrossover(population.getAtIndex(masPair[i]),population.getAtIndex(masPair[i-1]));
                }
                break;
        }
    }
}
    
    //Оператор мутации потомков.
    public void mutation() {
        Individual currentChromosome;
                   
                   
        int point2;//индекс эквивалентной вершины
        int flag;
        if (reserveChromosomes.isEmpty()) {
            flag = 0;//сокращаем хромосому до конца
        } else {
            flag = 1;
        }
        for (int i = 0; i < population.size(); i++) {
            currentChromosome = population.getAtIndex(i);
            //System.out.println( "cur ch = "+i+" "+ currentChromosome.printChromosome(matrix));
            for (int point1 = 1; point1 < currentChromosome.getChromomeStructure().size() - 1; point1++) {
                
                
                if (currentChromosome.isNumberVertex(currentChromosome.getChromomeStructure().get(point1), point1 + 1) != -1) {//есть ли начиная с индекса point1, вершина ch.get_list_chromosome().get(point1)
                    point2 = currentChromosome.isNumberVertex(currentChromosome.getChromomeStructure().get(point1), point1 + 1);
                    if (!existInReserve(currentChromosome) && currentChromosome.getFitnessF() == true)//сохраним в резерв хорошую хромосому по приспособленности до мутации, если её нет в резерве
                    {
                        this.addReserveChromosome(currentChromosome);
                    }
                    printReserveList();
                    currentChromosome.cutPartChromosome(point1, point2);
                    printReserveList();
                    currentChromosome.recalculateFitnessFunc(matrix, b);//изменилась длина маршрута и надо пересчитать фитнесс функцию
                  //  currentChromosome.formEdgesList(matrix);
                    if (flag == 1) {
                        break;
                    }
                }
            }
            Individual chromosomeAfterMutation;
            int j = 0;
            while(j++ < 3){
               chromosomeAfterMutation = new Individual(currentChromosome);
               if( chromosomeAfterMutation.mutation(matrix, b)) {
                        if (currentChromosome.getFitnessF() == true &&!existInReserve(currentChromosome))
                        {
                            this.addReserveChromosome(currentChromosome);
                        }
                    System.out.println("Изменение случайного гена Индекс: "+i+" До: "+currentChromosome.getChromomeStructure());
                    population.replaceChromosomeAtIndex(i, chromosomeAfterMutation);
                    System.out.println( "После: "+population.getAtIndex(i).getChromomeStructure());
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
        for (int i = 0; i < reserveChromosomes.size(); i++) {
            if (reserveChromosomes.get(i).equalsChromosome(ind)) {
                return true;
            }
        }
        return false;
    }

    //Вернуть индивид из резерва, который отсутствует в популяции.
    public Individual returnItemDifferentOthers(Population population) {
        for (int i = 0; i < reserveChromosomes.size(); i++) {
            if (!population.existInPopulation(reserveChromosomes.get(i))) {
                return reserveChromosomes.get(i);
            }
        }
        return null;
    }

   // Добавить в резервный список хромосому, не ссылку на её, а копию.
    public void addReserveChromosome(Individual ind) {
        reserveChromosomes.add(new Individual(ind));
    }

    public void printReserveList() {
        for (int i = 0; i < reserveChromosomes.size(); i++) {
            System.out.println(reserveChromosomes.get(i).printChromosome(matrix) + " ");
        }
    }
    
    public void twoPointCrossover(Individual parent1, Individual parent2) {
        int indexBeginFirst = -1;
        int indexEndFirst;
        int indexBeginSecond = -1;
        int indexEndSecond;
        Individual parentFirst;
        Individual parentSecond;
        if (parent1.getChromomeStructure().size() > parent2.getChromomeStructure().size()) {
            parentFirst = parent1;
            parentSecond = parent2;
        } else {
            parentFirst = parent2;
            parentSecond = parent1;
        }
        for (int j = 1; j < parentFirst.getChromomeStructure().size() - 1; j++) {
            if (parentSecond.getChromomeStructure().contains(parentFirst.getChromomeStructure().get(j)) == true) {
                if (indexBeginSecond == -1) {
                    indexBeginFirst = j;
                    indexBeginSecond = parentSecond.getChromomeStructure().lastIndexOf(parentFirst.getChromomeStructure().get(j));
                } else if(Math.abs(indexBeginFirst - j) > 1 && Math.abs(indexBeginSecond - parentSecond.getChromomeStructure().lastIndexOf(parentFirst.getChromomeStructure().get(j))) > 1) {
                    indexEndFirst = j;
                    indexEndSecond = parentSecond.getChromomeStructure().lastIndexOf(parentFirst.getChromomeStructure().get(j));
                        System.out.println("$$$ Parent1" + parentFirst.printChromosome(matrix) + indexBeginFirst + " " + indexEndFirst);
                        System.out.println("%%% Parent2" + parentSecond.printChromosome(matrix) + indexBeginSecond + " " + indexEndSecond);
                        Individual descendantChromosome1 = new Individual(parentFirst);
                        Individual descendantChromosome2 = new Individual(parentSecond);
                        int temp;
                        if(indexBeginFirst > indexEndFirst)
                        {
                            temp = indexEndFirst;
                            indexEndSecond = indexBeginFirst;
                            indexBeginFirst = temp;
                        }
                        if(indexBeginSecond > indexEndSecond)
                        {
                            temp = indexEndSecond;
                            indexEndSecond = indexBeginSecond;
                            indexBeginSecond = temp;
                        }
                        descendantChromosome1.changeChromosome(parentSecond.getChromomeStructure().subList(indexBeginSecond+1, indexEndSecond), indexBeginFirst + 1, indexEndFirst - 1);
                        descendantChromosome2.changeChromosome(parentFirst.getChromomeStructure().subList(indexBeginFirst+1, indexEndFirst), indexBeginSecond + 1, indexEndSecond - 1);
                        population.addChomosome(descendantChromosome1);
                        population.addChomosome(descendantChromosome2);
                    break;
                }
            }
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
    
    public String getChoiceParents() {
        return choiceParents.toString().replace('_', ' ');
    }
    
    public void setN(int n) {
         this.n = n;
    }
    
    public void setSelectionType(String selectionType) {
        this.selectionType = SelectionType.valueOf(selectionType.replace(' ', '_'));
    }
    
    public void setСrossingType(String crossingType) {
        this.crossingType =  CrossingType.valueOf(crossingType.toUpperCase().replace(' ', '_'));
    }
    
    public void setChoiceParents(String choiceParents) {
        this.choiceParents = ChoiceOfParents.valueOf(choiceParents.toUpperCase().replace(' ', '_'));
    }
}
