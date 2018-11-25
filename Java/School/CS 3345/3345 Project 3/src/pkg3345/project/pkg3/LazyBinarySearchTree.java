/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg3;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class LazyBinarySearchTree {
    private int size,height;
    private Node root;
    
    public LazyBinarySearchTree(){
        size = height = 0;
        root = new Node(0);
    }
    
    public boolean insert(int value) throws Exception{
        boolean inserted = true;
        validateArgument(value);
        Node current = root;
        while(current.hasValue()){
            if(current.key==value){
                inserted = false;
                break;
            }
            if(current.key < value){
                if(!current.hasRight())
                    current.addRightChild(new Node(current.height+1));
                current = current.right;
            }
            else{
                if(!current.hasLeft())
                    current.addLeftChild(new Node(current.height+1));
                current = current.left;
            }
        }
        current.deleted = false;
        current.key = value;
        size++;
        height = Math.max(height, current.height);
        return inserted;
    }
    
    public boolean delete(int value)throws Exception{
        validateArgument(value);
        Node current = root;
        while(current!=null && current.hasValue()){
            if(current.key==value){
                boolean logically = current.deleted;
                current.deleted = true;
                return !logically;
            }
            else{
                if(current.key > value)
                    current = current.left;
                else
                    current = current.right;
            }
        }
        return false;
    }
    
    public int findMax(){
        int max = -1;
        Node current = root;
        while(current!=null && current.hasValue()){
            max = Math.max(max, current.deleted?-1:current.key);
            current = current.right;
        }
        return max;
    }
    
    public int findMin(){
        int min = 100;
        Node current = root;
        while(current!=null && current.hasValue()){
            min = Math.min(min, current.deleted?100:current.key);
            current = current.left;
        }
        return min;
    }
    
    public boolean contains(int value)throws Exception{
        validateArgument(value);
        return containsHelper(value,root);
    }
    
    private boolean containsHelper(int value, Node n){
        if(n.key == value)
            return !n.deleted;
        else
            return n.hasLeft()?containsHelper(value,n.left):false || n.hasRight()?containsHelper(value,n.right):false;
    }
    
    private ArrayList inorder(Node n){
        if(n==null)
            return new ArrayList();
        ArrayList elems = inorder(n.left);
        elems.add((n.deleted?"*":"")+n.key);
        elems.addAll(inorder(n.right));
        return elems;
    }
    
    private ArrayList preorder(Node n){
        if(n==null || !n.hasValue())
            return new ArrayList();
        ArrayList elems = new ArrayList();
        elems.add((n.deleted?"*":"")+n.key);
        elems.addAll(preorder(n.left));
        elems.addAll(preorder(n.right));
        return elems;
    }
    
    private ArrayList postorder(Node n){
        if(n==null)
            return new ArrayList();
        ArrayList elems = postorder(n.left);
        elems.addAll(postorder(n.right));
        elems.add((n.deleted?"*":"")+n.key);
        return elems;
    }
    
    public String toString(){
        return preorder(root).toString();
    }
    
    public int height(){
        return height;
    }
    
    public int size(){
        return size;
    }
    
    private void validateArgument(int value)throws Exception{
        if(value < 1 || value > 99)
            throw new IllegalArgumentException("Value must be in the range [1-99]");
    }
}

class Node{
    Integer key,height;
    Node left,right;
    boolean deleted;
   
    public Node(int height){
        deleted = false;
        this.height = height;
    }
    
    public Node(int k, int height){
        key = k;
        deleted = false;
        this.height = height;
    }
    
    public void addLeftChild(Node n){
        left = n;
    }
    
    public void addRightChild(Node n){
        right = n;
    }
    
    public boolean hasValue(){
        return key!=null;
    }
    
    public boolean hasRight(){
        return right!=null;
    }
    
    public boolean hasLeft(){
        return left!=null;
    }
}
