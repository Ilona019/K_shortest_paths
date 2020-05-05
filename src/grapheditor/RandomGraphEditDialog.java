
package grapheditor;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import main.ValidateInput;

/**
 *
 * @author Ilona
 */
public class RandomGraphEditDialog  implements ChangeListener {
    private int percentOfEdges;
    private ValidateInput validationInput;
    JTextField textCountVertex;
    JTextField textFrom;
    JTextField textBefore;
    private int countVertex;
    private int from;
    private int before;
    private RandomGraphEditDialog randomGraphEditDialog;
    
    RandomGraphEditDialog(){
        this.countVertex = 10;
        this.percentOfEdges  = 60;
        this.from = 1;
        this.before = 100;
    }
    
     RandomGraphEditDialog(VisualizationViewerGraph visThis, RandomGraphEditDialog randomGraphEditDialog) {
            this.randomGraphEditDialog = randomGraphEditDialog;
            validationInput= new ValidateInput();
            JFrame frameCountVertex = new JFrame("Random ");
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            frameCountVertex.setResizable(false);
            gbc.insets = new Insets (10,0,0,0);

            JLabel label = new JLabel("Enter count vertex: ");
            textCountVertex = new JTextField(String.valueOf(randomGraphEditDialog.countVertex),5);
            textCountVertex. setHorizontalAlignment(JTextField.CENTER);
            
            JLabel percentOfEdgesLabel = new JLabel("Percent edge:");
            percentOfEdges = randomGraphEditDialog.percentOfEdges;//по умолчанию
            JSlider percentOfEdgesSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, percentOfEdges);
            JLabel weightRange = new JLabel("Weight range:");
            JLabel from = new JLabel("From:");
            textFrom = new JTextField(String.valueOf(randomGraphEditDialog.from), 5);
            textFrom. setHorizontalAlignment(JTextField.CENTER);
            JLabel before = new JLabel("Before:");
            textBefore= new JTextField(String.valueOf(randomGraphEditDialog.before),5);
            textBefore.setHorizontalAlignment(JTextField.CENTER);
            JButton btnParametersRandomGraph = new JButton("Ok");
            
            // слайдер - процент ребер
            percentOfEdgesSlider.setBounds(310, 125, 425, 40);
            percentOfEdgesSlider.setMajorTickSpacing(10);
            percentOfEdgesSlider.setMinorTickSpacing(5);
            percentOfEdgesSlider.setPaintTicks(true);
            percentOfEdgesSlider.setPaintLabels(true);
            percentOfEdgesSlider.setFont(new Font("Arial", Font.PLAIN, 10));
            percentOfEdgesSlider.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
            percentOfEdgesSlider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            percentOfEdgesSlider.setName("PercentOfEdgesSlider");
            percentOfEdgesSlider.addChangeListener(this);
        
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(label, gbc);
            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(textCountVertex, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(percentOfEdgesLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 1;
            panel.add(percentOfEdgesSlider, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(weightRange, gbc);
            
            JPanel panelFloat = new JPanel(new FlowLayout( FlowLayout.CENTER, 20, 15));
           
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.weightx = 0.0;
            gbc.gridwidth = 2;
            panel.add(panelFloat, gbc);

            panelFloat.add(from);
            panelFloat.add(textFrom);
            panelFloat.add(before);
            panelFloat.add(textBefore);
            
            gbc.ipadx = 20;
            gbc.gridx = 1;
            gbc.gridy = 5;         
            panel.add(btnParametersRandomGraph, gbc);

            
            frameCountVertex.getContentPane().add(panel);
            frameCountVertex.setPreferredSize(new Dimension(350, 300));
            frameCountVertex.setLocation(80, 100);
            frameCountVertex.pack();
            frameCountVertex.setVisible(true);

            //Создание случайного графа по параметрам.
            btnParametersRandomGraph.addActionListener((ActionEvent e1) -> {
               visThis.clearGraph();
               String errors="";
                if (!validationInput.isPositiveNumber(textCountVertex.getText())) {
                   errors += "* You incorrectly input filed 'Count vertex'! It is  positive, integer number.\n";
                }
                if(!validationInput.isPositiveNumber(textFrom.getText())){
                    errors += "* You incorrectly input  field 'From'! It is  positive, integer number\n";
                }
                if(!validationInput.isPositiveNumber(textBefore.getText())){
                    errors += "* You incorrectly input field 'Before'! It is  positive, integer number.";
                }
                if(!errors.isEmpty()) {
                     JOptionPane.showMessageDialog(panel, errors );
                }
                else {
                    frameCountVertex.setVisible(false);
                    visThis.setNewRandomGraphMatrix(textCountVertex, textFrom, textBefore, percentOfEdges);
                    updateField();
                }
            });
        }

         @Override
    public void stateChanged(ChangeEvent e) {

        Object source = e.getSource();
        JSlider slider = (JSlider)source;
                percentOfEdges = slider.getValue();
    }

    private void updateField() {
        randomGraphEditDialog.countVertex = Integer.parseInt(textCountVertex.getText());
        randomGraphEditDialog.percentOfEdges = percentOfEdges;
        randomGraphEditDialog.from = Integer.parseInt(textFrom.getText());
        randomGraphEditDialog.before = Integer.parseInt(textBefore.getText());
    }
    
    public RandomGraphEditDialog getrandomGraphEditDialog() {
        return randomGraphEditDialog;
    }
    
}
