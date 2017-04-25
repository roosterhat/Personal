/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchalgorithms;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author ostlinja
 */
public class SearchAlgorithms {

    public static void main(String[] args) {   
        ArrayList numbers = generateNumbers(100,10000);
        ArrayList<Integer> searchIndexes = new ArrayList();
        //searchIndexes.addAll(numbers.subList(20, 30));

        
        ArrayList methods = new ArrayList();
        //methods.add(new Linear(numbers,searchIndexes));
        //methods.add(new Binary(numbers,searchIndexes));
        //methods.add(new Interpolation(numbers,searchIndexes));  
        String s = "C:\\Users\\ostlinja\\Documents\\NetBeansProjects\\SearchAlgorithms\\src\\searchalgorithms\\search.txt";
        String n = "C:\\Users\\ostlinja\\Documents\\NetBeansProjects\\SearchAlgorithms\\src\\searchalgorithms\\numbers.txt";
        methods.add(new Linear(n,s));
        methods.add(new Binary(n,s));
        methods.add(new Interpolation(n,s));
        
        preformSearchs(methods);
        printResults(methods);
    }  
    
    public static ArrayList generateNumbers(int s,int range)
    {
        ArrayList temp = new ArrayList();
        for(int i=0;i<s;i++)
            temp.add((int)(Math.random()*range));
        return temp;
    }
    
    public static void preformSearchs(ArrayList<SearchMethod> m)
    {
        for(SearchMethod s: m)
            s.runSearch();
    }
    
    public static void printResults(ArrayList<SearchMethod> m)
    {
        String temp = "%-16s";
        for(SearchMethod _:m)
            temp+= "%-15s";
        ArrayList names = new ArrayList();
        names.add("");
        for(SearchMethod s:m)
            names.add(s.getName()+" Avg");

        String header = String.format(temp,names.toArray());
        
        temp = "%-9s: %-5d";
        for(SearchMethod _:m)
            temp+= "%-15.2f";
        ArrayList succ = new ArrayList();
        succ.add("Success");
        succ.add(m.get(0).success);
        for(SearchMethod s:m)
        {
            double avg = 0;
            for(int i:s.succProbs)
                avg+=i;
            avg = avg/s.succProbs.size();
            succ.add(avg);
        }
        String success = String.format(temp,succ.toArray());

        ArrayList fail = new ArrayList();
        fail.add("Failures");
        fail.add(m.get(0).failures);
        for(SearchMethod s:m)
        {
            double avg = 0;
            for(int i:s.failProbs)
                avg+=i;
            avg = avg/s.failProbs.size();
            fail.add(avg);
        }
        String failures = String.format(temp,fail.toArray());
        
        System.out.println(header);
        System.out.println(success);
        System.out.println(failures);
    }
}
