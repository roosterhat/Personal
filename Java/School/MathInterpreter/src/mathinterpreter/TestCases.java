/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import batchtester.BatchTester;
import batchtester.CustomComparator;
import batchtester.ErrorCase;
import batchtester.ScoredCase;
import batchtester.TimedCase;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ostlinja
 */
public class TestCases {
    public static void main(String[] args) {
        BatchTester bt = new BatchTester();
        createTestCases(bt);    
        bt.runTestCases();
        bt.displayReports();
    }
   
    public static void createTestCases(BatchTester bt){
        DoubleComparator c = new DoubleComparator();
        ScoredCase<Integer,Double> case1 = new ScoredCase(x->{
            Equation eq = new Equation("(x(-3-1))^2");
            return eq.fD((Integer)x);
        },new Integer[]{3,-4,5},new Double[]{144d,256d,400d});
        case1.customComparator = c;
        case1.title = "(x(-3-1))^2";
        case1.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case1);
        
        ScoredCase<Integer,Double> case2 = new ScoredCase(x->{
            Equation eq = new Equation("(1+(cosx)^2)/(cosx)^2");
            return eq.fD((Integer)x);
        },new Integer[]{10,-4,5},new Double[]{2.420371762,3.340550,13.42788170});
        case2.customComparator = c;
        case2.title = "(1+(cosx)^2)/(cosx)^2";
        case2.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case2);
        
        ScoredCase<Integer,Double> case3 = new ScoredCase(x->{
            Equation eq = new Equation("sin(pi/x)");
            return eq.fD((Integer)x);
        },new Integer[]{1,2,3,4,5,6},new Double[]{0d,1d,0.8660254,0.707106,0.587785,0.5});
        case3.customComparator = c;
        case3.title = "sin(pi/x)";
        case3.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case3);
        
        ErrorCase<Integer> case3_E = new ErrorCase(x->{
            Equation eq = new Equation("sin(pi/x)");
            return eq.fD((Integer)x);
        },new Integer[]{0},new Exception[]{new Exception("Divide by zero Error")});
        case3_E.title = "sin(pi/x)";
        bt.addTestCase(case3_E);
        
        ScoredCase<Integer,Double> case4 = new ScoredCase(x->{
            Equation eq = new Equation("(csclnx*cotlnx)/(2x)");
            return eq.fD((Integer)x);
        },new Integer[]{3,4,100},new Double[]{0.095577831,0.023730817,-0.00054126});
        case4.customComparator = c;
        case4.title = "(csclnx*cotlnx)/(2x)";
        case4.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case4);
        
        ScoredCase<Integer,Double> case5 = new ScoredCase(x->{
            Equation eq = new Equation("238Ex");
            eq.setDecimalDepth(10);
            return eq.fD((Integer)x);
        },new Integer[]{-8},new Double[]{0.00000238});
        case5.customComparator = c;
        case5.title = "238Ex";
        case5.errorMargin = 0.00000001;
        bt.addTestCase(case5);
        
        ScoredCase<Double,Double> case6 = new ScoredCase(x->{
            Equation eq = new Equation("238*10^((-2x)-1)");
            eq.setDecimalDepth(10);
            return eq.fD((Double)x);
        },new Double[]{3.5},new Double[]{0.00000238});
        case6.customComparator = c;
        case6.title = "238*10^((-2x)-1)";
        case6.errorMargin = 0.00000001;
        bt.addTestCase(case6);
        
        ScoredCase<Integer,Double> case7 = new ScoredCase(x->{
            Equation eq = new Equation("abs((5-15)/2)");
            return eq.fD((Integer)x);
        },new Integer[]{0},new Double[]{5d});
        case7.customComparator = c;
        case7.title = "abs((5-15)/2)";
        case7.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case7);
        
        Map values = new HashMap();
        values.put("<x>", 2);
        values.put("x", 3);
        values.put("y", 4);
        ScoredCase<Map,Double> case8 = new ScoredCase(x->{
            Equation eq = new Equation("3<x>+xy");
            eq.variables.add("<x>");
            eq.parseEquation();
            return Double.valueOf(eq.f((Map)x));
        },new Map[]{values},new Double[]{18d});
        case8.customComparator = c;
        case8.title = "3<x>+xy";
        case8.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case8);
        
        ScoredCase<Integer,Double> case9 = new ScoredCase(x->{
            Equation eq = new Equation("max[2,(x(-3-1))^2,50+3,6,7,12,8]");
            return eq.fD((Integer)x);
        },new Integer[]{3,0,1},new Double[]{144d,53d,53d});
        case9.customComparator = c;
        case9.title = "max[2,(x(-3-1))^2,50+3,6,7,12,8]";
        case9.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case9);
        
        ScoredCase<Integer,Double> case10 = new ScoredCase(x->{
            Equation eq = new Equation("min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]");
            return eq.fD((Integer)x);
        },new Integer[]{10,0,22},new Double[]{-20d,-12d,-44d});
        case10.customComparator = c;
        case10.title = "min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]";
        case10.errorMargin = ScoredCase.LOW_ERROR;
        bt.addTestCase(case10);
        
        TimedCase<Integer> case11 = new TimedCase(x->{
            Equation eq = new Equation();
            for(int i=0;i<10000;i++)
                eq.setEquation("(csclnx*cotlnx)/(2x)");
            return "";
        },new Integer[]{10});
        case11.title = "Parsing Test: '(csclnx*cotlnx)/(2x)'";
        bt.addTestCase(case11);
        
        ScoredCase<Double,Double> case12 = new ScoredCase(x->{
            double area = 0;
            double start = (Double)x;
            double end = 10;
            double step = 0.001;
            Equation eq = new Equation("sin(pi/x)");
            for(double i=start;i<end;i+=step)
                area+=(eq.fD(i)*step);
            return area;
        },new Double[]{0.001},new Double[]{4.9915});
        case12.customComparator = c;
        case12.title = "Integral Test: 'sin(pi/x)'";
        case12.errorMargin = ScoredCase.MEDIUM_ERROR;
        bt.addTestCase(case12);
    }
    
}

class DoubleComparator extends CustomComparator<Double>{
    @Override
    public double compare(Double d1, Double d2) {
        return 1-d1/d2;
    }
    
}
