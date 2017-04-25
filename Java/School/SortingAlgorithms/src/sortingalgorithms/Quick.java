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
public class Quick extends SortingMethod{
    public Quick(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Quick Sort";}
    
    public void sort()
    {
        sorted = (ArrayList)unsorted.clone();
        partition(0,unsorted.size()-1);
    }
    
    public void partition(int low,int high)
    {
        if (low < high)
        {
            int pi = sort(low, high);
            partition(low, pi-1);
            partition(pi+1, high);
        }
    }
    
    public int sort(int low,int high)
    {
        int pivot = (int)sorted.get(high); 
        int i = (low-1); 
        for (int j=low; j<=high-1; j++)
        {
            comparisons++;
            if ((int)sorted.get(j) <= pivot)
            {
                i++;
                int temp = (int)sorted.get(i);
                sorted.set(i, sorted.get(j));
                sorted.set(j, temp);
            }
        }
        int temp = (int)sorted.get(i+1);
        sorted.set(i+1, sorted.get(high));
        sorted.set(high, temp);
        
        return i+1;
    }
}
