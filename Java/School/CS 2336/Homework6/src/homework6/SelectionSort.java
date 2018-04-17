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
public class SelectionSort extends SortingAlgorithm{
    public SelectionSort(){
        super("SelectionSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        int comp = 0;
        int move = 0;
        long start = System.nanoTime();
        int n = list.length;
        for (int i = 0; i < n-1; i++)
        {
            int min_idx = i;
            for (int j = i+1; j < n; j++){
                if (list[j] < list[min_idx])
                    min_idx = j;
                comp++;
            }
            int temp = list[min_idx];
            list[min_idx] = list[i];
            list[i] = temp;
            move+=2;
        }
        long time = System.nanoTime()-start;
        return new Result(list.length ,this, arrangment,comp,move,time);
    }
}
