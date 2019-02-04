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
public class TestReport {
    public TestCase testCase;
    public boolean failed;
    public Exception exception;
    public int testNumber;
    public String outputFormat;
    public long duration;
    
    public TestReport(TestCase testcase, int number, long duration){
        this.testCase = testcase;
        this.testNumber = number;
        this.duration = duration;
        this.failed = false;
        setOutputFormat();
    }
    
    public TestReport(TestCase testcase, int number, long duration, boolean failed){
        this.testCase = testcase;
        this.duration = duration;
        this.testNumber = number;
        this.failed = failed;
        setOutputFormat();
    }
    
    public TestReport(TestCase testcase, int number, long duration, boolean failed, Exception exception){
        this.testCase = testcase;
        this.duration = duration;
        this.testNumber = number;
        this.failed = failed;
        this.exception = exception;
        setOutputFormat();
    }
    
    protected void setOutputFormat(){
        outputFormat = "#%d (%s) duration: %d ms %s";
    }
    
    public String toString(){
        return String.format(outputFormat,  testNumber,
                                            failed ? "Failed" : "Passed", 
                                            duration,
                                            exception==null ? "" : "["+exception.getMessage()+"]");
    }
}
