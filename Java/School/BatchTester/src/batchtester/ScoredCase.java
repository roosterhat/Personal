/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import batchtester.Errors.ErrorMarginTooLargeException;
import java.util.ArrayList;

/**
 *
 * @author eriko
 * @param <I> input type
 * @param <O> output type (must extend Comparable)
 */
public class ScoredCase<I,O extends Comparable> extends FunctionalCase<I,O>{
    public final static double LOW_ERROR = 0.00001;
    public final static double MEDIUM_ERROR = 0.001;
    public final static double HIGH_ERROR = 1;
    public double errorMargin = MEDIUM_ERROR;
    public CustomComparator<O> customComparator;
    
    public ScoredCase(){
        super();
        type = "ScoredCase";
    }
    
    public ScoredCase(Test<I,O> test, ArrayList<I> inputs, ArrayList<O> outputs){
        super(test,inputs,outputs);
        type = "ScoredCase";
    }
    
    public ScoredCase(Test<I,O> test, I[] inputs, O[] outputs){
        super(test,inputs,outputs);
        type = "ScoredCase";
    }
    
    protected FunctionalReport<I,O> runTestCase(int index, I input, O expected){
        long start = System.nanoTime();
        try{
            O output = ((Test<I,O>)test).run(input);
            long end = System.nanoTime();
            long duration = (end-start)/1000000;
            double score;
            if(customComparator==null)
                score = Math.abs(output.compareTo(expected));
            else
                score = customComparator.compare(output, expected);
            if(score>errorMargin)
                return new ScoredReport(this,index,duration,input,expected,output,score,errorMargin, true,new ErrorMarginTooLargeException(errorMargin,score));
            else
                return new ScoredReport(this,index,duration,input,expected,output,score,errorMargin);
        }catch(Exception e){
            return new ScoredReport(this,index,(System.nanoTime()-start)/1000000,input,expected,null,-1,errorMargin,true,e);
        }
    }
}
