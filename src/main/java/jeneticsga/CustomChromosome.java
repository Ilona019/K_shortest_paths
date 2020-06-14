package jeneticsga;

import geneticalgorithm.Individual;
import grapheditor.GenerationMatrix;
import io.jenetics.*;
import io.jenetics.util.ISeq;

import java.util.Iterator;
import java.util.LinkedList;

public class CustomChromosome extends Individual implements Chromosome<IntegerGene>{

    private ISeq<IntegerGene> iSeq;
    private final int length;
    private GenerationMatrix matrix;

    public CustomChromosome(ISeq<IntegerGene> genes, GenerationMatrix m) {
        super();
        this.iSeq = genes;
        this.length = iSeq.length();
        this.matrix = m;
    }

    public static CustomChromosome of(ISeq<IntegerGene> genes, GenerationMatrix m) {
        return new CustomChromosome(genes, m);
    }

    @Override
    public Chromosome<IntegerGene> newInstance(ISeq<IntegerGene> iSeq) {
        return new CustomChromosome(iSeq, matrix);
    }

    @Override
    public ISeq<IntegerGene> toSeq() {
        return iSeq;
    }

    @Override
    public IntegerGene getGene(int index) {
        return iSeq.get(index);
    }

    @Override
    public int length() {
        return iSeq.length();
    }

    @Override
    public Chromosome<IntegerGene> newInstance() {
        ISeq<IntegerGene> genes = ISeq.empty();

        LinkedList<Integer> chromosome = generateNewChromosome(matrix.getCountVerteces(), matrix, matrix.getS(), matrix.getT());

        for (int i = 0; i < chromosome.size(); i++)
        {
            IntegerGene gene = IntegerGene.of(chromosome.get(i), 1 ,1);
            genes = genes.append(ISeq.of(gene));
        }
        return new CustomChromosome(genes, matrix);
    }


    @Override
    public Iterator<IntegerGene> iterator() {
        return iSeq.iterator();
    }

    //Представляет ли хромосома путь.
    public boolean isPath() {
        for (int j = 0; j < length - 1; j++) {
            if (matrix.getWeight(iSeq.get(j).allele(), iSeq.get(j+1).allele()) == 0 || matrix.getS() != iSeq.get(0).allele()|| matrix.getT()!= iSeq.get(length-1).allele() )//нет ребра между вершинами => хромосома не образует путь;
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValid() {
        return isPath();
    }

}

