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
public class MergeSort extends SortingMethod {
    public MergeSort(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
    }
    public String getName(){return "Merge Sort";}
    
    public void sort()
    {
        sorted = mergeSort(unsorted);
    }
    
    public ArrayList mergeSort(ArrayList l)
    {
        if(l.size()<=1)
            return l;
        
        ArrayList a = new ArrayList();
        ArrayList b = new ArrayList();
        for(int i=0;i<l.size();i++)
        {
            comparisons++;
            if(i<l.size()/2)
                a.add(l.get(i));
            else
                b.add(l.get(i));
        }
        a = mergeSort(a);
        b = mergeSort(b);
        
        return merge(a,b);
    }
    
    public ArrayList merge(ArrayList<Integer> a,ArrayList<Integer> b)
    {
        ArrayList res = new ArrayList();
        while(!a.isEmpty() && !b.isEmpty()){
            comparisons++;
            if(a.get(0)<=b.get(0))
            {
                res.add(a.get(0));
                a.remove(0);
            }
            else
            {
                res.add(b.get(0));
                b.remove(0);
            }
        }
        for(int i:a)
            res.add(i);
        for(int i:b)
            res.add(i);
        return res;
    }
    
}
