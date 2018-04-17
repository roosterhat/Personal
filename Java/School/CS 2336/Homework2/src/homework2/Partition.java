/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework2;

import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Partition {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.print("Enter List(separated by ' ' or ',': ");
        String[] elements = scanner.nextLine().split("[,\\s]"); //gets input line a splits at each space or ','
        int[] list = new int[elements.length];
        for(int i = 0;i<elements.length;i++)                    //converts strings to int
            list[i] = Integer.valueOf(elements[i]);
        System.out.println(Arrays.toString(list));
        partition(list);
        System.out.println(Arrays.toString(list));
    }
    
    public static int partition(int[] list){
        int pivot;
        int pivotIndex = 0;
        if(list.length>0){
            pivot = list[0];
            for(int i=1;i<list.length;i++){
                if(list[i]<=pivot){                 //if number is less than pivot swap until its on the left side of the pivot
                    for(int x=i;x>pivotIndex;x--)
                        swap(list,x,x-1);
                    pivotIndex++;
                }
            }
        }
        return pivotIndex;
    }
    
    public static void swap(int[] list, int pos1, int pos2){
        int temp = list[pos1];
        list[pos1] = list[pos2];
        list[pos2] = temp;
    }
}
