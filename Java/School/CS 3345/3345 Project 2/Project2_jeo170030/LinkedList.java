/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg33445.project.pkg2;

/**
 *
 * @author eriko
 * @param <Type>
 */
public class LinkedList<Type extends IDedObject>{
    private int size;
    private Node head,tail;
    public LinkedList(){
        head = new Node(null);
        tail = new Node(null);
        makeEmpty();
    }
    
    public void makeEmpty(){                    //Clears the list
        head.setNext(tail);
        size = 0;
    }
    
    public Type findID(int id){                 //returns the value with the matching id
        Node<Type> n = head.next;
        for(;n.hasNext();n=n.getNext())
            if(n.getValue().getID() == id)
                return n.getValue();
        return null;
    }
    
    public boolean insertAtFront(Type value){   //prepends a value to the list
        if(findID(value.getID())==null){
            Node<Type> n = new Node(value);
            n.setNext(head.next);
            head.setNext(n);
            size++;
            return true;
        }
        return false;
    }
    
    public Type deleteFromFront(){              //deletes the first element of the list
        Node<Type> n = head.next;
        if(size>0)
            head.setNext(head.getNext().getNext());
        return n.getValue();
    }
    
    public Type delete(int id){                 //deletes the value with the matching id
        Node<Type> n = head;
        for(;n.hasNext();n=n.getNext())
            if(n.getNext().getValue() != null)
                if(((Type)n.getNext().getValue()).getID() == id){
                    Node<Type> tmp = n.getNext();
                    n.setNext(n.getNext().getNext());
                    return tmp.getValue();
                }
        return n.getValue();
    }
    
    public String toString(){                   //returns a string representation of the list
        String res = "[";
        Node<Type> n = head.next;
        for(;n.hasNext();n=n.getNext())
            res += n.getValue().getID() + (n.getNext().getValue()==null ? "" : ",");
        res+="]";
        return res;
    }
    
    public void printAll(){                     //prints all values in the list
        System.out.println(this);
    }

}

class Node<T>{
    Node next;
    T value;
    public Node(T value){
        this.value = value;
    }
    
    public void setNext(Node n){
        next = n;
    }
    
    public Node getNext(){
        return next;
    }
    
    public T getValue(){
        return value;
    }
    
    public boolean hasNext(){
        return next!=null;
    }
}
