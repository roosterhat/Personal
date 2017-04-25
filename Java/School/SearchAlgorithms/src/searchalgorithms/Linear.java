/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchalgorithms;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class Linear extends SearchMethod{
    
    public Linear(){}
    
    public Linear(ArrayList a, ArrayList b)
    {
        values.addAll(sort(a));
        searchValues.addAll(b);
    }
    
    public Linear(String a,String b)
    {
        values = sort(readFile(a,size));
        searchValues = readFile(b,search);
    }
    
    public String getName(){return "Linear";}
    
    public int search(int value)
    {
        probes = 0;
        if(!values.isEmpty() && isOrdered(values))
        {
            for(int i:values)
            {
                probes++;
                if(i==value)
                {
                    succProbs.add(probes);
                    success++;
                    return values.indexOf(i);
                }
                if(i>value)
                    break;
                    
            }
        }
        failProbs.add(probes);
        failures++;
        return -1;
    }
}
