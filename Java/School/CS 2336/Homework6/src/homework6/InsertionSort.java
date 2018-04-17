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
public class InsertionSort extends SortingAlgorithm{
    public InsertionSort(){
        super("InsertionSort");
    }
    
    public Result sort(Integer[] list, Arrangment arrangment){
        int comp = 0;
        int move = 0;
        long start = System.nanoTime();
        for (int i=1; i<list.length; ++i)
        {
            int key = list[i];
            int j = i-1;
            while (j>=0 && list[j] > key)
            {
                list[j+1] = list[j];
                j = j-1;
                comp++;
                move++;
            }
            list[j+1] = key;
            move++;
        }
        long time = System.nanoTime()-start;
        return new Result(list.length, this, arrangment, comp, move, time);
    }
}
