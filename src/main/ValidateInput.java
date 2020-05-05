package main;

import grapheditor.GraphElements;
import grapheditor.VisualizationViewerGraph;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 *
 * @author Ilona
 */
public class ValidateInput {

    private VisualizationViewerGraph visGraph;
    private String message;

    public ValidateInput() {

    }

    public ValidateInput(VisualizationViewerGraph visGraph) {
        this.message = "";
        this.visGraph = visGraph;
    }

    private boolean isInputErrors() {
        if (!"".equals(message)) {
            showMessage(message);
            return true;
        }
        return false;
    }

    public boolean checkWindowСonditionTask(TextField s, TextField t, TextField k, TextField b) {
        checkGeneraParametersTask(s, t, k, b);
        return isInputErrors();
    }

    public boolean checkDialogGeneticAlgorithm(TextField k, ComboBox<String> comboBoxParents, ComboBox<String> comboBoxCrossingTypes, ComboBox<String> comboBoxSelectionTypes, TextField numberPopulation) {
        checkParametersGeneticAlgorithm(k, comboBoxParents, comboBoxCrossingTypes, comboBoxSelectionTypes, numberPopulation);
        return isInputErrors();
    }

    public boolean checkDialogAntColonyOptimization(TextField k, TextField colonySize, TextField alpha, TextField betta, TextField evaporation, TextField maxIterations) {
        checkDialogACO(k, colonySize, alpha, betta, evaporation, maxIterations);
        return isInputErrors();
    }

    private void checkGeneraParametersTask(TextField s, TextField t, TextField k, TextField b) {
        String errors = "";
        if (!isPositiveNumber(k.getText())) {
            errors += "* You incorrectly input the K! It is positive, integer number. K > 1.\n";
        }
        if (!isPositiveNumber(b.getText())) {
            errors += "* You incorrectly input the B! It is  positive, integer number. B > 1.\n";
        }
        if (!isVertex(s.getText())) {
            errors += "* You incorrectly input the s! It is number of vertex.\n\tNumber of vertices = " + visGraph.getGraph().getVertexCount() + ".\n";
        } else {
            if (!visGraph.getGraph().containsVertex(getVertex(Integer.parseInt(s.getText())))) {
                errors += "* Please create vertece the beginning of the path in the graph!.\n";
            }
        }
        if (!isVertex(t.getText())) {
            errors += "* You incorrectly input the t! It is number of vertex.\n\tNumber of vertices = " + visGraph.getGraph().getVertexCount() + ".\n";
        } else {
            if (!visGraph.getGraph().containsVertex(getVertex(Integer.parseInt(t.getText())))) {
                errors += "* Please create vertece the end of the path in the graph! There is no such vertex " + Integer.parseInt(t.getText()) + ".\n";
            }
        }
        if (visGraph.getGraph().getVertexCount() == 0) {
            errors += "* Please draw a graph!\n";
        }
        if (visGraph.getGraph().getEdgeCount() == 0) {
            errors += "* Please create adges!";
        }
        this.message = errors;
    }

    public void checkParametersGeneticAlgorithm(TextField k, ComboBox<String> comboBoxParents, ComboBox<String> comboBoxCrossingTypes, ComboBox<String> comboBoxSelectionTypes, TextField numberPopulation) {
        String errors = "";
        if (!isPositiveNumber(numberPopulation.getText()) || 2 * Integer.parseInt(k.getText()) > Integer.parseInt(numberPopulation.getText())) {
            errors += " You incorrectly input the N! It is  positive, integer number!\n N > 0 AND N >= " + 2 * Integer.parseInt(k.getText()) + " (2*K).\n";
        }
        if (!isComboBox(comboBoxParents)) {
            errors += "* You didn't choose the operator of choice of the parents.\n";
        }
        if (!isComboBox(comboBoxCrossingTypes)) {
            errors += "* You didn't choose the crossing type.\n";
        }
        if (!isComboBox(comboBoxSelectionTypes)) {
            errors += "* You didn't choose the selection type.\n";
        }
        this.message = errors;
    }

    private void checkDialogACO(TextField k, TextField colonySize, TextField alpha, TextField betta, TextField evaporation, TextField maxIterations) {
        String errors = "";
        if (!isPositiveNumber(colonySize.getText()) || 2 * Integer.parseInt(k.getText()) > Integer.parseInt(colonySize.getText())) {
            errors += " You incorrectly input the colony size! It is  positive, integer number!\n Colony size > 0 AND Colony size >= " + 2 * Integer.parseInt(k.getText()) + " (2*K).\n";
        }
        if (!isNonNegativeNumber(alpha.getText())) {
            errors += "* You incorrectly input the alpha. It is  non negative, integer number!\n";
        }
        if (!isNonNegativeNumber(betta.getText())) {
            errors += "* You incorrectly input the betta. It is  non negative, integer number!\n";
        }
        if ((!isDouble(evaporation.getText()) && !isZepoOrOne(evaporation.getText())) || !isEvaporation(evaporation.getText())) {
            errors += "*0 <= Evaporation < =1.\n";
        }
        if (!isPositiveNumber(maxIterations.getText())) {
            errors += "*You incorrectly input the max iteration. It is positive, integer number!\n max iterations > 0 \n";
        }
        this.message = errors;
    }

    //Получить вершину по ёё индексу
    public GraphElements.MyVertex getVertex(int ind) {
        GraphElements.MyVertex v = new GraphElements.MyVertex("-1");
        for (GraphElements.MyVertex ver : visGraph.getGraph().getVertices()) {
            if (Integer.parseInt(ver.getName()) == ind) {
                return ver;
            }
        }
        return v;
    }

    private boolean isComboBox(ComboBox cb) {
        if (cb.getValue() == null) {
            return false;
        }
        return true;
    }

    public boolean isPositiveNumber(String text) {
        if (!text.matches("[\\+]?[1-9][0-9]*")) {
            return false;
        }
        return true;
    }

    public boolean isDouble(String text) {
        if (!text.matches("[\\+]?[0-9]\\.[0-9]*")) {
            return false;
        }
        return true;
    }

    public boolean isZepoOrOne(String text) {
        if (!text.matches("[0-1]")) {
            return false;
        }
        return true;
    }

    public boolean isNonNegativeNumber(String text) {
        if (!text.matches("[\\+]?[0-9]+")) {
            return false;
        }
        return true;
    }

    public boolean isVertex(String text) {
        if (!text.matches("0") && !text.matches("[1-9][0-9]*")) {
            return false;
        }
        return true;
    }

    public boolean isEvaporation(String text) {
        if (Double.parseDouble(text) >= 0 && Double.parseDouble(text) <= 1) {
            return true;
        }
        return false;
    }

    public void showMessage(String s) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Data file error");
        alert.setHeaderText("Data entry error");
        alert.setContentText(s);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(380, 300);
        alert.showAndWait();
    }
}
