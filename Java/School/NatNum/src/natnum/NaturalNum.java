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
public interface NaturalNum {
    
    public int getValue();
    public boolean isZero();
    public NaturalNum succ();
    public NaturalNum pred() throws Exception;
}
