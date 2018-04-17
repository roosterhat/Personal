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
public class AlmostOrder extends Arrangment{
    double percentOutOfOrder;
    public AlmostOrder(){
        super("AlmostOrder");
        percentOutOfOrder = 0.2;
    }
    
    public AlmostOrder(double percent){
        super("AlmostOrder");
        percentOutOfOrder = percent;
    }
    
    public void setPercent(double percent){
        percentOutOfOrder = percent;
    }
    
    public Integer[] rearrangeList(Integer[] list){
        for(int i = 0;i<list.length*percentOutOfOrder;i++)
            swap(list, (int)(Math.random()*list.length), (int)(Math.random()*list.length));
        return list;
    }
}
