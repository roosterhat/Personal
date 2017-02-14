/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posns;

/**
 *
 * @author ostlinja
 */
public abstract class Pair<x,y> {
    protected x left;
    protected y right;
    
    public x getLeft(){return left;}
    public y getRight(){return right;}
    
    protected abstract Pair<x,y> add(x dx, y dy);
    
    public void setLeft(x a){left = a;}
    public void setRight(y a){right = a;}
    
    protected abstract void madd(x dx, y d);
}
