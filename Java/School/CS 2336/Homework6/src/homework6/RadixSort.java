/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework6;

import java.util.Arrays;

/**
 *
 * @author eriko
 */
public class RadixSort extends SortingAlgorithm{
    private long comp,move;
    public RadixSort(){
        super("RadixSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        comp = 0;
        move = 0;
        long start = System.nanoTime();
        int m = getMax(list);
        for (int exp = 1; m/exp > 0; exp *= 10)
            countSort(list, exp);
        long time = System.nanoTime()-start;
        return new Result(list.length,this,arrangment,comp,move,time);
    }
    
    private int getMax(Integer arr[])
    {
        int mx = arr[0];
        for (int i = 1; i < arr.length; i++)
            if (arr[i] > mx)
                mx = arr[i];
        comp+=arr.length;
        return mx;
    }

    private void countSort(Integer arr[], int exp)
    {
        int n = arr.length;
        int output[] = new int[n];
        int i;
        int count[] = new int[10];
        Arrays.fill(count,0);
        move+=10;

        for (i = 0; i < n; i++)
            count[ (arr[i]/exp)%10 ]++;

        for (i = 1; i < 10; i++)
            count[i] += count[i - 1];
        move+=10;
 
        for (i = n - 1; i >= 0; i--)
        {
            output[count[ (arr[i]/exp)%10 ] - 1] = arr[i];
            count[ (arr[i]/exp)%10 ]--;
        }
        move+=n-1;

        for (i = 0; i < n; i++)
            arr[i] = output[i];
        move+=n;
    }
}
