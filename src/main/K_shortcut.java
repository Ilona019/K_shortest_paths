package main;

import geneticalgorithm.GenerationMatrix;
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
import javafx.application.Application;
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

/**
 *
 * @author Илона
 */
public class K_shortcut extends Application {
//Для вызова понели вывода нажала окно, сбросить окна.

    private Font font;
    private Label lk;
    private Label lb;
    private TextField k;
    private TextField b;
    private Label ls;
    private Label lt;
    private TextField s;
    private TextField t;
    private Button btnRun;
    private Button btnGeneticAlgorithm;
    private GenerationMatrix matrix;
    private static Population population;
    private VisualizationViewerGraph visGraph;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("General patameters task finding the k shortest paths in a graph");
        primaryStage.setScene(createScene());
        primaryStage.show();
        visGraph = new VisualizationViewerGraph(s, t);

        ValidateInput validationInput = new ValidateInput(visGraph);

        btnGeneticAlgorithm.setOnAction((javafx.event.ActionEvent eventGA) -> {
            validationInput.checkGeneraParametersTask(s, t, k, b);
            if (!validationInput.isInputErrors()) {
                GeneticAlgorithmEditDialog gaDialog = new GeneticAlgorithmEditDialog(visGraph);

                btnRun = gaDialog.getBtnRun();
                btnRun.setOnAction((javafx.event.ActionEvent eventRun) -> {
                    validationInput.checkParametersGeneticAlgorithm(k, s, t, gaDialog.getComboBoxParents(), gaDialog.getComboBoxCrossingTypes(), gaDialog.getComboBoxSelectionTypes(), gaDialog.getNumberPopulation());
                    if (!validationInput.isInputErrors()) {
                        visGraph.deleteParalEdges();
                        for (GraphElements.MyEdge e : visGraph.getGraph().getEdges()) {
                            if (e.getFlagPaint() == 1) {
                                e.setFlagPaint(0);
                            }
                        }
                        visGraph.setChEdgeList(null);
                        visGraph.setPaintedEdgeslist(null);
                        visGraph.repainFrame();

                        matrix = new GenerationMatrix(visGraph.getGraph(), s.getText(), t.getText());
                        performGeneticAlgorithm(gaDialog);
                        visGraph.addParalEdges();
                        visGraph.repainFrame();
                        try (FileWriter writer = new FileWriter("result.txt", false)) {
                            String text = population.printFile(matrix);
                            writer.write(text);
                            writer.append('\n');
                            writer.flush();
                            writer.close();
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                });
                gaDialog.getDialog().showAndWait();
            }
        });

    }

    //Основной алгоритм
    public void performGeneticAlgorithm(GeneticAlgorithmEditDialog gaDialog) {
        long old = System.currentTimeMillis(); // time
        matrix.printMatrix();
        System.out.println("s " + matrix.getS());
        System.out.println("t " + matrix.getT());
        Individual chromosome;
        population = new Population();
        visGraph.setPopulation(population);
        int noRes = 0;
        int generationN = 0;//Количество поколений
        RouteComparator myRouteComparator = new RouteComparator();
        for (int i = 0; i < Integer.parseInt(gaDialog.getNumberPopulation().getText()); i++) {
            chromosome = new Individual(visGraph.getGraph().getVertexCount(), matrix.getS(), matrix.getT(), matrix, Integer.parseInt(b.getText()));
            population.addChomosome(chromosome);

        }
        System.out.println("1. Вывод первого поколения:");
        population.printPopulation(matrix);//первое поколение
        GeneticAlgorithm gAlg = new GeneticAlgorithm(matrix, population, gaDialog.getComboBoxParents().getValue(), gaDialog.getComboBoxCrossingTypes().getValue(), gaDialog.getComboBoxSelectionTypes().getValue(), Integer.parseInt(b.getText()), Integer.parseInt(gaDialog.getNumberPopulation().getText()));
        for (int i = 0; i < population.size(); i++)//Поместили в резерв хромосомы из начальной популяции
        {
            if (population.getAtIndex(i).getFitnessF() && !gAlg.existInReserve(population.getAtIndex(i))) {
                gAlg.addReserveChromosome(population.getAtIndex(i));
            }
        }
        if (gAlg.getReserveChromosomes().size() >= Integer.parseInt(k.getText())) {//нашлось решение на первом шаге
            gAlg.printReserveList();
        } else {
            System.out.println("COUNT GOOD CHROMOSOME=" + population.countGoodChromosome(Integer.parseInt(b.getText())));
            System.out.println("Reserve начальной популяции:" + gAlg.getReserveChromosomes().size());
            gAlg.printReserveList();

            while (gAlg.getReserveChromosomes().size() < Integer.parseInt(k.getText())) {
                generationN++;
                if (generationN > 1000) {
                    System.out.println("На 1000 шаге нашёл " + gAlg.getReserveChromosomes().size() + " различных маршрутов.");
                    noRes = 1;
                    break;
                }
                System.out.println("ПОКОЛЕНИЕ НОМЕР:" + generationN);
                gAlg.choiceParents();
                gAlg.crossing();
                System.out.println("2. После кроссовера, родители и потомки" + generationN);
                population.printPopulation(matrix);//первое поколение
                System.out.println("Reserve после скрещивания:" + gAlg.getReserveChromosomes().size());
                gAlg.printReserveList();
                gAlg.mutation();
                System.out.println("3. После мутации. Номер поколения:" + generationN);
                population.printPopulation(matrix);
                System.out.println("Reserve после мутации:" + gAlg.getReserveChromosomes().size());
                gAlg.printReserveList();
                gAlg.selection();
                System.out.println("4. После отбора. Номер поколения:" + generationN);
                population.printPopulation(matrix);
                System.out.println("Reserve после отбора:" + gAlg.getReserveChromosomes().size());
                gAlg.printReserveList();
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
                                    System.out.println("Заменили особь j=:" + j + " " + population.getAtIndex(i).printChromosome(matrix));
                                    count_replace++;
                                    System.out.println("число замен" + count_replace);
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
                System.out.println("5. После замены одинаковых на запасные");
                population.printPopulation(matrix);

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
                System.out.println("Reserve после добавления новых хороших хромосом: " + gAlg.getReserveChromosomes().size());
                gAlg.printReserveList();
                System.out.println("COUNT GOOD CHROMOSOME =" + population.countGoodChromosome(Integer.parseInt(b.getText())));

            }
        }
        population.setListPopulation(gAlg.getReserveChromosomes());
        int n;
        if (noRes == 1) {
            n = -1;

        } else {
            n = population.size();
        }

        n = population.size();
        population.getPopulation().sort(myRouteComparator);
        population.printPopulation(matrix);
        for (int i = 0; i < (n - Integer.parseInt(k.getText())); i++) {
            population.getPopulation().removeLast();
        }

        System.out.println("GA time work: " + (System.currentTimeMillis() - old) / 1000F + " sec"); // веремя работы генетического алгоритма
        gaDialog.setResult(population.size());
        gaDialog.setNumGenerations(generationN);
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
        b.setText("7");
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
        root.add(btnGeneticAlgorithm, 0, 10);
        Scene scene = new Scene(root, 700, 450);
        return scene;
    }

}
