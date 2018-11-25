/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg4;

import java.util.ArrayList;

/**
 *
 * @author eriko
 * @param <E> Node element type
 */
public class RedBlackTree<E extends Comparable>{
    static final boolean RED = false;
    static final boolean BLACK = true;
    Node<E> root;

    public RedBlackTree(){
        root = new Node(null);
        root.color = BLACK;
    }
    
    public boolean insert(E element) throws NullPointerException{
        if(element==null)
            throw new NullPointerException("Given element is null");
        Node<E> current = root;
        while(current!=null && current.hasElement()){
            if(current.element.compareTo(element)==0)
                return false;
            if(current.element.compareTo(element)>0)
                current = current.hasLeft() ? current.left : current.addLeft(new Node(null));
            else
                current = current.hasRight() ? current.right : current.addRight(new Node(null));
        }
        current.element = element;
        rebalance(findImbalance(root));
        return true;
    }
    
    public boolean contains(E element){
        if(element!=null){
            Node<E> current = root;
            while(current!=null && current.hasElement()){
                if(current.element.compareTo(element)==0)
                    return true;
                current = current.element.compareTo(element)>0?current.left:current.right;
            }
        }
        return false;
    }
    
    private Node findImbalance(Node n){
        if(n==null || !(n.hasLeft() || n.hasRight()))
            return null;
        if(n.color==RED){
            if(n.hasLeft() && n.left.color==RED)
                return n.left;
            if(n.hasRight() && n.right.color==RED)
                return n.right;
        }
        Node left = findImbalance(n.left);
        Node right = null;
        if(left==null)
            right = findImbalance(n.right);
        return right==null?left:right;
    }
    
    private void rebalance(Node n){
        if(n==null)
            return;
        Node grand = n.parent.parent;
        Node uncle = n.getUncle();
        if(uncle==null || uncle.color == BLACK){
            if(n.parent.parent.element.compareTo(n.element)>0){
                if(n.parent.element.compareTo(n.element)>0)
                    grand = rightRotate(grand);
                else
                    grand = leftRightRotate(grand);
            }
            else{
                if(n.parent.element.compareTo(n.element)<0)
                    grand = leftRotate(grand);
                else
                    grand = rightLeftRotate(grand);
            }
            flipBlack(grand);
        }
        else{
            flipRed(grand);
        }
        rebalance(findImbalance(root));
    }
    
    private Node rightRotate(Node root) {
        Node pivot = root.left;
        Node par = root.parent;
        root.addLeft(pivot.right);
        pivot.addRight(root);
        if(root==this.root)
            this.root = pivot;
        else{
            if(par.element.compareTo(root.element)>0)
                par.addLeft(pivot);
            else
                par.addRight(pivot);
        }
        return pivot;
    }
     
    private Node leftRotate(Node root) {
        Node pivot = root.right;
        Node par = root.parent;
        root.addRight(pivot.left);
        pivot.addLeft(root);
        if(root==this.root)
            this.root = pivot;
        else{
            if(par.element.compareTo(root.element)>0)
                par.addLeft(pivot);
            else
                par.addRight(pivot);
        }
        return pivot;
    }
    
    private Node rightLeftRotate(Node n){
        if(n.hasRight()){
            rightRotate(n.right);
            return leftRotate(n);
        }
        return n;
    }
    
    private Node leftRightRotate(Node n){
        if(n.hasLeft()){
            leftRotate(n.left);
            return rightRotate(n);
        }
        return n;
    }
    
    private void flipBlack(Node n){
        n.color = BLACK;
        if(n.hasLeft())
            n.left.color = RED;
        if(n.hasRight())
            n.right.color = RED;
    }
    
    private void flipRed(Node n) {
        n.color = n==root?BLACK:RED;
        if(n.hasLeft())
            n.left.color = BLACK;
        if(n.hasRight())
            n.right.color = BLACK;
    }
    
    private ArrayList preorder(Node n){
        if(n==null || !n.hasElement())
            return new ArrayList();
        ArrayList elems = new ArrayList();
        elems.add(n.toString());
        elems.addAll(preorder(n.left));
        elems.addAll(preorder(n.right));
        return elems;
    }
    
    private ArrayList postorder(Node n){
        if(n==null || !n.hasElement())
            return new ArrayList();
        ArrayList elems = postorder(n.left);
        elems.addAll(postorder(n.right));
        elems.add(n.toString());
        return elems;
    } 
    
    private ArrayList inorder(Node<E> n){
        if(n==null || !n.hasElement())
            return new ArrayList();
        ArrayList res = inorder(n.left);
        res.add(n.toString());
        res.addAll(inorder(n.right));
        return res;
    }
    
    public String toString(){
        String res = "";
        for(String s: (ArrayList<String>)preorder(root))
            res+=(res.isEmpty()?"":" ")+s;
        return res;
    }
    
}

class Node<E extends Comparable>{
    E element;
    Node left,right,parent;
    boolean color;

    public Node(E element){
        this.element = element;
        color = RedBlackTree.RED;
    }
    
    public Node getUncle(){
        if(hasParent() && parent.hasParent())
            return parent.parent.left==parent ? parent.parent.right : parent.parent.left;
        return null;
    }
    
    public boolean hasElement(){
        return element!=null;
    }
    
    public boolean hasLeft(){
        return left!=null;
    }
    
    public boolean hasRight(){
        return right!=null;
    }
    
    public boolean hasParent(){
        return parent!=null;
    }
    
    public Node addLeft(Node<E> n){
        left = n;
        if(n!=null)
            n.parent = this;
        return left;
    }
    
    public Node addRight(Node<E> n){
        right = n;
        if(n!=null)
            n.parent = this;
        return right;
    }
    
    public String toString(){
        return (color?"":"*")+element;
    }
    
}
