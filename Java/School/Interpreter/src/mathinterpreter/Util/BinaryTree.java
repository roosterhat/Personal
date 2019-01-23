/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 *
 * @author eriko
 */
public class BinaryTree<E> implements Collection<E>{
    public static Comparator DEFAULT_COMPARATOR = (x,y)->x.toString().compareTo(y.toString());
    private BinaryTree left, right, parent;
    private E value;
    private Comparator<E> comparator;
    private int size;
    
    public BinaryTree(){
        this.value = null;
        this.comparator = DEFAULT_COMPARATOR;
    }
    
    public BinaryTree(E value){
        setValue(value);
        this.comparator = DEFAULT_COMPARATOR;
    }
    
    public BinaryTree(Comparator c){
        this.value = null;
        this.comparator = c;
    }
    
    public BinaryTree(E value, Comparator c){
        setValue(value);
        this.comparator = c;
    }
    
    public boolean hasLeft(){return left!=null;}    
    public boolean hasRight(){return right!=null;}
    public boolean hasParent(){return parent!=null;}
    public boolean hasValue(){return value!=null;}
    
    public BinaryTree getLeft(){return left;}
    public BinaryTree getRight(){return right;}
    public BinaryTree getParent(){return parent;}
    public E getValue(){return value;}
    public Comparator getComparator(){return comparator;}
    
    public boolean setLeft(BinaryTree b){
        if(b!=null)
            b.setParent(this);
        left = b;
        size++;
        return true;
    }
    public boolean setRight(BinaryTree b){
        if(b!=null)
            b.setParent(this);
        right = b;
        size++;
        return true;
    }
    public void setParent(BinaryTree b){parent = b;}
    public void setValue(E v){value = v;size++;}
    public void setComparator(Comparator c){comparator = c;}
    
    public boolean add(BinaryTree<E> b){
        if(hasValue()){
            if(comparator.compare(value, b.value)>=0){
                if(hasLeft())
                    return left.add(b);
                else
                    return setLeft(b);
            }
            else{
                if(hasRight())
                    return right.add(b);
                else
                    return setRight(b);
            }
        }
        else{
            this.value = b.value;
            size++;
            return true;
        }
        
    }
    
    public boolean add(E value){
        return add(new BinaryTree(value,comparator));
    }
    
    public boolean remove(Object value){
        if(hasValue() && getValue().equals(value)){
            BinaryTree<E> b = null;
            if(hasLeft())
                for(b=left;b.hasRight();b=b.right);
            else if(hasRight())
                for(b=right;b.hasLeft();b=b.left);
            if(b!=null){
                setValue(b.value);
                b.remove(b.value);
            }
            else
                setValue(null);
            size--;
            
            return true;
        }
        else{
            if(hasValue()){
                if(comparator.compare(this.value, (E)value)>0){
                    if(hasLeft())
                        return left.remove(value);
                }
                else{
                    if(hasRight())
                        return right.remove(value);
                }
            }
            else
                return false;
        }
        return false;
    }
    
    public boolean contains(Object value){
        if(hasValue()){
            if(getValue().equals(value))
                return true;
            else{
                if(comparator.compare(this.value, (E)value)>0)
                    if(hasLeft())
                        return left.contains(value);
                else
                    if(hasRight())
                        return right.contains(value);
            }
        }
        return false;
    }
    
    public void clear(){
        setValue(null);
        setRight(null);
        setLeft(null);
        setParent(null);
        size = 0;
    }
    
    public ArrayList<E> preorder(){
        ArrayList<E> result = new ArrayList(size);
        if(hasValue())
            result.add(value);
        if(hasLeft())
            result.addAll(left.preorder());
        if(hasRight())
            result.addAll(right.preorder());
        return result;
    }
    
    public ArrayList<E> inorder(){
        ArrayList<E> result = new ArrayList(size);
        if(hasLeft())
            result.addAll(left.inorder());
        if(hasValue())
            result.add(value);
        if(hasRight())
            result.addAll(right.inorder());
        return result;
    }
    
    public ArrayList<E> postorder(){
        ArrayList<E> result = new ArrayList(size);
        if(hasLeft())
            result.addAll(left.postorder());
        if(hasRight())
            result.addAll(right.postorder());
        if(hasValue())
            result.add(value);
        return result;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public Iterator iterator() {
        return inorder().iterator();
    }

    @Override
    public boolean addAll(Collection c) {
        for(Object o: c)
            if(!add((E)o))
                return false;
        return true;       
    }

    @Override
    public void forEach(Consumer action) {
        for(E e: preorder())
            action.accept(e);
    }

    @Override
    public boolean containsAll(Collection c) {
        for(Object o: c)
            if(!contains((E)c))
                return false;
        return true;
    }

    @Override
    public boolean removeAll(Collection c) {
        boolean state = true;
        for(Object o: c)
            if(!remove((E)o))
                state = false;
        return state;
        
    }

    @Override
    public boolean retainAll(Collection c) {
        ArrayList rem = inorder();
        rem.removeAll(c);
        return removeAll(rem);
    }

    @Override
    public Object[] toArray() {
        return inorder().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return inorder().toArray(a);
    }
    
    public String toString(){
        return inorder().toString();
    }
}
