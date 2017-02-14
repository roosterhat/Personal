/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Matrixs;

import java.awt.GridLayout;
import java.util.*;
import javax.swing.*;
import schoolprograms.SchoolProgramsCommon;

public class Matrix implements SchoolProgramsCommon
{
    String name = "Matrix";
    ArrayList matrix;
    ArrayList<JTextField> temp;
    public static void main(String[] args)
    {
        
    }
    
    public void run()
    {
        matrix = new ArrayList();
        temp  = new ArrayList();
        int result = JOptionPane.showOptionDialog(null, "Select a Matrix", "Matrix", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"2X2", "3X3","Cancel"}, null);
        
        if (result == 0)
        {
            matrix2X2();
        }
        else if(result == 1)
        {
            matrix3X3();
        }
    }
    
    public void matrix2X2()
    {
        getMatrix(2);
        ArrayList<Double> d = new ArrayList();
        for(Object o:matrix)
        {
            d.add(Double.parseDouble(String.valueOf(o)));
        }
        double result = doMatrix(d);
        JOptionPane.showMessageDialog(null,matrix.get(0)+"*"+matrix.get(3)+"-"+matrix.get(1)+"*"+matrix.get(2)+"="+result,"Results",1);
    }
    
    public double doMatrix(ArrayList<Double> m)
    {
        return m.get(0)*m.get(3)-m.get(1)*m.get(2);
    }
    
    public void matrix3X3()
    {
        getMatrix(3);
        if(!String.valueOf(matrix.get(0)).matches("[-+]?\\d*\\.?\\d+"))
        {
            ArrayList<Double> d = new ArrayList();
            d.add(Double.parseDouble(String.valueOf(matrix.get(4))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(5))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(7))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(8))));
            double value1 = doMatrix(d);
            d = new ArrayList();
            d.add(Double.parseDouble(String.valueOf(matrix.get(3))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(5))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(6))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(8))));
            double value2 = doMatrix(d);
            d = new ArrayList();
            d.add(Double.parseDouble(String.valueOf(matrix.get(3))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(4))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(6))));
            d.add(Double.parseDouble(String.valueOf(matrix.get(7))));
            double value3 = doMatrix(d);
            JOptionPane.showMessageDialog(null,value1+""+matrix.get(0)+"-("+value2+""+matrix.get(1)+")+"+value3+""+matrix.get(2)+"="+(value1-value2+value3),"Results",1);
        }
        else
        {
            ArrayList<Double> m = new ArrayList();
            for(Object o:matrix)
            {
                m.add(Double.parseDouble(String.valueOf(o)));
            }
            double result = (m.get(0)*m.get(4)*m.get(8) + m.get(1)*m.get(5)*m.get(6) + m.get(2)*m.get(3)*m.get(7))-
                            (m.get(2)*m.get(4)*m.get(6) + m.get(1)*m.get(3)*m.get(8) + m.get(0)*m.get(5)*m.get(7));
            JOptionPane.showMessageDialog(null,result,"Results",1);
        }
    }
    
    public void getMatrix(int size)
    {
        JPanel panel = new JPanel(new GridLayout(size,size));
        for(int x=0;x<size*size;x++)
        {
            JTextField a = new JTextField();
            temp.add(a);
            panel.add(a);
        }
        int result = JOptionPane.showConfirmDialog(null, panel, size+"X"+size+" Matrix", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) 
        {
            for(int x=0;x<temp.size();x++)
            {
                matrix.add((temp.get(x).getText()));
            }
        }
    }
    
    public String getName()
    {
        return name;
    }
}
