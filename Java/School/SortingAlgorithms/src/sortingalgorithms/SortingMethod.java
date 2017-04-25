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
public abstract class SortingMethod {
    ArrayList unsorted = new ArrayList();
    ArrayList sorted = new ArrayList();
    int comparisons = 0;
    public void sort()
    {
        sorted = (ArrayList)unsorted.clone();
    }
    public String getName()
    {
        return "";
    }
    public String toString()
    {
        return "";//Unsorted: "+unsorted.toString()+"\nSorted: "+sorted.toString();
    }
}
