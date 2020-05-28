package main;

import antcolonyalgorithm.Ant;
import antcolonyalgorithm.AntColonyAlgorithmEditDialog;
import antcolonyalgorithm.AntColonyOptimisation;
import grapheditor.GenerationMatrix;
import geneticalgorithm.GeneticAlgorithm;
import geneticalgorithm.GeneticAlgorithmEditDialog;
import geneticalgorithm.Individual;
import geneticalgorithm.Population;
import geneticalgorithm.RouteComparator;
import grapheditor.GraphElements;
import grapheditor.VisualizationViewerGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import jeneticsga.JeneticsGA;

/**
 * @author Илона
 */
public class K_shortcut extends Application {

    private Font font;
    private Label lk;
    private Label lb;
    private TextField k;
    private TextField b;
    private Label ls;
    private Label lt;
    private TextField s;
    private TextField t;
    private Button btnJeneticsRun;
    private Button btnRun;
    private Button btnGeneticAlgorithm;
    private Button btnAntColonyAlgorithm;
    private GenerationMatrix matrix;
    private Population population;
    private VisualizationViewerGraph visGraph;
    public GeneticAlgorithm gAlg;
    public JeneticsGA jeneticsGA;
    public AntColonyOptimisation antColony;
    private ValidateInput validationInput;
    private ArrayList<LinkedList<Integer>> listRoutes;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("General parameters task finding the k shortest paths in a graph");
        primaryStage.setScene(createScene());
        primaryStage.show();
        visGraph = new VisualizationViewerGraph(s, t);
        listRoutes = new ArrayList<>();

        validationInput = new ValidateInput(visGraph);
        visGraph.setValidationInput(validationInput);

        settingListenersForST();

        gAlg = new GeneticAlgorithm();
        jeneticsGA = new JeneticsGA();
        btnGeneticAlgorithm.setOnAction((ActionEvent eventGA) -> {
            if (!validationInput.checkWindowСonditionTask(s, t, k, b)) {
                GeneticAlgorithmEditDialog gaDialog = new GeneticAlgorithmEditDialog(visGraph, gAlg, jeneticsGA);

                btnJeneticsRun = gaDialog.getJeneticBtn();
                btnRun = gaDialog.getBtnRun();

                btnRun.setOnAction((ActionEvent eventRun) -> {
                    if (!validationInput.checkDialogGeneticAlgorithm(k, gaDialog.getNumberPopulation(), gaDialog.getMutationProbability())) {
                        resetGraphParameters();

                        matrix = new GenerationMatrix(visGraph.getGraph(), s.getText(), t.getText());
                        visGraph.setMatrix(matrix);
                        performGeneticAlgorithm(gaDialog);

                        displayRoutesOnGraph();

                        writeResponseToFile(population.convertRoutesToString(matrix));
                    }
                });


                btnJeneticsRun.setOnAction((ActionEvent eventRun) -> {
                            if (!validationInput.checkDialogGeneticAlgorithm(k, gaDialog.getNumberPopulation(), gaDialog.getMutationProbability())) {
                                resetGraphParameters();

                                matrix = new GenerationMatrix(visGraph.getGraph(), s.getText(), t.getText());
                                visGraph.setMatrix(matrix);

                                JeneticsGA jga = new JeneticsGA(matrix, Integer.parseInt(gaDialog.getNumberPopulation().getText()), Integer.parseInt(k.getText()), Integer.parseInt(b.getText()), gaDialog.getComboBoxCrossingJenetics().getValue(), gaDialog.getComboBoxMutationTypeJenetics().getValue(), gaDialog.getComboBoxSelectionJenetics().getValue(), Double.parseDouble(gaDialog.getMutationProbability().getText()));
                                JeneticsGA.runAlgorithm();

                                listRoutes = jga.getPopulationChromosomes();

                                gaDialog.setResult(listRoutes.size());
                                gaDialog.setDurationAlgorithm(jga.getDurationAlg());
                                gaDialog.setNumGenerations(jga.numberGeneration());

                                displayRoutesOnGraph();


                                writeResponseToFile(jga.convertRoutesToString(matrix));
                            }
                        }
                );
                gaDialog.getDialog().showAndWait();
            }
        });

        antColony = new AntColonyOptimisation();
        btnAntColonyAlgorithm.setOnAction((ActionEvent eventACO) -> {
            if (!validationInput.checkWindowСonditionTask(s, t, k, b)) {
                AntColonyAlgorithmEditDialog antDialog = new AntColonyAlgorithmEditDialog(visGraph, antColony, k);
                btnRun = antDialog.getBtnRun();
                btnRun.setOnAction((ActionEvent eventRun) -> {
                    if (!validationInput.checkDialogAntColonyOptimization(k, antDialog.getColonySize(), antDialog.getAlpha(), antDialog.getBetta(), antDialog.getEvaporation(), antDialog.getMaxIterations(), antDialog.getQ())) {
                        resetGraphParameters();
                        matrix = new GenerationMatrix(visGraph.getGraph(), s.getText(), t.getText());
                        visGraph.setMatrix(matrix);
                        antColony = new AntColonyOptimisation(Integer.parseInt(antDialog.getColonySize().getText()), Integer.parseInt(antDialog.getAlpha().getText()), Integer.parseInt(antDialog.getBetta().getText()), Double.parseDouble(antDialog.getEvaporation().getText()), matrix, Integer.parseInt(antDialog.getMaxIterations().getText()), Integer.parseInt(b.getText()), Integer.parseInt(k.getText()), Integer.parseInt(antDialog.getQ().getText()));

                        antColony.startAntOptimization();
                        formListRoutesFromColonyAnts(antColony.getAntsColonyBest());

                        antDialog.setResult(antColony.getAntsColonyBest().size());
                        antDialog.setCountIterations(antColony.getCountIterations());
                        antDialog.setCountIterationsOnBestColony(antColony.getCountIterationsOnBestColony());
                        antDialog.setDurationAlgorithm(antColony.getDuration());

                        displayRoutesOnGraph();


                        writeResponseToFile(antColony.convertRoutesToString(matrix));
                    }
                });

                antDialog.getDialog().showAndWait();
            }
        });

    }

    public void performGeneticAlgorithm(GeneticAlgorithmEditDialog gaDialog) {
        long old = System.currentTimeMillis(); // time start
        Individual chromosome;
        population = new Population();
        int generationN = 0;//Количество поколений
        RouteComparator myRouteComparator = new RouteComparator();
        for (int i = 0; i < Integer.parseInt(gaDialog.getNumberPopulation().getText()); i++) {
            chromosome = new Individual(visGraph.getGraph().getVertexCount(), matrix.getS(), matrix.getT(), matrix, Integer.parseInt(b.getText()));
            population.addChomosome(chromosome);
        }
        gAlg = new GeneticAlgorithm(matrix, population, gaDialog.getComboBoxParents().getValue(), gaDialog.getComboBoxCrossingTypes().getValue(), gaDialog.getComboBoxMutationTypes().getValue(), gaDialog.getComboBoxSelectionTypes().getValue(), Integer.parseInt(b.getText()), Integer.parseInt(gaDialog.getNumberPopulation().getText()), Double.parseDouble(gaDialog.getMutationProbability().getText()));
        for (int i = 0; i < population.size(); i++)//Поместили в резерв хромосомы из начальной популяции
        {
            if (population.getAtIndex(i).getFitnessF() && !gAlg.existInReserve(population.getAtIndex(i))) {
                gAlg.addReserveChromosome(population.getAtIndex(i));
            }
        }
        if (gAlg.getReserveChromosomes().size() >= Integer.parseInt(k.getText())) {//нашлось решение на первом шаге
        } else {
            while (gAlg.getReserveChromosomes().size() < Integer.parseInt(k.getText())) {
                generationN++;
                if (generationN >= 1000) {
                    break;
                }
                gAlg.choiceParents();
                gAlg.crossing();
                gAlg.mutation();
                gAlg.selection();
                Individual replace_ind;
                int count_replace = 0;//Число замен
                ArrayList<Integer> vertexDublicate = new ArrayList<>();//список вершин, заменили из резерва
                if (!gAlg.getReserveChromosomes().isEmpty()) {
                    for (int i = 0; i < population.size(); i++) {
                        for (int j = i + 1; j < population.size(); j++) {
                            if (population.getAtIndex(i).equalsChromosome(population.getAtIndex(j))) {
                                replace_ind = gAlg.returnItemDifferentOthers(population); //Вернуть отличный от других в популяции элемент из резерва.
                                if (replace_ind != null) {
                                    population.replaceChromosomeAtIndex(j, new Individual(replace_ind));//Заменить повторяющийся уникальным для популяции. 
                                    vertexDublicate.add(j);
                                    count_replace++;
                                    if (count_replace == gAlg.getReserveChromosomes().size()) {//"кончились" элементы резерва для замены.
                                        break;
                                    }
                                }
                            }
                        }
                        if (count_replace == gAlg.getReserveChromosomes().size()) {
                            break;
                        }
                    }
                }

                //Замена худщих хромосом из резерва
                if (count_replace != gAlg.getReserveChromosomes().size()) {
                    for (int i = 0; i < population.size(); i++) {
                        if (!population.getAtIndex(i).getFitnessF()) {
                            replace_ind = gAlg.returnItemDifferentOthers(population); //Вернуть отличный от других в популяции элемент из резерва.
                            if (replace_ind != null) {
                                population.replaceChromosomeAtIndex(i, new Individual(replace_ind));//Заменить повторяющийся уникальным для популяции. 
                                vertexDublicate.add(i);
                                count_replace++;
                                if (count_replace == gAlg.getReserveChromosomes().size()) {//"кончились" элементы резерва для замены.
                                    break;
                                }
                            }
                        }
                    }
                }
                //Поместим отличные от хромосом в резерве, хромосомы из популяции, удовлетворяющие фитнесс ф-ции
                for (int i = 0; i < population.size(); i++) {
                    if (population.getAtIndex(i).getFitnessF() && !vertexDublicate.contains(i)) {
                        if (!gAlg.existInReserve(population.getAtIndex(i))) {
                            gAlg.addReserveChromosome(population.getAtIndex(i));
                        }
                    }
                }
            }
        }
        population.setListPopulation(gAlg.getReserveChromosomes());
        population.getPopulation().sort(myRouteComparator);

        formListRoutes(population);
        gaDialog.setResult(population.size());
        gaDialog.setNumGenerations(generationN);
        gaDialog.setDurationAlgorithm((System.currentTimeMillis() - old) / 1000F);
    }

    public void writeResponseToFile(String textRoutes) {
        try (FileWriter writer = new FileWriter("result.txt", false)) {
            String text;
            text = textRoutes;
            writer.write(text);
            writer.append('\n');
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    //остановить окно Swing вместе с окном javaFx
    @Override
    public void stop() {
        System.exit(0);
    }

    public Scene createScene() {
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        font = javafx.scene.text.Font.font("TimesRoman", FontWeight.BOLD, 15);
        lk = new Label("Enter the number of shortest paths:\t\t\t\t\t  K = ");
        lk.setFont(font);
        root.add(lk, 0, 0);
        k = new TextField();
        k.setText("5");
        root.add(k, 1, 0);
        lb = new Label("The length of the edge no more then B:\t\t\t\t  B = ");
        lb.setFont(font);
        root.add(lb, 0, 1);
        b = new TextField();
        b.setText("100");
        root.add(b, 1, 1);
        ls = new Label("Enter the beginning of the path(first vertex number):\t  s = ");
        ls.setFont(font);
        lt = new Label("Enter the end of the path(last vertex number):\t\t\t  t = ");
        lt.setFont(font);
        s = new TextField();
        s.setText("0");
        t = new TextField();
        t.setText("4");
        root.add(ls, 0, 3);
        root.add(s, 1, 3);
        root.add(lt, 0, 4);
        root.add(t, 1, 4);
        DropShadow shadow = new DropShadow();
        btnGeneticAlgorithm = new Button();
        btnGeneticAlgorithm.setText("Genetic algorithm");
        btnGeneticAlgorithm.setPadding(new Insets(20, 20, 20, 20));
        btnGeneticAlgorithm.setFont(font);
        btnGeneticAlgorithm.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        btnGeneticAlgorithm.setEffect(shadow);

        btnAntColonyAlgorithm = new Button();
        btnAntColonyAlgorithm.setText("Ant colony algorithm");
        btnAntColonyAlgorithm.setPadding(new Insets(20, 20, 20, 20));
        btnAntColonyAlgorithm.setFont(font);
        btnAntColonyAlgorithm.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        btnAntColonyAlgorithm.setEffect(shadow);

        root.add(btnGeneticAlgorithm, 0, 10);
        root.add(btnAntColonyAlgorithm, 1, 10);
        return new Scene(root, 700, 450);
    }

    private void settingListenersForST() {
        s.textProperty().addListener((observable, oldValue, newValue) -> {
            if (validationInput.isNonNegativeNumber(s.getText())) {
                visGraph.repainFrame();
            }
        });

        t.textProperty().addListener((observable, oldValue, newValue) -> {
            if (validationInput.isNonNegativeNumber(t.getText())) {
                visGraph.repainFrame();
            }
        });
    }

    private void formListRoutes(Population population) {
        for (int i = 0; i < population.size(); i++) {
            listRoutes.add(population.getAtIndex(i).getChromomeStructure());
        }
    }

    private void resetGraphParameters() {
        visGraph.deleteParalEdges();

        for (GraphElements.MyEdge e : visGraph.getGraph().getEdges()) {
            if (e.getFlagPaint() == 1) {
                e.setFlagPaint(0);
            }
        }
        visGraph.setChEdgeList(null);
        visGraph.setPaintedEdgeslist(null);
        visGraph.setListOfShortcut(null);
        listRoutes.clear();

        visGraph.repainFrame();

    }

    private void formListRoutesFromColonyAnts(LinkedList<Ant> antsColonyBest) {
        for (Ant ant : antsColonyBest) {
            listRoutes.add(ant.getRoute());
        }
    }

    private void displayRoutesOnGraph() {
        visGraph.setListOfShortcut(listRoutes);
        visGraph.addParalEdges(Integer.parseInt(k.getText()));
        visGraph.repainFrame();
    }

}
