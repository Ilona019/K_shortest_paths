package geneticalgorithm;

import grapheditor.GenerationMatrix;
import grapheditor.GraphElements;
import grapheditor.GraphElements.MyEdge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Илона
 */
public class Individual {

    private LinkedList<Integer> chromosome;//1 хромосома
    private int route;
    private boolean fitnessF;

    public Individual(LinkedList<Integer> descendantChromosome, GenerationMatrix matrix, int b) {
        chromosome = descendantChromosome;
        route = fitnessFunctionPath(matrix);
        fitnessF = fitnessFunctionWeight(b);
    }

    public Individual(int countVertex, int s, int t, GenerationMatrix matrix, int b) {
        chromosome = new LinkedList<>();
        if (matrix.getCountVerteces() < 2) {
            return;
        }

        chromosome = generateNewChromosome(countVertex, matrix, s, t);
        route = fitnessFunctionPath(matrix);
        fitnessF = fitnessFunctionWeight(b);
    }

    //Конструктор для создания копии объекта
    public Individual(Individual indCopy) {
        chromosome = (LinkedList<Integer>) indCopy.chromosome.clone();
        route = indCopy.getRoute();
        fitnessF = indCopy.getFitnessF();
    }

    public Individual() {
    }

    public LinkedList<Integer> generateNewChromosome(int countVertex, GenerationMatrix matrix, int s, int t) {
        LinkedList<Integer> newChromosome = new LinkedList<>();
        boolean path = false;//флаг, получился ли путь от s до t?
        while (!path) {
            int i = countVertex - 1;

            if (matrix.getCountVerteces() == 2) {
                i = 1;
            }

            int pred = s;
            newChromosome.addFirst(s);
            Random random = new Random();
            while (i != 1) {
                int verRandom = random.nextInt(countVertex - 1);
                if (verRandom != s && verRandom != t && matrix.getWeight(pred, verRandom) != 0) {
                    pred = verRandom;
                    newChromosome.add(verRandom);
                    i--;
                } else if (matrix.getWeight(verRandom, t) != 0) {
                    break;
                }
            }
            //Collections.shuffle(chromosome);//переставить случайным образом массив генов.

            if (matrix.getWeight(pred, t) != 0) {//хромосома образует путь
                newChromosome.add(t);
                path = true;
                newChromosome = removeDublicatesVertex(newChromosome);
            } else {
                newChromosome.clear();

            }

        }
        return newChromosome;
    }

    public void cutPartChromosome(int startIndex, int endIndex) {
        for (int k = startIndex, count = 0; endIndex - startIndex != count; count++) {
            chromosome.remove(k);
        }
    }


    //Представляет ли хромосома путь.
    public boolean isPath(GenerationMatrix matrix) {
        for (int j = 0; j < chromosome.size() - 1; j++) {
            if (matrix.getWeight(chromosome.get(j), chromosome.get(j + 1)) == 0 || matrix.getS() != chromosome.getFirst() || matrix.getT() != chromosome.getLast())//нет ребра между вершинами, хромосома не образует путь;
            {
                return false;
            }
        }
        return true;
    }

    //Убирать дубликаты вершин, стоящие рядом;
    public LinkedList<Integer> removeDublicatesVertex(LinkedList<Integer> newChromosome) {
        for (int j = 1, curSize = newChromosome.size(); j < curSize - 2; j++) {
            if (Objects.equals(newChromosome.get(j), newChromosome.get(j + 1))) {
                newChromosome.remove(j);
                curSize--;
                j--;
            }
        }
        return newChromosome;
    }

    //Целевая функция - длина маршрута.
    public int fitnessFunctionPath(GenerationMatrix matrix) {
        int path = 0;
        for (int i = 0; i < chromosome.size() - 1; i++) {
            path += matrix.getWeight(chromosome.get(i), chromosome.get(i + 1));
        }
        return path;
    }

    public void recalculateFitnessFunc(GenerationMatrix matrix, int b) {
        route = fitnessFunctionPath(matrix);
        fitnessF = fitnessFunctionWeight(b);
    }

    //Вес пути не превосходит В?
    public boolean fitnessFunctionWeight(int b) {
        return route <= b;
    }

    //Наибольшая по длине хромосома из 2 - х, первый элемент массива наибольший
    public ArrayList<Individual> maxLengthInd1AndInd2(Individual ind2) {
        ArrayList<Individual> mas = new ArrayList<>();
        if (this.getChromomeStructure().size() <= ind2.getChromomeStructure().size()) {
            mas.add(ind2);
            mas.add(this);
        } else {
            mas.add(this);
            mas.add(ind2);
        }
        return mas;
    }

    //Содержит хромосома номер вершины, начиная с номера index? Да - вернуть её номер
    public int isNumberVertex(int v, int index) {
        for (int i = index; i < chromosome.size() - 1; i++) {
            if (chromosome.get(i) == v) {
                return i;
            }
        }
        return -1;
    }

    //Заменить фраграмент [indexBegin indexEnd] хромосомы на передаваемый фрагмент списка.
    public void changeChromosome(List<Integer> fragmentList, int indexBegin, int indexEnd) {
        for (int i = indexBegin; i <= indexEnd; i++)
            this.getChromomeStructure().remove(indexBegin);
        this.getChromomeStructure().addAll(indexBegin, fragmentList);
    }

    //Получить хромосому будущего потомка в виде списка
    public LinkedList<Integer> getDescendantChromosome(int point, Individual parent2) {
        LinkedList<Integer> dCh = new LinkedList<>();
        for (int i = 0; i < parent2.getChromomeStructure().size(); i++) {
            if (i <= point) {
                dCh.add(this.getChromomeStructure().get(i));
            } else {
                dCh.add(parent2.getChromomeStructure().get(i));
            }
        }
        return dCh;
    }

    //Хромосомы совпадают?    
    public boolean equalsChromosome(Individual ind2) {
        String str1 = this.getChromomeStructure().toString();
        String str2 = ind2.getChromomeStructure().toString();
        return str1.equals(str2);
    }

    //мутация в гене, вставить случайную вершину.
    public boolean mutation(GenerationMatrix matrix, int b, double mutationProbability) {
        if (matrix.getCountVerteces() == 2) {
            return false;
        }

        int randomVertex;
        int positionChromosome;

        for (int i = 1; i < chromosome.size() - 1; i++) {
            double random = Math.random();
            if (random >= mutationProbability) {
                if (chromosome.size() != 2) {
                    positionChromosome = i;
                    do {
                        randomVertex = (int) (Math.random() * matrix.getCountVerteces());
                    } while (randomVertex == chromosome.get(0) || randomVertex == chromosome.getLast());

                    chromosome.add(positionChromosome, randomVertex);
                    chromosome.remove(positionChromosome + 1);
                }
            }
        }

        //   вставить на случайную позицию вершину
        if (chromosome.size() == 2)
            positionChromosome = 1;
        else
            positionChromosome = (int) (Math.random() * (chromosome.size() - 3)) + 1;
        do {
            randomVertex = (int) (Math.random() * matrix.getCountVerteces());
        } while (randomVertex == chromosome.get(0) || randomVertex == chromosome.getLast());

        chromosome.add(positionChromosome, randomVertex);


        removeDublicatesVertex(chromosome);
        if (isPath(matrix)) {
            route = fitnessFunctionPath(matrix);
            fitnessF = fitnessFunctionWeight(b);
            return true;
        }
        return false;
    }

    public LinkedList<Integer> getChromomeStructure() {
        return chromosome;
    }

    public int getRoute() {
        return this.route;
    }

    public boolean getFitnessF() {
        return fitnessF;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Integer ch : chromosome) {
            str.append(ch).append(" ");
        }
        return str.toString();
    }

}
