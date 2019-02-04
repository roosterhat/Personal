/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

/**
 *
 * @author eriko
 */
public class FunctionalReport<I,O> extends TestReport {
    public I input;
    public O expectedOutput;
    public O actualOutput;
    
    public FunctionalReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual){
        super(testcase, number, duration);
        this.input = input;
        this.expectedOutput = expected;
        this.actualOutput = actual;
    }
    
    public FunctionalReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual, boolean failed){
        super(testcase, number, duration, failed);
        this.input = input;
        this.expectedOutput = expected;
        this.actualOutput = actual;
    }
    
    public FunctionalReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual, boolean failed, Exception exception){
        super(testcase, number, duration, failed, exception);
        this.input = input;
        this.expectedOutput = expected;
        this.actualOutput = actual;
    }
    
    protected void setOutputFormat(){
        outputFormat = "#%d (%s) duration: %d ms (Input: %s Expected: %s Actual: %s) %s";
    }
    
    public String toString(){
        return String.format(outputFormat,  testNumber,
                                            failed ? "Failed" : "Passed", 
                                            duration,
                                            input,
                                            expectedOutput,
                                            actualOutput,
                                            exception==null ? "" : "["+exception.getMessage()+"]");
    }
    
}
