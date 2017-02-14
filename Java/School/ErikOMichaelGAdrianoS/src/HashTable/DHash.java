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

//Double Hash class which extends LPHash but has a different hashing method
public class DHash<K,E> extends NHash<K,E> {
        
    //constructor, calls the super class to construct the object
    public DHash(int s, HashComparator<K> hc)
    {
        super(s,hc);
    }
    
    //overrides the find method of the LPHash class to implement double hashing
    //invariance: when found==true and done==true
    private int find(K k)
    {
        int ph = this.h.hashIndex(k)%size;
        int j = 1;
        int i = 0;
        int res = -1;
        boolean found = false;
        boolean done = false;
        while(!found && !done)
        {
            if(h.keyEqual(key(i),k))
            {
                res = i;
                found = true;
            }
            else if(empty(i)||available(i))
                done = true;
            i = Math.abs(ph + j*doubleHash(j))%size;
            j++;
        }
        return res;
    }
    
    //doubleHash, creates a new hash given a number
    private int doubleHash(int i)
    {
        int a = 23;
        //int a = (int)((new Random(i).nextDouble())*i);
        //System.out.println(i+","+a);
        return a*i;
                
    }
    
    //insert, overrides the insert method for LPHash 
    //invariance: when done==true and the hash table is not full
    public void insert(K k,E e)
    {
        int ph = h.hashIndex(k)%size;
        int i = 0;
        int j = 1;
        boolean done = false;
        while (!done && !full())
        {
            if(empty(i)||available(i))
            {
                A.set(i,new Item<K,E>(k,e));
                n++;
                done = true;
            }
            else
            {
                collisions++;
            }
            
            i = Math.abs(ph + j * doubleHash(j))%size;
            j++;
        }
    }
    
    //overrides LPHash method
    public E findElement(K k)
    {
        int i = find(k);
        if(i<0)
            return null;
        else
            return element(i);
    }
    //overrides LPHash method
    public void delete(K k)
    {
        int i = find(k);
        if(i>-1)
        {
            A.set(i,AVAILABLE);
            n--;
        }
    }
    
    public boolean contains(K k){return find(k)>-1;}
    
}
