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
public class Posn extends Pair<Integer,Integer> implements Iposn {
    private Integer x,y;
    public Posn(Integer a, Integer b)
    {
        x=a;
        y=b;
    }
    
    protected Posn add(Integer dx, Integer dy)
    {
        return new Posn(left+dx,right+dy);
    }
    
    protected void madd(Integer dx,Integer dy)
    {
        setX(left+dx);
        setY(right+dy);
    }
    
    public Posn move(Integer dx,Integer dy)
    {
        return add(dx,dy);
    }
       
    public Integer getX(){return getLeft();}
    public Integer getY(){return getRight();}
    
    public void setX(Integer a){setLeft(a);}
    public void setY(Integer b){setRight(b);}
    
    public void mmove(Integer dx,Integer dy)
    {
        madd(dx,dy);
    }
}
