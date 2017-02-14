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
public class Zero implements NaturalNum {
    private int value = 0;
    public Zero(){}
    
    public int getValue(){return value;}
    public boolean isZero(){return value==0;}
    public NaturalNum succ(){return new NonZero(this);}
    public NaturalNum pred()throws Exception
    {
        throw new Exception("Zero has no predecsessor");
    }
}
