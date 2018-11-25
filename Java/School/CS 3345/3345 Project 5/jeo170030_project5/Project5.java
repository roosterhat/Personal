/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg5;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author eriko
 */
public class Project5 {

    public static void main(String[] args) {
        new Project5().run();
    }
    
    public void run(){
        Scanner sc = new Scanner(System.in);
        ArrayList<Integer> unsorted,sorted = new ArrayList();
        ArrayList<Pivot> pivots = new ArrayList(Arrays.asList(new Pivot[]{
                new Pivot("First Element",x->{return x.get(0);}),
                new Pivot("Random Element",x->{return x.get((int)(Math.random()*x.size()));}),
                new Pivot("Median of Random Elements",x->{
                    Set<Integer> elems = new HashSet();
                    int size;
                    for(int i = 0; i < 3; i++){
                        do{
                            size = elems.size();
                            if(x.size()-size<=0)
                                break;
                            elems.add((int)(Math.random()*x.size()));
                        }while(size == elems.size());
                    }
                    if(elems.size()<3){
                        ArrayList tmp = new ArrayList(elems);
                        for(int i = elems.size(); i < 3; i++)
                            tmp.add(tmp.get(0));
                        return pickMedian(tmp,x);
                    }
                    return pickMedian(new ArrayList(elems),x);
                }),
                new Pivot("Median of First, Middle, Last Elements",x->{
                    return pickMedian(new ArrayList(Arrays.asList(new Integer[]{0,x.size()/2,x.size()-1})),x);
                })}));
        
        while(true){
            System.out.println("Enter list size [1 - 1000000] ([q]uit): ");
            String input = sc.next();
            if(input.matches("[Qq](uit)?"))
                break;
            else{
                try{
                    int size = Integer.valueOf(input);
                    if(size > 0 && size <= 1000000)
                        unsorted = fillArray(size);
                    else{
                        throw new IllegalArgumentException("Size out of bounds");
                    }
                    System.out.println("Original: "+unsorted);
                    for(Pivot p : pivots){
                        sorted = (ArrayList<Integer>)unsorted.clone();
                        long start = System.nanoTime();
                        quicksort(sorted,0,size-1,p.picker);
                        long end = System.nanoTime();
                        System.out.println(p.name+": "+sorted);
                        System.out.println("Time: "+(double)((end-start))/1000000000);
                    }
                    writeToFile(unsorted,"unsorted.txt");
                    writeToFile(sorted,"sorted.txt");
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }            
        }
    }
    
    private ArrayList fillArray(int size){
        ArrayList<Integer> temp = new ArrayList(size);
        for(int i = 0; i < size; i++)
            temp.add((int)(Math.random()*size));
        return temp;
    }
    
    private int pickMedian(ArrayList<Integer> indexes, ArrayList<Integer> elems){
        return Math.max(Math.min(elems.get(indexes.get(0)),elems.get(indexes.get(1))), Math.min(Math.max(elems.get(indexes.get(0)),elems.get(indexes.get(1))), elems.get(indexes.get(2))));
    }
    
    public void  quicksort( ArrayList<Integer> a, int left, int right , PivotPicker p){
        if(left>=right)
            return;
        int pivot = p.pick(new ArrayList(a.subList(left, right)));
        int i = left, j = right;
        do{
            while(a.get(i) < pivot){i++;}
            while(a.get(j) > pivot){j--;}
            if (i <= j)
                swap(a, i++, j--);
        }while(i<=j);
        if(left < j)
            quicksort(a, left, j, p);   
	if(i < right)
            quicksort(a, i, right, p); 
    }
    
    private void swap(ArrayList<Integer> a, int i, int j){
        int temp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, temp);
    }
    
    private void writeToFile(ArrayList<Integer> array, String file)throws Exception{
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(array.toString());
        writer.close();
    }
    
}

class Pivot{
    String name;
    PivotPicker picker;
    public Pivot(String name, PivotPicker picker){
        this.name = name;
        this.picker = picker;
    }
}

interface PivotPicker{
    public int pick(ArrayList<Integer> array);
}
