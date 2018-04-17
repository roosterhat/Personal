/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework6;

import java.util.HashMap;
import java.util.Map;
import java.lang.Long;

/**
 *
 * @author eriko
 */
public class Homework6 {

    /**
     * @param args the command line arguments
     */
    Map<String, SortingAlgorithm> alg;
    Map<String, Arrangment> arrng;
    public Homework6(){
        alg = new HashMap();    //create all algorithms
        alg.put("InsertionSort",new InsertionSort());
        alg.put("SelectionSort",new SelectionSort());
        alg.put("QuickSort",new QuickSort());
        alg.put("MergeSort",new MergeSort());
        alg.put("HeapSort",new HeapSort());
        alg.put("RadixSort",new RadixSort());
        
        arrng = new HashMap();  //create all arrangments
        arrng.put("InOrder", new InOrder());
        arrng.put("ReverseOrder", new ReverseOrder());
        arrng.put("AlmostOrder", new AlmostOrder());
        arrng.put("Random", new Random());
        
        new GUI(alg,arrng).setVisible(true);    //create GUI
    }
    
    public static void main(String[] args) {
        new Homework6();
    }
    
}
