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
public abstract class Arrangment{
    String name;
    public Arrangment(String name){
        this.name = name;
    }
    public abstract Integer[] rearrangeList(Integer[] list);
    
    protected void swap(Integer[] list, int index1, int index2){
        int temp = list[index1];
        list[index1] = list[index2];
        list[index2] = temp;
    }
}
