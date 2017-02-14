/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dictionary;

import java.util.Iterator;

/**
 *
 * @author ostlinja
 */
public interface IDictionary<K,E> {

    public Integer size();
    public boolean isEmpty();
    public Iterator<E> elements();
    public Iterator<K> keys();
    public E findElement(K k);
    public void insert(K k,E e);
    public void delete(K k);
    
    
}
