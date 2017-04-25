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
public class ShellSort extends SortingMethod{
    public ShellSort(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Shell Sort";}
    
    public void sort()
    {
        sorted = shellSort(unsorted);
    }
    
    public ArrayList shellSort(ArrayList<Integer> l)
    {
        ArrayList<Integer> a = (ArrayList<Integer>)l.clone();
        int interval = 0;
        while(interval < a.size()/3)
            interval += interval*3 + 1;
        
        while(interval>0){
            for(int out=interval;out<a.size();out++)
            {
                int val = a.get(out);
                int in = out;
                while(in>interval-1 && a.get(in-interval)>=val)
                {
                    comparisons++;
                    a.set(in,a.get(in-interval));
                    in -= interval;
                }
                a.set(in, val);
            }
            interval = (interval-1)/3;
        }
        return a; 
    }
}
