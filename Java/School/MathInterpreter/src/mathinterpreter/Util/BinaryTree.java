/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Util;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author eriko
 */
public class BinaryTree<V> {
    public BinaryTree left, right, parent;
    V value;
    Comparator<V> comparator;
    public BinaryTree(){
        this(null);
    }
    
    public BinaryTree(V value){
        this.value = value;
        this.comparator = (x,y)->x.toString().compareTo(y.toString());
    }
    
    public BinaryTree(V value, Comparator c){
        this.value = value;
        this.comparator = c;
    }
    
    public boolean hasLeft(){return left!=null;}    
    public boolean hasRight(){return right!=null;}
    public boolean hasParent(){return parent!=null;}
    public boolean hasValue(){return value!=null;}
    
    public BinaryTree getLeft(){return left;}
    public BinaryTree getRight(){return right;}
    public BinaryTree getParent(){return parent;}
    public V getValue(){return value;}
    public Comparator getComparator(){return comparator;}
    
    public void setLeft(BinaryTree b){
        if(b!=null)
            b.setParent(this);
        left = b;
    }
    public void setRight(BinaryTree b){
        if(b!=null)
            b.setParent(this);
        right = b;
    }
    public void setParent(BinaryTree b){parent = b;}
    public void setValue(V v){value = v;}
    public void setComparator(Comparator c){comparator = c;}
    
    public void add(BinaryTree<V> b){
        if(hasValue()){
            if(comparator.compare(value, b.value)>=0){
                if(hasLeft())
                    left.add(b);
                else
                    setLeft(b);
            }
            else{
                if(hasRight())
                    right.add(b);
                else
                    setRight(b);
            }
        }
        else
            this.value = b.value;
        
    }
    
    public void add(V value){
        add(new BinaryTree(value,comparator));
    }
    
    public boolean remove(V value){
        if(getValue().equals(value)){
            BinaryTree<V> b;
            if(hasRight()){
                b=right;
                for(;b.hasLeft();b=b.left);
                if(b.equals(right))
                    b.parent.setRight(null);
                else
                    b.parent.setLeft(null);
            }
            else{
                b=left;
                for(;b.hasRight();b=b.right);
                if(b.equals(left))
                    b.parent.setLeft(null);
                else
                    b.parent.setRight(null);
            }
            if(b!=null)
                setValue(b.value);
            else
                setValue(null);
            return true;
        }
        else{
            if(comparator.compare(this.value, value)>0){
                if(hasLeft()){
                    return left.remove(value);
                }
            }
            else{
                if(hasRight()){
                    return right.remove(value);
                }
            }
        }
        return false;
    }
    
    public boolean contains(V value){
        if(getValue().equals(value))
            return true;
        else{
            if(comparator.compare(this.value, value)>0)
                if(hasLeft())
                    return left.contains(value);
            else
                if(hasRight())
                    return right.contains(value);
        }
        return false;
    }
    
    public void clear(){
        setValue(null);
        setRight(null);
        setLeft(null);
        setParent(null);
    }
    
    public ArrayList<V> preorder(){
        ArrayList<V> result = new ArrayList();
        if(hasValue())
            result.add(value);
        if(hasLeft())
            result.addAll(left.preorder());
        if(hasRight())
            result.addAll(right.preorder());
        return result;
    }
    
    public ArrayList<V> inorder(){
        ArrayList<V> result = new ArrayList();
        if(hasLeft())
            result.addAll(left.inorder());
        if(hasValue())
            result.add(value);
        if(hasRight())
            result.addAll(right.inorder());
        return result;
    }
    
    public ArrayList<V> postorder(){
        ArrayList<V> result = new ArrayList();
        if(hasLeft())
            result.addAll(left.postorder());
        if(hasRight())
            result.addAll(right.postorder());
        if(hasValue())
            result.add(value);
        return result;
    }
}
