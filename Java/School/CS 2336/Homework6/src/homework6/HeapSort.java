/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework6;

/**
 *
 * @author eriko
 */
public class HeapSort extends SortingAlgorithm{
    private long comp,move;
    public HeapSort(){
        super("HeapSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        comp = 0;
        move = 0;
        long start = System.nanoTime();
        sort(list);
        long time = System.nanoTime()-start;
        return new Result(list.length,this,arrangment,comp,move,time);
    }
    
    private void sort(Integer arr[])
    {
        int n = arr.length;
         for (int i = n / 2 - 1; i >= 0; i--)
            heapify(arr, n, i);
 
        for (int i=n-1; i>=0; i--)
        {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            move+=2;
            heapify(arr, i, 0);
        }
    }

    private void heapify(Integer arr[], int n, int i)
    {
        int largest = i;
        int l = 2*i + 1;
        int r = 2*i + 2;
        if (l < n && arr[l] > arr[largest])
            largest = l;
        comp++;
        if (r < n && arr[r] > arr[largest])
            largest = r;
        comp++;
        if (largest != i)
        {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            move+=2;
            heapify(arr, n, largest);
        }
    }
    
}
