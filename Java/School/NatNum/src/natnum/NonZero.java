/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package natnum;

/**
 *
 * @author ostlinja
 */
public class NonZero implements NaturalNum{
    private int value;
    private NaturalNum prev;
    public NonZero(int n)
    {
        value = n;
        if(value<=1)
            prev = new Zero();
        else
            prev = new NonZero(--n);
    }
    public NonZero(NaturalNum n)
    {
        value = n.getValue()+1;
        prev = n;
    }
    public int getValue(){return value;}
    public boolean isZero(){return value==0;}
    public NaturalNum succ(){return new NonZero(this);}
    public NaturalNum pred(){return prev;}
}
