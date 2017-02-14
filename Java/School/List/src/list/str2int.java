/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package list;

/**
 *
 * @author ostlinja
 */
public class str2int implements lFun<String,Integer>{
    public Integer f(String s)
    {
        return s.length();
    }
}
