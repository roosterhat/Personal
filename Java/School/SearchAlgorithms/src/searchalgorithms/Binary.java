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
public class Binary extends SearchMethod {

    public Binary(){}
    
    public Binary(ArrayList a, ArrayList b)
    {
        values.addAll(sort(a));
        searchValues.addAll(b);
    }
    
    public Binary(String a,String b)
    {
        values = sort(readFile(a,size));
        searchValues = readFile(b,search);
    }
    
    public String getName(){return "Binary";}

    public int search(int value) 
    {
        probes = 0;
        if(!values.isEmpty() && isOrdered(values))
        {
            int low = 0;
            int high = values.size();
            while(high>low)
            {
                probes++;
                int mid = low + (high-low)/2;
                if(values.get(mid)==value)
                {
                    succProbs.add(probes);
                    success++;
                    return mid;
                }
                else
                {
                    if(values.get(mid)>value)
                        high = mid-1;
                    else if(values.get(mid)<value)
                        low = mid+1;
                }
                    
            }
        }
        failProbs.add(probes);
        failures++;
        return -1;
    }
    
    
   
}
