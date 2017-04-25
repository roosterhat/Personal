/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sortingalgorithms;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class LinearSelect extends SortingMethod {
    public LinearSelect(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Linear Select";}
    
    public void sort()
    {
        ArrayList temp = (ArrayList)unsorted.clone();
        for(int i=0;i<unsorted.size();i++)
        {
            int lowest = 0;
            for(int index = 0;index<temp.size();index++)
            {
                comparisons++;
                if((int)temp.get(index)<(int)temp.get(lowest))
                    lowest = index;
            }
            sorted.add(temp.get(lowest));
            temp.remove(lowest);
        }
    }
    
    
}
