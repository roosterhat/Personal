/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author ostlinja
 */
public class MathInterpreter {

    
    public static void main(String[] args) {
        Equation eq = new Equation("x^-1");
        Equation eq2 = new Equation("(1+(cosx)^2)/(cosx)^2");
        Equation eq3 = new Equation("sin(pi/x)");
        Equation eq4 = new Equation("(csc(lnx)*cot(lnx))/(2x)");
        System.out.println(eq.f(4));
        System.out.println(eq2.f(2));
        for(int i=1;i<=6;i++)
            System.out.println(eq3.f(i));
        System.out.println(eq4.f(3));
    }
    
    public MathInterpreter(){
       
    }
    
    
}
