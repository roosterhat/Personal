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
public abstract class SortingAlgorithm {
    String name;
    public SortingAlgorithm(String name){
        this.name = name;
    }
    public abstract Result sort(Integer[] list, Arrangment arrangment);
}
