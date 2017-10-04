/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ostlinja
 */
public class TestCases {
    public static void main(String[] args) {
        new TestCases().run();        
    }
   
    public void run(){
        Equation eq = new Equation("-4--(x!)");
        Equation eq2 = new Equation("(1+(cosx)^2)/(cosx)^2");
        Equation eq3 = new Equation("sin(pi/x)");
        eq3.setDecimalDepth(10);
        Equation eq4 = new Equation("(csclnx*cotlnx)/(2x)");
        Equation eq5 = new Equation("238Ex");
        Equation eq5v2 = new Equation("238*10^(-x-1)");
        eq5.setDecimalDepth(10);
        eq5v2.setDecimalDepth(10);
        Equation eq6 = new Equation("abs((5-15)/2)");
        Equation eq7 = new Equation("2^x^x");
        Equation eq8 = new Equation("x+2y");
        Equation eq9 = new Equation();
        eq9.variables.add("<x>");
        eq9.setEquation("3<x>+xy");
        try{
            System.out.println(eq+" = "+eq.f(3));        
            System.out.println(eq2+" = "+eq2.f(2));
            for(int i=1;i<=6;i++)
                System.out.println(eq3+" = "+eq3.f(i));
            System.out.println(eq4+" = "+eq4.f(3));
            System.out.println(eq5+"         = "+eq5.f(-8));
            System.out.println(eq5v2+" = "+eq5v2.f(7));
            System.out.println(eq6+" = "+eq6.f(0));
            System.out.println(eq7+" = "+eq7.f(3));
            System.out.println(eq8+" = "+eq8.f(2,3));
            Map values = new HashMap();
            values.put("<x>", 2);
            values.put("x", 3);
            values.put("y", 4);
            System.out.println(eq9+" = "+eq9.f(values));
            System.out.println(eq9+" = "+eq9.f(3,4,2));
            
            Equation test = new Equation("max[2,2x,50+3,6,7,12,8]");       
            System.out.println(test+" = "+test.f(26));
            Equation test2 = new Equation("min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]");       
            System.out.println(test2+" = "+test2.f(10));
        }catch(Exception e){System.out.println(e);}
        
        
        Equation test = new Equation();
        int tests = 10000;
        long startTime = System.nanoTime();
        for(int i=0;i<tests;i++)
            test.setEquation("(csclnx*cotlnx)/(2x)");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("\nParsing Test");
        System.out.println(test+" => "+test.getParsedEquation());
        System.out.println(tests+" times in "+duration+" ms");
        
        
        try{
            double area = 0;
            double start = 0;
            double end = 10;
            double step = 0.001;
            Equation integral = new Equation("e^x");
            startTime = System.nanoTime();
            for(double i=start;i<=end;i+=step)
                area+=integral.fD(i);
            endTime = System.nanoTime();

            duration = (endTime - startTime)/1000000;
            System.out.println(String.format("\nArea of '%s' from %.1f to %.1f",integral,start,end));
            System.out.println("Area: "+area*step);
            System.out.println("Steps: "+(end-start)/step);
            System.out.println("Time: "+duration+" ms");
        }catch(Exception e){System.out.println(e);}
    }
    
}
