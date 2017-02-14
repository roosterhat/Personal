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
public class NatNum {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NaturalNum zero = new Zero();
        NaturalNum one = new NonZero(zero);
        NaturalNum two = one.succ();
        NaturalNum three = new NonZero(3);
        
        System.out.println(zero.getValue());
        System.out.println(one.getValue());
        System.out.println(two.getValue());
        System.out.println(three.getValue());
        try{System.out.println(three.pred().getValue());
            System.out.println(three.pred().pred().getValue());
            System.out.println(three.pred().pred().pred().getValue());
        }catch(Exception e){System.out.println(e);}
    }
    
}
