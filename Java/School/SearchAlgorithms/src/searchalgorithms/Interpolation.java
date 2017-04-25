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
public class Interpolation extends SearchMethod{
    
    public Interpolation(){}
    
    public Interpolation(ArrayList a, ArrayList b)
    {
        values.addAll(sort(a));
        searchValues.addAll(b);
    }
    
    public Interpolation(String a,String b)
    {
        values = sort(readFile(a,size));
        searchValues = readFile(b,search);
    }
    
    public String getName(){return "Interpolation";}
    
    
    public int search(int value)
    {
        probes = 0;
        if(!values.isEmpty() && isOrdered(values))
        {
            int low = 0;
            int high = values.size()-1;
            while(low<=high && value>=values.get(low) && value<=values.get(high))
            {
                probes++;
                int pos = low + ((value-values.get(low))*(high-low) / (values.get(high)-values.get(low)));
                if(value==values.get(pos))
                {
                    succProbs.add(probes);
                    success++;
                    return pos;
                }
                else 
                {
                    if(values.get(pos)<value)
                        low = pos+1;
                    else
                        high = pos-1;
                }
            }
        }
        failProbs.add(probes);
        failures++;
        return -1;
    }
}
