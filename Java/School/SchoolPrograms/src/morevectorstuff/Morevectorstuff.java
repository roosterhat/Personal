/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package morevectorstuff;
import schoolprograms.SchoolProgramsCommon;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Morevectorstuff implements SchoolProgramsCommon
{

    String name = "Weight Vectors";
    public static void main(String[] args)
    {
    }
    public void run()
    {
        double x,y,z;
        JPanel myPanel = new JPanel(new GridLayout(0,6));
        JTextField a = new JTextField();
        JTextField b = new JTextField();
        JTextField C = new JTextField();
        myPanel.add(new JLabel("Alpha:"));
        myPanel.add(a);
        myPanel.add(new JLabel("Beta:"));
        myPanel.add(b);
        myPanel.add(new JLabel("Downward force:"));
        myPanel.add(C);
        int result = JOptionPane.showConfirmDialog(null, myPanel, "Vectors", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) 
        {
            
            printResults(Double.parseDouble(a.getText()),Double.parseDouble(b.getText()),Double.parseDouble(C.getText()));
        }
    }
    public String getName()
    {
        return name;
    }
    public static void printResults(double alpha, double beta, double DForce)
    {
        double B = DForce/((Math.cos(Math.toRadians(beta)))*(Math.sin(Math.toRadians(alpha)))/(Math.cos(Math.toRadians(alpha)))+(Math.sin(Math.toRadians(beta))));
        double A = B*(Math.cos(Math.toRadians(beta)))/(Math.cos(Math.toRadians(alpha)));
        JOptionPane.showMessageDialog(null,"F1 = "+A+"\nF2 = "+B+"\nDownward Force = "+DForce,"Results",1);
    }
}
