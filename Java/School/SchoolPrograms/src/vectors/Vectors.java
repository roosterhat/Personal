/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vectors;
import java.awt.*;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import schoolprograms.SchoolProgramsCommon;

public class Vectors implements SchoolProgramsCommon
{
    ArrayList<vector> vectorList = new ArrayList();
    String name = "Vectors";
    public static void main(String[] args)
    {
    }
    public void run()
    {
        Vectors vec = new Vectors();
        vec.calculation();
    }
    public String getName()
    {
        return name;
    }
    public void calculation()
    {
        Scanner scan = new Scanner(System.in);
        String number = JOptionPane.showInputDialog("How many vectors are there?", null);
        int amount = Integer.parseInt(number);
        ArrayList<JTextField> panelLength = new ArrayList();
        ArrayList<JTextField> panelAngle = new ArrayList();
        JPanel myPanel = new JPanel(new GridLayout(0,4));
        for(int x=0;x<amount;x++)
        {
            JTextField l = new JTextField();
            panelLength.add(l);
            JTextField a = new JTextField();
            panelAngle.add(a);
            myPanel.add(new JLabel(x+"   Magnitude:"));
            myPanel.add(panelLength.get(x));
            myPanel.add(new JLabel("Angle:"));
            myPanel.add(panelAngle.get(x));
        }
            int result = JOptionPane.showConfirmDialog(null, myPanel, "Vectors", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) 
            {
                for(int x=0;x<amount;x++)
                {
                    vector v = new vector(Double.parseDouble(panelLength.get(x).getText()),Double.parseDouble(panelAngle.get(x).getText()));
                    vectorList.add(v); 
                }
                
            }
            printResults();
    }
    public void printResults()
    {
        double totalXLength=0;
        double totalYLength=0;
        double magnitude,resultant;
        if(vectorList.size()>0)
        {
            String values = "";
            magnitude = Math.sqrt(getTotalLength()*getTotalLength()+getTotalHeight()*getTotalHeight());
            resultant = Math.toDegrees(Math.atan((getTotalLength()/getTotalHeight())));
            for(int x=0;x<vectorList.size();x++)
            {
                double xlength=vectorList.get(x).getHeight();
                double ylength=vectorList.get(x).getLength();
                xlength*=1000;
                ylength*=1000;
                xlength = Math.round(xlength);
                ylength = Math.round(ylength);
                xlength/=1000;
                ylength/=1000;
                totalXLength+=xlength;
                totalYLength+=ylength;
                values+=x+"  X= "+Double.toString(xlength)+"  Y= "+Double.toString(ylength)+"\n";
                        
            }
            JOptionPane.showMessageDialog(null, "Magnitude = "+magnitude+"\nDirection = "+resultant+"\n           Components\n"+values+"\nTotal X: "+totalXLength+"\nTotal Y: "+totalYLength,"Results",1);
            
        }
    }
    public double getTotalLength()
    {
        double total = 0;
        for(int x=0;x<vectorList.size();x++)
        {
            total += vectorList.get(x).getLength();
        }
        return total;
    }
    public double getTotalHeight()
    {
        double total = 0;
        for(int x=0;x<vectorList.size();x++)
        {
            total += vectorList.get(x).getHeight();
        }
        return total;
    }
}

class vector
{
    double length,angle,ax,ay;
    public vector(double l, double a)
    {
       length = l;
       angle = a;
       ay = length*(Math.cos(Math.toRadians(angle)));
       ax = length*(Math.sin(Math.toRadians(angle)));
    }
    public double getLength()
    {
        return ax;
    }
    public double getHeight()
    {
        return ay;
    }
}
