/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author eriko
 */
public class Homework4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Integer> test = new ArrayList();
        for(int i=0;i<10;i++)
            test.add((int)(Math.random()*100));
        test.add(100);
        test.forEach(x->System.out.println(x));
        System.out.println(maximum(test));
        System.out.println(maximum(new ArrayList()));
        test.forEach(x->System.out.println(x));
        ArrayList<Integer> duplicate = new ArrayList();
        duplicate.add(1);
        duplicate.add(2);
        duplicate.add(2);
        duplicate.add(3);
        duplicate.add(4);
        duplicate.add(5);
        duplicate.add(5);
        duplicate.add(5);
        removeDuplicate(duplicate);
        duplicate.forEach(x->System.out.println(x));
    }
    
    public static Integer maximum(ArrayList<Integer> array){
        Integer max = null;
        if(!(array==null || array.isEmpty())){
            for(Integer i : array)
                max = Math.max(max == null ? array.get(0) : max, i);
        }
        return max;
    }
    
    public static void removeDuplicate(ArrayList<Integer> array){
        Set<Integer> temp = new HashSet();
        array.removeIf(x->!temp.add(x));
    }
    
}
