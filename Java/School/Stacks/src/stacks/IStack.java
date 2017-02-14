/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stacks;

import java.util.ArrayList;

public interface IStack<X> {
    public IStack<X> push(X x);
    public X top()throws Exception;
    public IStack<X> pop()throws Exception;
    public boolean isEmpty();
    public String toString();
    public int length();
    public ArrayList<X> toArrayList();
}
