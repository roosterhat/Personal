/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkgfinal;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ostlinja
 */
public class Final {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Integer> numbers = fillList(100);
        ArrayList<IBucket> buckets = new ArrayList();
        for(int i = 0;i<10;i++)
        {
            buckets.add(new Bucket<Integer>());
        }
        System.out.println("Before Sort:\n"+numbers.toString());
        numbers = radixSort(buckets,numbers);
        System.out.println("After Sort:\n"+numbers.toString());
        
    }
    
    public static ArrayList<Integer> fillList(int s)
    {
        ArrayList<Integer> temp = new ArrayList();
        for(int i=0;i<s;i++)
        {
            temp.add((int)(Math.random()*1000));
        }
        return (ArrayList<Integer>)temp.clone();
    }

    public static int maxDigits(ArrayList<Integer> x)
    {
        int largest = 0;
        for(Integer i:x)
        {
            int l = Integer.toString(i).length();
            if(l>largest)
                largest = l;
        }
        return largest;
    }
        
    public static ArrayList<Integer> getNumbers(ArrayList<Integer> a,int d)
    {
        ArrayList<Integer> temp = new ArrayList();
        for(Integer i: a)
        {
            int l = Integer.toString(i).length();
            if(l==d)
                temp.add(i);
        }
        return temp;
    }
    
    public static ArrayList<Integer> radixSort(ArrayList<IBucket> bs, ArrayList<Integer> l)
    {
        ArrayList<Integer> temp = new ArrayList();
        for(int digit = 1;digit<=maxDigits(l);digit++)
        {
            for(int x = 0;x<bs.size();x++)
            {
                for(Integer i:l)
                {
                    double convNum = Math.floor(i/Math.pow(10, digit-1));
                    if(x==convNum)
                        if(convNum==0 && digit>1){}
                        else
                            bs.get(x).add(i);
                }
            }
            for(IBucket<Integer> b:bs)
            {
                Collections.sort(b.elements());
                //System.out.println(bs.indexOf(b)+":"+b.toString());
                b.dump(temp);
            }
        }
        return (ArrayList<Integer>)temp.clone();
    }
    
}
