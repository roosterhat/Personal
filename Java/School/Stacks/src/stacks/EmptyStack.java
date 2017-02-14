/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stacks;

import java.util.ArrayList;

public class EmptyStack<X> implements IStack<X> {

    public IStack<X> push(X x) {
        return new Stack<X>(x,this);
    }

    public X top() throws Exception {
       throw new Exception("This stack is empty");
    }

    public IStack<X> pop() throws Exception {
        throw new Exception("This stack is empty");
    }

    public boolean isEmpty() {
        return true;
    }
    
    public int length(){
        return 0;
    }
    
    public String toString(){
        return "";
    }
    
    public ArrayList<X> toArrayList(){
        return null;
    }
}
