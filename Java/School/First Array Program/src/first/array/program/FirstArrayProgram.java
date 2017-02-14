/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package first.array.program;

/**
 *
 * @author ostlinja
 */
public class FirstArrayProgram {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        int[] a = {15,123,34,35,6,76,28,188,77,512,2};
        printIntArray(a);
        System.out.println(" The sum is: "+sumIntArray(a));
        insort(a);
        System.out.print("Sorted: ");
        printIntArray(a);
    }


    public static void printIntArray(int[] i)
    {
        for(int v:i)
        {
            System.out.print(v+" ");
        }
        System.out.println();
    }
    
    public static int sumIntArray(int[] i)
    {
        int sum = 0;
        for(int v:i)
        {
            sum+=v;
        }
        return sum;
    }

    public static void insort(int[] x)
    {
        for(int i=0;i<x.length;i++)
        {
            int smallest = x[i];
            int index = i;
            for(int a=i;a<x.length;a++)
            {
                if(x[a]<smallest)
                {
                    index = a;
                    smallest = x[a];
                }
            }
            int b = x[i];
            x[i] = x[index];
            x[index] = b;
        }
    }
    
}
