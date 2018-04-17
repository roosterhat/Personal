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
public class MergeSort extends SortingAlgorithm{
    private long comp,move;
    public MergeSort(){
        super("MergeSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        comp = 0;
        move = 0;
        long start = System.nanoTime();
        sort(list,0,list.length-1);
        long time = System.nanoTime()-start;
        return new Result(list.length,this,arrangment,comp,move,time);
    }
    
    private void merge(Integer arr[], int l, int m, int r)
    {
        int n1 = m - l + 1;
        int n2 = r - m;
 
        int L[] = new int [n1];
        int R[] = new int [n2];
 
        for (int i=0; i<n1; ++i)
            L[i] = arr[l + i];
        for (int j=0; j<n2; ++j)
            R[j] = arr[m + 1+ j];
        move+=(n1+n2);

        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2)
        {
            if (L[i] <= R[j])
            {
                arr[k] = L[i];
                i++;
            }
            else
            {
                arr[k] = R[j];
                j++;
            }
            comp++;
            move++;
            k++;
        }
 
        move+=(n1-1)+(n2-j);
        while (i < n1)
        {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2)
        {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    private void sort(Integer arr[], int l, int r)
    {
        if (l < r)
        {
            int m = (l+r)/2;
            
            sort(arr, l, m);
            sort(arr , m+1, r);
 
            merge(arr, l, m, r);
        }
    }
}
