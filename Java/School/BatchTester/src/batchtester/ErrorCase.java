/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import batchtester.Errors.FailedtoCrashException;
import batchtester.Errors.IncompatableErrorException;
import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class ErrorCase<I> extends FunctionalCase<I,Exception>{
    public ErrorCase(){
        super();
        type = "ErrorCase";
    }
    
    public ErrorCase(Test<I,Exception> test, ArrayList<I> inputs, ArrayList<Exception> outputs){
        super(test,inputs,outputs);
        type = "ErrorCase";
    }
    
    public ErrorCase(Test<I,Exception> test, I[] inputs, Exception[] outputs){
        super(test,inputs,outputs);
        type = "ErrorCase";
    }
        
    protected FunctionalReport<I, Exception> runTestCase(int index, I input, Exception expected) {
        long start = System.nanoTime();
        try{
            test.run(input);
        }catch(Exception e){
            long end = System.nanoTime();
            long duration = (end-start)/1000000;
            if(e.getClass().equals(expected.getClass()))
                return new FunctionalReport(this,index,duration,input,expected,e);
            else
                return new FunctionalReport(this,index,duration,input,expected,e,true,new IncompatableErrorException(expected, e));
        }
        long duration = (System.nanoTime()-start)/1000000;
        return new FunctionalReport(this,index,duration,input,expected,null,true,new FailedtoCrashException());
    }
    
}
