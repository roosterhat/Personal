/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Parser;

import batchtester.BatchTester;
import batchtester.TimedCase;
import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Equation;

/**
 *
 * @author ostlinja
 */
public class ParsingTests {
    public static void main(String[] args) {
        StringParser p = new StringParser();
        p.tokens = new ArrayList(Arrays.asList(new String[]{"summation","i"}));
        p.parseString("summation[0,0,i]");
        BatchTester bt = new BatchTester();
        createTestCases(bt);    
        for(int i = 0; i < 5; i++){
            bt.runTestCases();
            System.out.println(bt.getBatchReports().get(i));
        }
    }
    
    public static void createTestCases(BatchTester bt){
        TimedCase<Integer> case1 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("-4--(x!)");
            return "";
        },new Integer[]{10000});
        case1.title = "Parsing Test: '-4--(x!)'";
        bt.addTestCase(case1);
        
        TimedCase<Integer> case2 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("(1+(cosx)^2)/(cosx)^2");
            return "";
        },new Integer[]{100,1000,10000});
        case2.title = "Parsing Test: '(1+(cosx)^2)/(cosx)^2'";
        bt.addTestCase(case2);
        
        TimedCase<Integer> case3 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("sin(pi/x)");
            return "";
        },new Integer[]{10000});
        case3.title = "Parsing Test: 'sin(pi/x)'";
        bt.addTestCase(case3);
        
        TimedCase<Integer> case4 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("238*10^(-x-1)");
            return "";
        },new Integer[]{10000});
        case4.title = "Parsing Test: '238*10^(-x-1)'";
        bt.addTestCase(case4);
        
        TimedCase<Integer> case5 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]");
            return "";
        },new Integer[]{10000});
        case5.title = "Parsing Test: 'min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]'";
        bt.addTestCase(case5);
        
        TimedCase<Integer> case6 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<(int)x;i++)
                eq.setEquation("(csclnx*cotlnx)/(2x)");
            return "";
        },new Integer[]{10000});
        case6.title = "Parsing Test: '(csclnx*cotlnx)/(2x)'";
        bt.addTestCase(case6);      
    }
}
