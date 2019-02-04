/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author eriko
 * @param <I> Input parameters
 * @param <O> Output parameters
 */
public abstract class FunctionalCase<I,O> extends TestCase<I,O>{
    public ArrayList<I> inputs;
    public ArrayList<O> expectedOutputs;
    public OutputStream output;
    
    public FunctionalCase(){
        super();
        inputs = new ArrayList();
        expectedOutputs = new ArrayList();
        output = System.out;
        checkInputOutput(inputs,expectedOutputs);
        type = "FunctionalCase";
    }
    
    public FunctionalCase(Test<I,O> test, ArrayList<I> inputs, ArrayList<O> outputs){
        super(test);
        this.inputs = inputs;
        this.expectedOutputs = outputs;
        output = System.out;
        checkInputOutput(inputs,expectedOutputs);
        type = "FunctionalCase";
    }
    
    public FunctionalCase(Test<I,O> test, I[] inputs, O[] outputs){
        this(test, new ArrayList(Arrays.asList(inputs)),new ArrayList(Arrays.asList(outputs)));
    }
    
    private boolean checkInputOutput(ArrayList<I> in, ArrayList<O> out)throws IllegalArgumentException{
        if(in.size()==out.size())
            return true;
        throw new IllegalArgumentException("Input and Output arguments are of different sizes");
    }
       
    public void outputReports(ArrayList<FunctionalReport<I,O>> reports){
        for(TestReport report: reports)
            try{
                output.write(report.toString().getBytes());
            }catch(IOException e){
                String errorMessage = e.getMessage()+"\n";
                for(StackTraceElement elem: e.getStackTrace())
                    errorMessage += elem.toString()+"\n";
                System.out.println(errorMessage);
            }
    }
    
    public ArrayList<? extends TestReport> runTestCases(){
        ArrayList<FunctionalReport<I,O>> reports = new ArrayList();
        for(int i = 0; i<inputs.size(); i++)
            reports.add(runTestCase(i,inputs.get(i),expectedOutputs.get(i)));
        return reports;
    }
    
    protected abstract FunctionalReport<I,O> runTestCase(int index, I input, O expected);
    
}
