/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;

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
        testStrings.add("sinpi");
        testStrings.add("sincospi");
        testStrings.add("sincostanpi");
        testStrings.add("sincostancscpi");
        testStrings.add("sincostancscsecpi");
        testStrings.add("(csclnx*cotlnx)/(2x)");
        for(String equation: testStrings)
            testEquation(equation,10000);
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
