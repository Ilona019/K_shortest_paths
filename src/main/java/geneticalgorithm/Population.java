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
    
    public void replaceChromosomeAtIndex(int index, Individual newIndividual){
        population.set(index, newIndividual);
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
            if (population.get(i).getLengthRoute() > indexMax) {
                indexMax = i;
            }
        }
        return indexMax;
    }

    public void setListPopulation(LinkedList<Individual> list) {
        population = list;
    }

    //Ищет особь с наиболее близким по длине маршрута, возвращает её индекс в популяции.
    public int indexNearbyRoute(int index) {
        int path = population.get(index).getLengthRoute();
        int minDifference = population.get(indexMaxRoute()).getLengthRoute();
        int indexNear = index;
        
        for (int i = 0; i < population.size(); i++) {
            if (i != index && Math.abs(population.get(i).getLengthRoute() - path) < minDifference) {
                minDifference  = Math.abs(population.get(i).getLengthRoute() - path);
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

    public String convertRoutesToString(GenerationMatrix m) {
        String str = "";
        for (int i = 0; i < population.size(); i++) {
            str += i+")"+ "Route == " + population.get(i).getLengthRoute() + ";\t" + routeToString(m, population.get(i).getChromomeStructure()) + "\n";
        }
        return str;
    }
}
