/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hashing.MyHash;

import Hashing.HashComparator;
import Hashing.Item;
import dictionary.IDictionary;
import java.util.ArrayList;
import java.util.Iterator;

public class CHash<K,E> implements IDictionary<K,E> {
    private Item<K,E> AVAILABLE = new Item<K,E>(null,null);
    private int n;
    private int size;
    private ArrayList<Item<K,E>> A;
    private HashComparator<K> h;
    
    
    public CHash(int s, HashComparator<K> hc)
    {
        n = 0;
        size = s;
        h = hc;
        A = new ArrayList<Item<K,E>>(s);
        for(int i=0;i<s;i++)
        {
            A.add(i,null);
        }
    }
    
    private boolean available(int i)
    { return A.get(i)==AVAILABLE;}
    
    private boolean empty(int i)
    {return A.get(i)==null;}
    
    private K key(int i)
    {return A.get(i).getKey();}
    
    private E element(int i)
    {return A.get(i).getElement();}
    
    private int find(K k)
    {
        int i = h.hashIndex(k)%size;
        int j = i;
        int res = -1;
        boolean found = false;
        boolean done = false;
        while(!found && !done)
        {
            if(h.keyEqual(key(j),k))
            {
                res = j;
                found = true;
            }
            j = (j+1)%size;
            if(j==i)
                done = true;
        }
        return res;
    }
    
    public Integer size(){return n;}
    
    public boolean isEmpty(){return n==0;}
    
    public Iterator<E> elements()
    {
        Iterator<Item<K,E>> htLooper = A.iterator();
        ArrayList<E> elems = new ArrayList<E>();
        Item<K,E> k;
        while(htLooper.hasNext())
        {
            k = htLooper.next();
            if((k!=null)&&(k!=AVAILABLE))
            {
                elems.add(k.getElement());
            }
        }
        return elems.iterator();
    }
    
    public Iterator<K> keys()
    {
        Iterator<Item<K,E>> htLooper = A.iterator();
        ArrayList<K> keys = new ArrayList<K>();
        Item<K,E> k;
        while(htLooper.hasNext())
        {
            k = htLooper.next();
            if((k!=null)&&(k!=AVAILABLE))
            {
                keys.add(k.getKey());
            }
        }
        return keys.iterator();
    }
    
    public E findElement(K k)
    {
        int i = find(k);
        if(i<0)
            return null;
        else
            return element(i);
    }
    
    public void insert(K k,E e)
    {
        int i = h.hashIndex(k)%size;
        int j = i;
        boolean done = false;
        while (!done)
        {
            if(empty(j)||available(j))
            {
                A.set(j,new Item<K,E>(k,e));
                n++;
                done = true;
            }
            j = (j+1)%size;
        }
    }
    
    public void delete(K k)
    {
        int i = find(k);
        if(i>-1)
        {
            A.set(i,AVAILABLE);
            n--;
        }
    }
    
    public void resize(int s)
    {
        size = s; 
    }
}
