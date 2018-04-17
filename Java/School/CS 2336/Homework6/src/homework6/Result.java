/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework6;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author eriko
 */
public class Result {
    int size;
    SortingAlgorithm algorithm;
    Arrangment dataArrangment;
    long comparisons;
    long movements;
    long time;//nanoseconds
    public Result(int size, SortingAlgorithm algorithm, Arrangment arrangment, long comparisons, long movements, long time){
        this.size = size;
        this.algorithm = algorithm;
        this.dataArrangment = arrangment;
        this.comparisons = comparisons;
        this.movements = movements;
        this.time = time; 
    }
    
    public JPanel generateOuputPanel(){
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0,1));
        p.add(new JLabel("Comparisons: "+comparisons));
        p.add(new JLabel("Movements: "+movements));
        p.add(new JLabel("Time: "+((double)(time)/1000000000)+" seconds"));
        return p;
    }
}


