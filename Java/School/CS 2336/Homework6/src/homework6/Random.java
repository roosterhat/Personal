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
public class Random extends Arrangment{
    public Random(){
        super("Random");
    }
    
    public Integer[] rearrangeList(Integer[] list){
        for(int i = 0;i<list.length;i++)
            swap(list,i,(int)(Math.random()*list.length));
        return list;
    }
}
