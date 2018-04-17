/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework6;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eriko
 */
public class ReverseOrder extends Arrangment{
    public ReverseOrder(){
        super("ReverseOrder");
    }
    public Integer[] rearrangeList(Integer[] list){
        List<Integer> l = Arrays.asList(list);
        Collections.reverse(l);
        return (Integer[])l.toArray();
    }
}
