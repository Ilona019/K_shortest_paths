package antcolonyalgorithm;

import grapheditor.VisualizationViewerGraph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.ValidateInput;

/**
 * @author Ilona
 */
public class AntColonyAlgorithmEditDialog {

    private VisualizationViewerGraph visGraph;
    private GridPane root;
    private Dialog<Object> dialog;
    private ValidateInput validationInput;

    private Label lColonySize;
    private Label lAlfa;
    private Label lBetta;
    private Label lEvaporation;
    private Label lMaxIteration;
    private Label lQ;
    private Label countIterationsOnBestColony;
    private Label countIterations;
    private Label result;
    private Label durationAlg;
    private TextField colonySize;
    private TextField alpha;
    private TextField betta;
    private TextField evaporation;//коэффициент испарения феромона.
    private TextField maxIterations;
    private TextField textQ;
    private Font font;
    private Button runAntColonyAlg;
    AntColonyOptimisation aco;

    public AntColonyAlgorithmEditDialog(VisualizationViewerGraph visGraph, AntColonyOptimisation aco, TextField k) {
        this.visGraph = visGraph;
        this.aco = aco;
        dialog = new Dialog<>();
        dialog.getDialogPane().setPrefSize(700, 300);
        dialog.setTitle("Ant Colony Algorithm");
        dialog.setHeaderText("Enter parameters ant colony algorithm.");
        createDialog(aco);
        ButtonType buttonTypeOk = new ButtonType("Save parameters", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk,
                ButtonType.CLOSE
        );
        dialog.getDialogPane().setContent(root);

        validationInput = new ValidateInput(visGraph);

        dialog.setResultConverter((ButtonType b) -> {
            if (b == buttonTypeOk) {
                if (!validationInput.checkDialogAntColonyOptimization(k, colonySize, alpha, betta, evaporation, maxIterations, textQ)) {
                    handleOk();
                    return aco;
                }
            }
            return null;
        });
    }

    private void createDialog(AntColonyOptimisation aco) {
        root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        font = Font.font("TimesRoman", FontWeight.BOLD, 15);
        lColonySize = new Label("Colony size:");
        lColonySize.setFont(font);
        root.add(lColonySize, 0, 0);
        lAlfa = new Label("α:");
        lAlfa.setFont(font);
        root.add(lAlfa, 0, 1);

        lBetta = new Label("β:");
        lBetta.setFont(font);
        root.add(lBetta, 0, 2);

        lEvaporation = new Label("Evaporation pheromone rate:");
        lEvaporation.setFont(font);
        root.add(lEvaporation, 0, 3);

        lMaxIteration = new Label("Max iterations:");
        lMaxIteration.setFont(font);
        root.add(lMaxIteration, 0, 4);

        lQ = new Label("Q:");
        lQ.setFont(font);
        root.add(lQ, 0, 5);

        colonySize = new TextField(String.valueOf(aco.getColonySize()));
        root.add(colonySize, 1, 0);

        alpha = new TextField(String.valueOf(aco.getAlpha()));
        root.add(alpha, 1, 1);

        betta = new TextField(String.valueOf(aco.getBetta()));
        root.add(betta, 1, 2);

        evaporation = new TextField(String.valueOf(aco.getEvaporation()));
        root.add(evaporation, 1, 3);

        maxIterations = new TextField(String.valueOf(aco.getMaxIterations()));
        root.add(maxIterations, 1, 4);

        textQ = new TextField(String.valueOf(aco.getQ()));
        root.add(textQ, 1, 5);

        countIterations = new Label("");
        countIterations.setFont(font);
        root.add(countIterations, 1, 8);

        result = new Label("");
        result.setFont(font);
        root.add(result, 0, 7);

        countIterationsOnBestColony = new Label("");
        countIterationsOnBestColony.setFont(font);
        root.add(countIterationsOnBestColony, 0, 8);

        durationAlg = new Label();
        durationAlg.setFont(font);
        root.add(durationAlg, 1, 7);

        DropShadow shadow = new DropShadow();
        runAntColonyAlg = new Button("Run algorithm");
        runAntColonyAlg.setFont(font);
        runAntColonyAlg.setStyle("-fx-text-fill: navy; -fx-border-color: navy; -fx-border-width: 3px; -fx-underline: true; ");
        runAntColonyAlg.setPadding(new Insets(10, 20, 10, 20));
        runAntColonyAlg.setEffect(shadow);
        root.add(runAntColonyAlg, 0, 6);
    }

    private void handleOk() {
        aco.setColonySize(Integer.parseInt(colonySize.getText()));
        aco.setAlpha(Integer.parseInt(alpha.getText()));
        aco.setBetta(Integer.parseInt(betta.getText()));
        aco.setEvaporation(Double.parseDouble(evaporation.getText()));
        aco.setMaxIterations(Integer.parseInt(maxIterations.getText()));
        aco.setQ(Integer.parseInt(textQ.getText()));
    }

    public void setResult(int countFoundPaths) {
        result.setText("Found\t" + countFoundPaths + " routes");
    }

    public void setCountIterations(int count) {
        countIterations.setText("Count iterations:\t" + count);
    }

    public void setDurationAlgorithm(float time) {
        durationAlg.setText("Time:\t" + time + "  sec");
    }

    public Dialog<Object> getDialog() {
        return dialog;
    }

    public Button getBtnRun() {
        return runAntColonyAlg;
    }

    public TextField getAlpha() {
        return alpha;
    }

    public TextField getBetta() {
        return betta;
    }

    public TextField getEvaporation() {
        return evaporation;
    }

    public TextField getColonySize() {
        return colonySize;
    }

    public TextField getMaxIterations() {
        return maxIterations;
    }

    public TextField getQ() {
        return textQ;
    }
}
