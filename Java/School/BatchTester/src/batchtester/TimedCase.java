/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author eriko
 */
public class TimedCase<I> extends TestCase<I,Object>{
    ArrayList<I> inputs;
    public TimedCase(Test<I,Object> test,ArrayList<I> inputs){
        super(test);
        this.inputs = inputs;
        type = "TimedCase";
    }
    
    public TimedCase(Test<I,Object> test, I[] inputs){
        this(test,new ArrayList(Arrays.asList(inputs)));
    }
    
    protected TestReport runTestCase(int index, I input){
        Exception exception = null;
        boolean failed = false;
        long start = System.nanoTime();
        long duration;
        try{
            test.run(input);
        }
        catch(Exception e){
            failed = true;
            exception = e;
        }
        finally{
            long end = System.nanoTime();
            duration = (end-start)/1000000;
        }
        return failed ? new TestReport(this,index,duration,failed,exception) : new TestReport(this,index,duration);
    }
    
    public ArrayList<? extends TestReport> runTestCases() {
        ArrayList<TestReport> reports = new ArrayList();
        for(int i = 0; i<inputs.size(); i++)
            reports.add(runTestCase(i,inputs.get(i)));
        return reports;
    }
    
}
