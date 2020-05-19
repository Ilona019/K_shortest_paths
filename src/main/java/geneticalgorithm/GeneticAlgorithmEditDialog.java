package geneticalgorithm;

import grapheditor.VisualizationViewerGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import jeneticsga.JeneticsGA;
import main.ValidateInput;

/**
 *
 * @author Илона
 */
public class GeneticAlgorithmEditDialog {
    private Font font;
    private Label lparents;
    private Label lcrossing;
    private Label lselection;
    private Label lmutation;
    private ComboBox<String> comboBoxParents;
    private ComboBox<String> comboBoxCrossingTypes;
    private ComboBox<String> comboBoxSelectionTypes;
    private ComboBox<String> comboBoxCrossingTypesJenetics;
    private ComboBox<String> comboBoxMutationTypesJenetics;
    private ComboBox<String> comboBoxSelectionTypesJenetics;
    private Label lp;
    private TextField numberPopulation;
    private Button btnRun;
    private Button btnJenetics;
    private Label result;
    private Label numGenerations;
    private Label durationAlg;
    private VisualizationViewerGraph visGraph;
    private GridPane root;
    private Dialog<Object> dialog;
    private ValidateInput validationInput;
    private GeneticAlgorithm gAlg;
    private JeneticsGA jeneticsGA;
    

    public GeneticAlgorithmEditDialog(VisualizationViewerGraph visGraph, GeneticAlgorithm gAlg, JeneticsGA jeneticsGA) {
        this.visGraph = visGraph;
        this.gAlg = gAlg;
        this.jeneticsGA = jeneticsGA;
        dialog = new Dialog<>();
        dialog.getDialogPane().setPrefSize(700, 300);
        dialog.setTitle("Genetic Algorithm");
        dialog.setHeaderText("Enter parameters genetic algorithm.");
        createDialog();
        ButtonType buttonTypeOk = new ButtonType("Save parameters", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.getDialogPane().setContent(root);
        validationInput = new ValidateInput(visGraph);
        
    dialog.setResultConverter((ButtonType b) -> {
            if (b == buttonTypeOk) {
                if (validationInput.isPositiveNumber(numberPopulation.getText())) {
                    handleOk();
                    return gAlg;
                } else {
                    validationInput.showMessage("You incorrectly input the N! It is positive, integer number!");
                }
            }
            return null;
        });
    }

    private void createDialog() {
        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(20);
        root.setVgap(20);
        font = Font.font("TimesRoman",FontWeight.BOLD, 15);

        createLabelsOperators();
        createMyComboBoxOperators();
        createComboBoxForJeneticsOperators();

        DropShadow shadow = new DropShadow();
        btnRun = new Button();
        btnRun.setText("Run algorithm");
        btnRun.setPadding(new Insets(10, 20, 10, 20));
        btnRun.setFont(font);
        btnRun.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        btnRun.setEffect(shadow);

        btnJenetics = new Button("Run jenetics algorithm");
        btnJenetics.setPadding(new Insets(10, 20, 10, 20));
        btnJenetics.setFont(font);
        btnJenetics.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        btnJenetics.setEffect(shadow);
        root.add(btnRun, 1, 6);
        root.add(btnJenetics, 2, 6);

        result = new Label();
        result.setFont(font);
        numGenerations = new Label();
        numGenerations.setFont(font);
        root.add(result, 0, 7);
        root.add(numGenerations, 1, 7);
        durationAlg = new Label("");
        durationAlg.setFont(font);
        root.add(durationAlg, 2, 7);
    }

    private void createLabelsOperators() {
        lp = new Label("Number of chromosomes:");
        lp.setFont(font);
        root.add(lp, 0, 0);
        numberPopulation = new TextField(Integer.toString(gAlg.getN()));
        numberPopulation.setMaxWidth(80);
        root.add(numberPopulation, 1, 0);
        lparents = new Label("Choice of the parents: ");
        lparents.setFont(font);
        lcrossing = new Label("Crossing type: ");
        lcrossing.setFont(font);
        lmutation = new Label("Mutation type: ");
        lmutation.setFont(font);
        lselection = new Label("Selection type: ");
        lselection.setFont(font);
        root.add(lparents, 0, 1);
        root.add(lcrossing, 0, 2);
        root.add(lmutation, 0, 3);
        root.add(lselection, 0, 4);
    }

    private void createMyComboBoxOperators(){
        ObservableList<String> parentsList = FXCollections.observableArrayList("PANMIXIA", "INBREEDING", "OUTBREEDING");
        ObservableList<String> crossingTypesList = FXCollections.observableArrayList("SINGLE POINT CROSSOVER", "TWO POINT CROSSOVER");
        ObservableList<String> selectionTypesList = FXCollections.observableArrayList("ELITE");

        comboBoxParents = new ComboBox<>(parentsList);
        comboBoxCrossingTypes = new ComboBox<>(crossingTypesList);
        comboBoxSelectionTypes = new ComboBox<>(selectionTypesList);
        comboBoxParents.getSelectionModel().select(gAlg.getChoiceParents());
        comboBoxCrossingTypes.getSelectionModel().select(gAlg.getСrossingType());
        comboBoxSelectionTypes.getSelectionModel().select(gAlg.getSelectionType());
        root.add(comboBoxParents, 1, 1);
        root.add(comboBoxCrossingTypes, 1, 2);
        root.add(comboBoxSelectionTypes, 1, 4);
    }

    private void createComboBoxForJeneticsOperators(){
        ObservableList<String> crossingTypesList = FXCollections.observableArrayList("UNIFORM", "LINE");
        ObservableList<String> mutationTypesList = FXCollections.observableArrayList( "SWAP", "GAUSSIAN");
        ObservableList<String> selectionTypesList = FXCollections.observableArrayList("TOURNAMENT", "ROULETTE WHEEL");

        comboBoxCrossingTypesJenetics = new ComboBox<>(crossingTypesList);
        comboBoxMutationTypesJenetics = new ComboBox<>(mutationTypesList);
        comboBoxSelectionTypesJenetics = new ComboBox<>(selectionTypesList);
        comboBoxCrossingTypesJenetics.getSelectionModel().select(jeneticsGA.getСrossingType());
        comboBoxMutationTypesJenetics.getSelectionModel().select(jeneticsGA.getMutationType());
        comboBoxSelectionTypesJenetics.getSelectionModel().select(jeneticsGA.getSelectionType());
        root.add(comboBoxCrossingTypesJenetics, 2, 2);
        root.add(comboBoxMutationTypesJenetics, 2,3);
        root.add(comboBoxSelectionTypesJenetics, 2, 4);
    }
    
    private void handleOk() {
        gAlg.setN(Integer.parseInt(numberPopulation.getText()));
        gAlg.setChoiceParents(comboBoxParents.getValue());
        gAlg.setСrossingType(comboBoxCrossingTypes.getValue());
        gAlg.setSelectionType(comboBoxSelectionTypes.getValue());

        jeneticsGA.setN(Integer.parseInt(numberPopulation.getText()));
        jeneticsGA.setСrossingType(comboBoxCrossingTypesJenetics.getValue());
        jeneticsGA.setMutationType(comboBoxMutationTypesJenetics.getValue());
        jeneticsGA.setSelectionType(comboBoxSelectionTypesJenetics.getValue());
    }
    
    public void setResult(int countFoundPaths){
        result.setText("Found "+countFoundPaths+" routes");
    }
    
    public void setNumGenerations(int numGenerations){
        this.numGenerations.setText("Number of generations: "+numGenerations);
        this.numGenerations.setMinWidth(200);
    }
    
    public void setDurationAlgorithm(float time){
        durationAlg.setText("Time:\t"+time+"  sec");
    }
    
    public Dialog getDialog(){
        return dialog;
    }
    
    public Button getBtnRun(){
        return btnRun;
    }

    public Button getJeneticBtn(){
        return btnJenetics;
    }
    
    public ComboBox<String> getComboBoxParents(){
        return comboBoxParents;
    }
    
    public ComboBox<String> getComboBoxCrossingTypes(){
        return comboBoxCrossingTypes;
    }   
    
    public ComboBox<String> getComboBoxSelectionTypes(){
        return comboBoxSelectionTypes;
    }

    public ComboBox<String> getComboBoxCrossingJenetics(){
        return comboBoxCrossingTypesJenetics;
    }

    public ComboBox<String> getComboBoxMutationTypeJenetics(){
        return comboBoxMutationTypesJenetics;
    }

    public ComboBox<String> getComboBoxSelectionJenetics(){
        return comboBoxSelectionTypesJenetics;
    }
    
    public TextField getNumberPopulation(){
        return numberPopulation;
    }

}
