/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stacks;

import java.util.ArrayList;


public class Stack<X> implements IStack<X>{
    X top;
    IStack<X> rest;
    public Stack(X a,IStack<X> r)
    {
        top = a;
        rest = r;
    }
    
    public IStack<X> push(X x) {
        //rest = new Stack<X>(top,rest);
        //top = x;
        return new Stack<X>(x,this);
    }

    public X top() throws Exception {
        try{
            return top;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public IStack<X> pop() throws Exception {
        try{
            return rest;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean isEmpty() {
        return false;
    }
    
    public int length(){
        return rest.length()+1;
    }
    
    //public X[] toArray(){return null;}
    public ArrayList<X> toArrayList(){
        ArrayList<X> temp = new ArrayList();
        IStack<X> cur = this;
        while(!cur.isEmpty())
        {
            try{
                temp.add(cur.top());
                cur = cur.pop();
            }
            catch(Exception e){System.out.println(e.getMessage());}
        }
        return temp;
    }
    
    public String toString(){
        if(rest.isEmpty())
            return top+rest.toString();
        else
            return top+","+rest.toString();
    }
}
