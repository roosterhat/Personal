/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sortingalgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ostlinja
 */
public class SortingAlgorithms {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int size = 10000;
        //int[] n = {391,463,590,439,332,795,764,40,998,146};
        //{3,7,11,12,18,25,31,36,48,51,57,59,65,73,84,88,94,97,110,119,137,142,151,158,161,174,176,179,185,192,194,208,217,221,228,232,235,241,242,243,251,257,260,262,274,282,285,290,293,304,310,313,317,325,341,346,350,356,365,373,381,382,388,400,417,421,425,429,432,441,450,455,458,461,472,482,488,495,502,507,511,516,523,526,528,539,547,552,556,563,572,573,574,579,592,599,604,610,617,628,637,642,648,655,668,669,674,679,683,692,701,709,717,736,745,751,753,759,772,775,781,786,792,794,803,807,818,821,825,834,841,847,853,858,871,875,884,888,892,902,924,926,938,945,955,961,974,982,989,995};
        //ArrayList<Integer> numbers = new ArrayList();
        //for(int i:n)
        //    numbers.add(i);
        ArrayList numbers = generateNumbers(size,1000);
        ArrayList<SortingMethod> methods = new ArrayList();
        methods.add(new LinearSelect(numbers));
        methods.add(new HeapSelect(numbers));
        methods.add(new Quick(numbers));
        methods.add(new Binary(numbers));
        methods.add(new MergeSort(numbers));
        methods.add(new ShellSort(numbers));
        methods.add(new BubbleSort(numbers));
        
        sortAll(methods);
        printResults(methods);
    }
    
    public static void sortAll(ArrayList<SortingMethod> m)
    {
        for(SortingMethod s:m)
            s.sort();
    }
    
    public static void printResults(ArrayList<SortingMethod> m)
    {
        for(SortingMethod s:m)
            System.out.println(s.getName()+"\nComparisons: "+s.comparisons+"\n");
    }
    
     public static ArrayList generateNumbers(int s,int range)
    {
        ArrayList temp = new ArrayList();
        for(int i=0;i<s;i++)
            temp.add((int)(Math.random()*range));
        return temp;
    }
    
}
