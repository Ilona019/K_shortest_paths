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

    public ValidateInput(VisualizationViewerGraph visGraph) {
        this.message = "";
        this.visGraph = visGraph;
    }

    public boolean isInputErrors() {
        if (!"".equals(message)) {
            showMessage(message);
            return true;
        }
        return false;
    }

    public void checkGeneraParametersTask(TextField s, TextField t, TextField k, TextField b) {
        String errors = "";
        if (!isPositiveNumber(k.getText())) {
            errors += "* You incorrectly input the K! It is positive, integer number. K > 1.\n";
        }
        if (!isPositiveNumber(b.getText())) {
            errors += "* You incorrectly input the B! It is  positive, integer number. B > 1.\n";
        }
        if (!isVertex(s)) {
            errors += "* You incorrectly input the s! It is number of vertex. 0 <= s <= 100.\n";
        }
        if (!isVertex(t)) {
            errors += "* You incorrectly input the t! It is number of vertex. 0 <= t <= 100.\n";
        }
        if (visGraph.getGraph().getVertexCount() == 0) {
            errors += "* Please draw a graph!\n";
        }
        if (!visGraph.getGraph().containsVertex(getVertex(Integer.parseInt(s.getText()))) || !visGraph.getGraph().containsVertex(getVertex(Integer.parseInt(t.getText())))) {
            errors += "* Please create verteces the beginning of the path and the end of the path.\n";
        }
        if (visGraph.getGraph().getEdgeCount() == 0) {
            errors += "* Please create adges!";
        }
        this.message = errors;
    }

    public void checkParametersGeneticAlgorithm(TextField k, TextField s, TextField t, ComboBox<String> comboBoxParents, ComboBox<String> comboBoxCrossingTypes, ComboBox<String> comboBoxSelectionTypes, TextField numberPopulation) {
        String errors = "";
        if (2 * Integer.parseInt(k.getText()) > Integer.parseInt(numberPopulation.getText())) {
            errors += " You incorrectly input the N! It is  positive, integer number!\n N > 0 AND N >= 2*K.\n";
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

    public boolean isVertex(TextField text) {
        if (!text.getText().matches("[+]?[0-9]+")) {
            return false;
        }
        return true;
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
