package geneticalgorithm;

import grapheditor.GenerationMatrix;
import main.ConvertRouteToString;

import java.util.LinkedList;
import java.util.ListIterator;

/**
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

    public void replaceChromosomeAtIndex(int index, Individual newIndividual) {
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

    //Возращает индекс индивида непохожий по длине хромосомы.
    public int indexDifferenceChromosome(int indexCurrentIndividual) {
        int differenceIndex = indexCurrentIndividual;
        int maxDifference = 0;
        int currentDifference;
        LinkedList<Integer> listDifferenceChromosome = new LinkedList<>();
        for (int i = (int) (Math.random() * (population.size() - 1)); i < population.size(); i++) {
            currentDifference = Math.abs(population.get(i).getSizeChromosome() - population.get(indexCurrentIndividual).getSizeChromosome());
            if (i != indexCurrentIndividual && currentDifference >= maxDifference) {
                maxDifference = currentDifference;
                differenceIndex = i;
                listDifferenceChromosome.add(i);
            }
        }
        if (!listDifferenceChromosome.isEmpty()) {
            if (listDifferenceChromosome.size() > 10) {
                differenceIndex = listDifferenceChromosome.get((int) (Math.random() * (listDifferenceChromosome.size() - listDifferenceChromosome.size() / 2)) + listDifferenceChromosome.size() / 2);
            } else {
                differenceIndex = listDifferenceChromosome.get((int) (Math.random() * listDifferenceChromosome.size()));
            }
        }
        return differenceIndex;
    }

    //Возращает индекс индивида ближайшего по длине хромосомы.
    public int indexSameChromosome(int indexCurrentIndividual) {
        int sameIndex = indexCurrentIndividual;
        int minDifference = 10000;
        int currentDifference;
        for (int i = (int) (Math.random() * (population.size() - 1)); i < population.size(); i++) {
            currentDifference = Math.abs(population.get(i).getSizeChromosome() - population.get(indexCurrentIndividual).getSizeChromosome());
            if (i != indexCurrentIndividual && currentDifference < minDifference) {
                minDifference = currentDifference;
                sameIndex = i;
            }
            if (i != indexCurrentIndividual && population.get(i).getSizeChromosome() == population.get(indexCurrentIndividual).getSizeChromosome()) {
                return i;
            }
        }
        return sameIndex;
    }

    public void setListPopulation(LinkedList<Individual> list) {
        population = list;
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
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < population.size(); i++) {
            str.append(i).append(")").append("Route == ").append(population.get(i).getLengthRoute()).append(";\t").append(routeToString(m, population.get(i).getChromomeStructure())).append("\n");
        }
        return str.toString();
    }
}
