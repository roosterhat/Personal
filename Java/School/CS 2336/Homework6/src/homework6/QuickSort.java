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
public class QuickSort extends SortingAlgorithm{
    private int comp,move;
    public QuickSort(){
        super("QuickSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        comp = 0;
        move = 0;
        long start = System.nanoTime();
        sort(list,0,list.length-1);
        long time = System.nanoTime()-start;
        return new Result(list.length,this,arrangment,comp,move,time);
    }
    
    private void sort(Integer arr[], int low, int high)
    {
        int i = low, j = high;
        int pivot = arr[low + (high-low)/2];
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
                comp++;
            }
            while (arr[j] > pivot) {
                j--;
                comp++;
            }
            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                move+=2;
                i++;
                j--;
            }
        }
        if (low < j)
            sort(arr,low, j);
        if (i < high)
            sort(arr,i, high);
    }
}
