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
public class Binary extends SortingMethod{
    public Binary(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Binary Sort";}
    
    public void sort()
    {
        for(Integer i:(ArrayList<Integer>)unsorted)
        {
            
            sorted.add(findPos(i,sorted), i);
        }
    }
    
    public int findPos(int v,ArrayList<Integer> l)
    {
        if(!l.isEmpty())
        {
            int low = 0;
            int high = l.size();
            while(high>low+1)
            {
                comparisons++;
                int mid = low + (high-low)/2;
                if(l.get(mid)==v)
                    return mid;
                else
                {
                    if(l.get(mid)>v)
                        high = mid;
                    else if(l.get(mid)<v)
                        low = mid;                    
                }
            }
            if(l.get(high-1)>v)
                return high-1;
            else
                return high;
        }
        return 0;
    }
}
