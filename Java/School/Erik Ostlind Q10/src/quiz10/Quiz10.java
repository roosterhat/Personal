/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quiz10;

import java.util.Random;

/*
 * Grade: 
 * 
 * INSTRUCTIONS
 * 
 * Complete the following program by writing the method allSatisfyP.
 * This method takes as input a predicate and an array of Integer (Integer[])
 * and returns a Boolean. It returns true if all elements of the given
 * array satisfy the given predicate. Otherwise, it returns false.
 * 
 * Make sure to provide a loop invariant and prove that your implementation
 * works correctly.
 */

public class Quiz10 {

    public interface IPred<X>
    {
        public Boolean p(X val);
    }
    
    public static Integer[] createArray(int size)
    // Purpose: To return an Integer array of the given size 
    //          containing random integers in 0..999999
    {
            Random randomGenerator = new Random();

            Integer[] A = new Integer[size];

            for (int i = 0; i < size; i = i + 1)
            {
                    A[i] = randomGenerator.nextInt(1000000);
            }

            return(A);
    }

    //purpose: to check the contense of an array and return true or false whether or not the values of the array
    //         pass the given predicate
    //no loop invariant needed because for each loops take care of all the iteration themselves
    //works by looping for each item in an array and doing the specified commands in the loop
    public static  boolean allSatisfyP(IPred<Integer> p, Integer[] l)
    {
        for(Integer i:l)
        {
            if(!p.p(i))
                return false;
        }
        return true;
    }
    
    public static void main(String[] args) 
    {
            Integer[] A = createArray(101);
            
            System.out.println("Array:");
            for(Integer i:A)
            {
                System.out.print(i+",");
            }

            System.out.println();
            
            Boolean res = allSatisfyP(i -> (i.intValue() % 10 != 0), A);

            System.out.println("All elements of A satisfy are not multiple of 10: " + res);

            res = allSatisfyP(i -> (i.intValue() > -10), A);

            System.out.println("All elements of A satisfy are greater than -10: " + res);
            
            res = allSatisfyP(i -> (i.intValue() < 1000001), A);

            System.out.println("All elements of A satisfy are less than 1000001: " + res);

    }
    
    
    
}
