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
public abstract class NHash<K,E> implements IDictionary<K,E> {
    protected Item<K,E> AVAILABLE = new Item<K,E>(null,null);
    protected int n;
    protected int collisions;
    protected int size;
    protected ArrayList<Item<K,E>> A;
    protected HashComparator<K> h;
    public NHash(int s, HashComparator hc)
    {
        n = 0;
        collisions = 0;
        size = s;
        h = hc;
        A = new ArrayList<Item<K,E>>(s);
        for(int i=0;i<s;i++)
        {
            A.add(i,null);
        }
    }
    //checks to see if the given index 'i' is equal to the constant AVAILABLE 
    protected boolean available(int i)
    { return A.get(i)==AVAILABLE;}
    
    //checks to see if the given index 'i' is equal to null
    protected boolean empty(int i)
    {return A.get(i)==null;}
    
    //returns the key for a given index 'i'
    protected K key(int i)
    {
        if(!empty(i))
            return A.get(i).getKey();
        return null;
    }
    
    //returns the element for a given index 'i'
    protected E element(int i)
    {
        if(!empty(i))
            return A.get(i).getElement();
        return null;
    }
    
    //finds the index of the item that contains the given key 'k'
    //invariance: when found and done == true
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
    
    //returns the number of elements in the hash table 
    public Integer size(){return n;}
    
    //returns true or false if the hash table is empty 
    public boolean isEmpty(){return n==0;}
    
    //returns an Iterator for all the elements in the hash table
    //Invariance: while the iterator has another element
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
    
    //returns an Iterator for all the key in the hash table
    //Invariance: while the Iterator has another element
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
    
    //returns the element with the given key 'k'
    public E findElement(K k)
    {
        int i = find(k);
        if(i<0)
            return null;
        else
            return element(i);
    }
    
    //inserts an Item containing key 'k' and element 'e' into the an available spot in the hash table
    //based on a linear probing algorithm
    //Invariance: until done==true
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
            else
            {
                collisions++;
            }
            j = (j+1)%size;
        }
    }
    
    //delete the Item from the hashtable that contains the given key 'k'
    public void delete(K k)
    {
        int i = find(k);
        if(i>-1)
        {
            A.set(i,AVAILABLE);
            n--;
        }
    }
    
    //changes the total size of the hash table to size 's' 
    public void resize(int s)
    {
        size = s; 
    }
    
    //returns the number of collisions detected so far
    public int getCollisions()
    {
        return collisions;
    }
    
    //returns if the hash table is full
    public boolean full(){return size==n;}
    
    public boolean contains(K k){return find(k)>-1;}
}
