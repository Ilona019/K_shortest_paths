package main;

import grapheditor.GraphElements;
import grapheditor.VisualizationViewerGraph;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
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

    public boolean checkDialogGeneticAlgorithm(TextField k, TextField numberPopulation, TextField probability) {
        checkParametersGeneticAlgorithm(k, numberPopulation, probability);
        return isInputErrors();
    }

    public boolean checkDialogAntColonyOptimization(TextField k, TextField colonySize, TextField alpha, TextField betta, TextField evaporation, TextField maxIterations, TextField Q) {
        checkDialogACO(k, colonySize, alpha, betta, evaporation, maxIterations, Q);
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
            } else if (Integer.parseInt(s.getText()) == Integer.parseInt(t.getText()))
                errors += "* Vertces s != t.\n";
        }
        if (visGraph.getGraph().getVertexCount() == 0) {
            errors += "* Please draw a graph!\n";
        }
        if (visGraph.getGraph().getEdgeCount() == 0) {
            errors += "* Please create adges!";
        }
        this.message = errors;
    }

    public void checkParametersGeneticAlgorithm(TextField k, TextField numberPopulation, TextField probability) {
        String errors = "";
        if (!isPositiveNumber(numberPopulation.getText()) || Integer.parseInt(k.getText()) > Integer.parseInt(numberPopulation.getText())) {
            errors += " You incorrectly input the N! It is  positive, integer number!\n N > 0 AND N >= " + Integer.parseInt(k.getText()) + " (K).\n";
        }
        if (!isProbability(probability.getText())) {
            errors += "You incorrectly input the Probability! 0 <=p <= 1";
        }
        this.message = errors;
    }

    private void checkDialogACO(TextField k, TextField colonySize, TextField alpha, TextField betta, TextField evaporation, TextField maxIterations, TextField Q) {
        String errors = "";
        if (!isPositiveNumber(colonySize.getText())) {
            errors += " You incorrectly input the colony size! It is  positive, integer number!\n Colony size > 0.\n";
        }
        if (!isNonNegativeNumber(alpha.getText())) {
            errors += "* You incorrectly input the alpha. It is  non negative, integer number!\n";
        }
        if (!isNonNegativeNumber(betta.getText())) {
            errors += "* You incorrectly input the betta. It is  non negative, integer number!\n";
        }
        if ((!isDouble(evaporation.getText()) && !isZepoOrOne(evaporation.getText())) || !isIncludedInterval(evaporation.getText(), 0, 1)) {
            errors += "*0 <= Evaporation < =1.\n";
        }
        if (!isPositiveNumber(maxIterations.getText())) {
            errors += "*You incorrectly input the max iteration. It is positive, integer number!\n max iterations > 0 \n";
        }
        if (!isPositiveNumber(Q.getText()) || !isIncludedInterval(Q.getText(), 10, 10000)) {
            errors += "*You incorrectly input the Q. t is positive, integer number 10 <= Q <= 10000!";
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

    public boolean isPositiveNumber(String text) {
        return text.matches("[\\+]?[1-9][0-9]*");
    }

    public boolean isDouble(String text) {
        return text.matches("[\\+]?[0-9]+\\.[0-9]*");
    }

    public boolean isZepoOrOne(String text) {
        return text.matches("[0-1]");
    }

    public boolean isNonNegativeNumber(String text) {
        return text.matches("[\\+]?[0-9]+");
    }

    public boolean isVertex(String text) {
        return text.matches("0") || text.matches("[1-9][0-9]*");
    }

    public boolean isIncludedInterval(String text, double leftBorder, double rightBorder) {
        return Double.parseDouble(text) >= leftBorder && Double.parseDouble(text) <= rightBorder;
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

    public boolean isProbability(String text) {
        return (isDouble(text) || isZepoOrOne(text)) && isIncludedInterval(text, 0, 1);
    }
}
