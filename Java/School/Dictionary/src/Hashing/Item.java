/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hashing;

/**
 *
 * @author ostlinja
 */
public class Item<K,E> {
    private K key;
    private E element;
    public Item(K k,E e)
    {
        key = k;
        element = e;
    }
    
    public K getKey(){return key;}
    public E getElement(){return element;}
}
