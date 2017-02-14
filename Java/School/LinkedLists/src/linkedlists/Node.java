/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedlists;

/**
 *
 * @author ostlinja
 */
public class Node<K> {
    K value;
    Node next;
    public Node(K v)
    {
        value = v;
        next = null;
    }
    public Node(K v, Node n)
    {
        value = v;
        next = n;
    }
    public K getValue(){return value;}
    public Node getNext(){return next;}
    public void setValue(K v){value = v;}
    public void setNext(Node n){next = n;}
    public boolean hasNext(){return next!=null;}//returns a boolean whether or not 'next' == null
    public int getSize()//recursivly calls getSize until Node does not have a 'next' otherwise it adds 1 to the total size
    {
        if(hasNext())
            return next.getSize()+1;
        else
            return 1;
    }
    
    public void insert(Node n)
    {
        if(hasNext())
            if((int)next.getValue()>=(int)n.getValue())
            {
                n.setNext(next);
                setNext(n);
            }
            else
                next.insert(n);
        else
            next = n;
    }
    
    public void insert(Node n, int index)
    {
        if(index==0)
        {
            if(hasNext())
                n.setNext(next);
            next = n;                
        }
        else
            next.insert(n,--index);
    }
    
    public void append(Node n)//recursivly calls append until the current node does not have a 'next' then sets the new node to 'next'
    {
        if(hasNext())
            next.append(n);
        else
            next = n;
    }
    public void print()//recursivly calls print while the current node has a next node
    {
        System.out.print(value+" ");
        if(hasNext())
            next.print();
    }
    public String toString()//recursivly calls toString until the current node has no 'next' then returns an empty string
    {
        if(hasNext())
            return value+", "+next.toString();
        else
            return value+"";
    }
}
