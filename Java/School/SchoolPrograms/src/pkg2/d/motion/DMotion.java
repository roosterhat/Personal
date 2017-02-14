/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg2.d.motion;
import schoolprograms.SchoolProgramsCommon;
import java.awt.*;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
public class DMotion implements SchoolProgramsCommon
{

    String name = "2D Motion";

    public String getName()
    {
        return name;
    }
    public void run()
    {
        double dy=0;
        double angle=0;
        double velocity=0;
        JPanel myPanel = new JPanel(new GridLayout(0,1));
        JTextField dyfeild = new JTextField();
        JTextField anglefeild = new JTextField();
        JTextField velocityfeild = new JTextField();
        myPanel.add(new JLabel("Delta Y:"));
        myPanel.add(dyfeild);
        myPanel.add(new JLabel("Angle:"));
        myPanel.add(anglefeild);
        myPanel.add(new JLabel("Velocity:"));
        myPanel.add(velocityfeild);
        int result = JOptionPane.showConfirmDialog(null, myPanel, "2-D Motion", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) 
        {
            dy = Double.parseDouble(dyfeild.getText());
            angle = Double.parseDouble(anglefeild.getText());
            velocity = Double.parseDouble(velocityfeild.getText());
            double dx = velocity*Math.cos(Math.toRadians(angle));
            dx *= (velocity*Math.sin(Math.toRadians(angle)))/(9.81);
            dx += Math.sqrt((2*dy)/(9.81));
            double time = velocity/9.81+Math.sqrt((velocity*velocity)/(9.81*9.81)-(2*dy)/(9.81));
            JOptionPane.showMessageDialog(null, "Distance: "+dx+"\nTime: "+time);
        }
        
        
    }
}

