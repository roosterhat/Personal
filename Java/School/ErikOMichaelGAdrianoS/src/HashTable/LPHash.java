/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HashTable;

import dictionary.IDictionary;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ostlinja
 */

//LPHash, a class that uses linear probing algorithms
public class LPHash<K,E> extends NHash<K,E> {
    
    public LPHash(int s, HashComparator<K> hc)
    {
        super(s,hc);
    }
    
}
