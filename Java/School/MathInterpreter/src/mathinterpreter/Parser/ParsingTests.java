/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Parser;

import java.util.ArrayList;
import mathinterpreter.Equation;

/**
 *
 * @author ostlinja
 */
public class ParsingTests {
    public static void main(String[] args) {
        new ParsingTests().run();        
    }
    
    public void run(){
        ArrayList<String> testStrings = new ArrayList();
        testStrings.add("-4--(x!)");
        testStrings.add("(1+(cosx)^2)/(cosx)^2");
        testStrings.add("sin(pi/x)");
        testStrings.add("238*10^(-x-1)");
        testStrings.add("min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]");
        testStrings.add("(csclnx*cotlnx)/(2x)");
        
        testStrings.forEach(x->testEquation(x,10000));
    }
    
    public void testEquation(String equation, int amount){
        int trials = 10;
        Equation testEquation = new Equation();
        ArrayList<Long> times = new ArrayList();
        System.out.println("Parsing '"+equation+"' "+amount+" times");
        System.out.println("Trial: Duration (ms)");
        for(int rep = 0;rep<trials;rep++){
            System.out.print(rep+"    : ");
            long startTime = System.nanoTime();
            for(int i=0;i<amount;i++)
                testEquation.setEquation(equation);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000000;
            times.add(duration);
            System.out.println(duration);
        }
        double total = 0.0;
        for(long time: times)
            total+=time;
        System.out.println("Average: "+total/trials+"\n");
    }
}
