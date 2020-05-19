package geneticalgorithm;

import grapheditor.GenerationMatrix;
import main.ConvertRouteToString;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Илона
 */
public class Population extends ConvertRouteToString {

    private LinkedList<Individual> population;

    public Population() {
        population = new LinkedList<>();
    }

    public void addChomosome(Individual ch) {
        population.add(ch);
    }

    public void deleteChromosomAtIndex(int index) {
        population.remove(index);
    }
    
    public void replaceChromosomeAtIndex(int index, Individual newIndividual){
        population.set(index, newIndividual);
    }
    
    public int getIndexChromosome(Individual chromosome) {
        return population.indexOf(chromosome);
    }
    
    public Individual getAtIndex(int index) {
        return population.get(index);
    }

    public LinkedList<Individual> getPopulation() {
        return population;
    }

    public int size() {
        return this.population.size();
    }

    //Возращает индекс индивида с наибольшим значением маршрута в популяции.
    public int indexMaxRoute() {
        int indexMax = 0;
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getRoute() > indexMax) {
                indexMax = i;
            }
        }
        return indexMax;
    }

    public void setListPopulation(LinkedList<Individual> list) {
        population = list;
//        population.clear();
//        for(Individual item: list){
//            population.add(new Individual(item));
//        }
    }

    //Ищет особь с наиболее близким по длине маршрута, возвращает её индекс в популяции.
    public int indexNearbyRoute(int index) {
        int path = population.get(index).getRoute();
        int minDifference = population.get(indexMaxRoute()).getRoute();
        int indexNear = index;
        
        for (int i = 0; i < population.size(); i++) {
            if (i != index && Math.abs(population.get(i).getRoute() - path) < minDifference) {
                minDifference  = Math.abs(population.get(i).getRoute() - path);
                indexNear = i;
            }
        }
        return indexNear;
    }

    //количество хромосом, удовлетворяющих условию окончания алгоритма
    public int countGoodChromosome(int b) {
        int count = 0;
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).getFitnessF()) {
                count++;
            }
        }
        return count;
    }

    //Существует ли в population такой индивид ind?
    public boolean existInPopulation(Individual ind) {
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).equalsChromosome(ind)) {
                return true;
            }
        }
        return false;
    }

    public void printPopulation(GenerationMatrix m) {
        ListIterator<Individual> iteratorP = population.listIterator();
        int i = 0;
        Individual currentChr;
        while (iteratorP.hasNext()) {
            currentChr = iteratorP.next();
            System.out.println(i++ + ")  route == " + currentChr.getRoute() + "\t" + routeToString(m, currentChr.getChromomeStructure()) + "\n (weight < B) ? = " + currentChr.getFitnessF() + "\n");
        }
    }

    public String convertRoutesToString(GenerationMatrix m) {
        String str = "";
        for (int i = 0; i < population.size(); i++) {
            str += i+")"+ "Route == " + population.get(i).getRoute() + ";\t" + routeToString(m, population.get(i).getChromomeStructure()) + "\n";
        }
        return str;
    }
}
