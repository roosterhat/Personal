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
public class BubbleSort extends SortingMethod{
    public BubbleSort(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Bubble Sort";}
    
    public void sort()
    {
        sorted = bubbleSort(unsorted);
    }
    
    public ArrayList bubbleSort(ArrayList<Integer> l)
    {
        ArrayList<Integer> a = (ArrayList<Integer>)l.clone();
        for(int i=0;i<a.size();i++)
        {
            boolean swap = false;
            for(int j=0;j<a.size()-1;j++)
            {
                comparisons++;
                if(a.get(j)>a.get(j+1))
                {
                    int t = a.get(j);
                    a.set(j,a.get(j+1));
                    a.set(j+1,t);
                }
            }
            if(swap)
                break;
        }
        return a;
    }
}
