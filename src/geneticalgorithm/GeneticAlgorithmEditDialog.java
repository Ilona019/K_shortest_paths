package geneticalgorithm;

import grapheditor.VisualizationViewerGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
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
    private ComboBox<String> comboBoxParents;
    private ComboBox<String> comboBoxCrossingTypes;
    private ComboBox<String> comboBoxSelectionTypes;
    private Label lp;
    private TextField numberPopulation;
    private Button btnRun;
    private Label result;
    private Label numGenerations;
    private Label durationAlg;
    private VisualizationViewerGraph visGraph;
    private GridPane root;
    private Dialog<Object> dialog;
    private ValidateInput validationInput;
    private GeneticAlgorithm gAlg;
    

    public GeneticAlgorithmEditDialog(VisualizationViewerGraph visGraph, GeneticAlgorithm gAlg) {
        this.visGraph = visGraph;
        this.gAlg = gAlg;
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
        root.setHgap(10);
        root.setVgap(10);
        font = Font.font("TimesRoman",FontWeight.BOLD, 15);
        lp = new Label("Enter the number of chromosomes in population:\t\t N =");
        lp.setFont(font);
        root.add(lp, 0, 5);
        numberPopulation = new TextField(Integer.toString(gAlg.getN()));
        root.add(numberPopulation, 1, 5);
        ObservableList<String> parentsList = FXCollections.observableArrayList("PANMIXIA", "INBREEDING", "OUTBREEDING");
        ObservableList<String> crossingTypesList = FXCollections.observableArrayList("SINGLE POINT CROSSOVER", "TWO POINT CROSSOVER");
        ObservableList<String> selectionTypesList = FXCollections.observableArrayList("ELITE");
        lparents = new Label("Select the operator of choice of the parents: ");
        lparents.setFont(font);
        lcrossing = new Label("Select the operator of crossing type: ");
        lcrossing.setFont(font);
        lselection = new Label("Select the operatot of selection type: ");
        lselection.setFont(font);
        root.add(lparents, 0, 6);
        root.add(lcrossing, 0, 7);
        root.add(lselection, 0, 8);
        comboBoxParents = new ComboBox<>(parentsList);
        comboBoxCrossingTypes = new ComboBox<>(crossingTypesList);
        comboBoxSelectionTypes = new ComboBox<>(selectionTypesList);
        comboBoxParents.getSelectionModel().select(gAlg.getChoiceParents());
        comboBoxCrossingTypes.getSelectionModel().select(gAlg.getСrossingType());
        comboBoxSelectionTypes.getSelectionModel().select(gAlg.getSelectionType());
        GridPane.setHalignment(comboBoxParents, HPos.CENTER);
        root.add(comboBoxParents, 1, 6);
        root.add(comboBoxCrossingTypes, 1, 7);
        root.add(comboBoxSelectionTypes, 1, 8);
        DropShadow shadow = new DropShadow();
        btnRun = new Button();
        btnRun.setText("Run algorithm");
        btnRun.setPadding(new Insets(10, 20, 20, 10));
        btnRun.setFont(font);
        btnRun.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        btnRun.setEffect(shadow);
        root.add(btnRun, 0, 10);
        result = new Label();
        result.setFont(font);
        numGenerations = new Label();
        numGenerations.setFont(font);
        root.add(result, 0, 11);
        root.add(numGenerations, 0, 12);
        durationAlg = new Label("");
        durationAlg.setFont(font);
        root.add(durationAlg, 1, 11);
    }
    
    private void handleOk() {
        gAlg.setN(Integer.parseInt(numberPopulation.getText()));
        gAlg.setChoiceParents(comboBoxParents.getValue());
        gAlg.setСrossingType(comboBoxCrossingTypes.getValue());
        gAlg.setSelectionType(comboBoxSelectionTypes.getValue());
    }
    
    public void setResult(int countFoundPaths){
        result.setText("Found "+countFoundPaths+" routes");
    }
    
    public void setNumGenerations(int numGenerations){
        this.numGenerations.setText("Number of generations = "+numGenerations);
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
    
    public ComboBox<String> getComboBoxParents(){
        return comboBoxParents;
    }
    
    public ComboBox<String> getComboBoxCrossingTypes(){
        return comboBoxCrossingTypes;
    }   
    
    public ComboBox<String> getComboBoxSelectionTypes(){
        return comboBoxSelectionTypes;
    }
    
    public TextField getNumberPopulation(){
        return numberPopulation;
    }

}
