/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HashTable;

/**
 *
 * @author ostlinja
 */

//holds a key and an element for a hashtable
public class Item<K,E> {
    private K key;
    private E element;
    public Item(K k,E e)
    {
        key = k;
        element = e;
    }
    
    //returns the key 
    public K getKey(){return key;}
    //returns the item
    public E getElement(){return element;}
}
