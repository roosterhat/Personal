/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dictionary;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ostlinja
 */
public class Dictionary<K,E> implements IDictionary<K,E>{

    private ArrayList<K> keys;
    private ArrayList<E> elements;
    private int size;
    
    public Dictionary()
    {
        keys = new ArrayList<K>();
        elements = new ArrayList<E>();
        size = 0;
    }
    
    public Integer size(){return size;}
    public boolean isEmpty(){return size==0;}
    public Iterator<E> elements(){return elements.iterator();}
    public Iterator<K> keys(){return keys.iterator();}
    
    public E findElement(K k)
    {
        int i = keys.indexOf(k);
        if(i>-1)
            return elements.get(i);
        else
            return null;
    }
    
    public void insert(K k,E e)
    {
        keys.add(k);
        elements.add(e);
        size++;
    }
    
    public void delete(K k)
    {
        int i = keys.indexOf(k);
        keys.remove(i);
        elements.remove(i);
        size--;
    }
    
}
