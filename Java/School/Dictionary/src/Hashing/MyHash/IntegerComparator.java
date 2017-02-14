/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hashing.MyHash;

import Hashing.HashComparator;

/**
 *
 * @author ostlinja
 */
public class IntegerComparator implements HashComparator<Integer> {
    public IntegerComparator(){}
    public int hashIndex(Integer i)
    {
        return i;
    }
    
    public boolean keyEqual(Integer k1, Integer k2)
    {
        return k1.equals(k2);
    }
}
