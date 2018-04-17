/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework8;

import java.util.Comparator;

/**
 *
 * @author eriko
 */
public class LinkedList<V>{
    LinkedList<V> previous;
    LinkedList<V> next;
    V value;    
    Comparator comparator;
    public LinkedList(Comparator comparator){
        this.comparator = comparator;
    }
    
    public LinkedList(V value, Comparator comparator){
        this.value = value;
        this.comparator = comparator;
    }
    
    public void setPrevious(LinkedList parent){
        previous = parent;
        if(parent!=null)
            parent.next = this;
    }
    
    public LinkedList getPrevious(){
        return previous;
    }
    
    public void setNext(LinkedList child){
        next = child;
        if(child!=null)
            child.previous = this;
    }
    
    public LinkedList getNext(){
        return next;
    }
    
    public V getValue(){
        return value;
    }
    
    public void setValue(V value){
        this.value = value;
    }
    
    public boolean hasNext(){
        return next!=null;
    }
    
    public boolean hasPrevious(){
        return previous!=null;
    }
    
    public boolean hasValue(){
        return value!=null;
    }
    
    public void add(LinkedList<V> list){
            if(hasValue()){
                if(comparator.compare(value, list.value)>0){
                    V temp = value;
                    value = list.getValue();
                    list.setValue(temp);
                    list.setNext(next);
                    setNext(list);
                }
                else{
                    if(hasNext())
                        next.add(list);
                    else
                        setNext(list);
                }    
            }
            else
                value = list.getValue();
        
            
    }
    
    public void add(V value){
        add(new LinkedList(value,comparator));
    }
    
    public void insert(LinkedList<V> list, int index){
        if(index==0){
            add(list);
        }
        else{
            if(hasNext())
                next.insert(list, index-1);
            else
                setNext(list);
        }
    }
    
    public void insert(V value, int index){
        insert(new LinkedList(value,comparator),index);
    }
    
    public boolean delete(V value){
        if(this.value.equals(value)){
            if(hasPrevious())
                previous.setNext(next);
            else{
                if(hasNext()){
                    this.value = next.getValue();
                    this.next = next.getNext();
                }
                else{
                    this.value = null;
                    this.next = null;
                }
            }
            return true;
        }
        else{
            if(hasNext())
                return next.delete(value);
            else
                return false;
        }            
    }
    
    public boolean delete(int index){
        if(index==0){
            if(hasPrevious())
                previous.setNext(next);
            else{
                if(hasNext()){
                    this.value = next.getValue();
                    this.next = next.getNext();
                }
                else{
                    this.value = null;
                    this.next = null;
                }
            }
            return true;
        }
        else{
            if(hasNext())
                return next.delete(index-1);
            else
                return false;
        }      
    }
    
    public V get(int index){
        if(index==0)
            return value;
        if(hasNext())
            return next.get(index-1);
        return null;
    }
    
    public boolean contains(V value){
        if(hasValue() && this.value.equals(value))
            return true;
        if(hasNext())
            return next.contains(value);
        return false;
    }
    
    public String toString(){
        String result = hasPrevious() ? "" : "["; 
        result += hasValue() ? value.toString() : "null";
        return result += hasNext() ? ", "+next.toString() : "]";
    }
    
    public int size(){
        return 1 + (hasNext() ? next.size() : 0);
    }
    
}
