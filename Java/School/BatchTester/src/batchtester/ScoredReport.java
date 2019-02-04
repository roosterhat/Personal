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
public class ScoredReport<I,O> extends FunctionalReport<I,O> {
    double score;
    double tolerance;
    public ScoredReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual, double score, double tolerance){
        super(testcase, number, duration, input, expected, actual);
        this.score = score;
        this.tolerance = tolerance;
    }
    
    public ScoredReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual, double score, double tolerance, boolean failed){
        super(testcase, number, duration, input, expected, actual, failed);
        this.score = score;
        this.tolerance = tolerance;
    }
    
    public ScoredReport(FunctionalCase testcase, int number, long duration, I input, O expected, O actual, double score, double tolerance, boolean failed, Exception exception){
        super(testcase, number, duration, input, expected, actual, failed, exception);
        this.score = score;
        this.tolerance = tolerance;
    }
    
    protected void setOutputFormat(){
        outputFormat = "#%d (%s) duration: %d ms (Input: %s Expected: %s Actual: %s) (Score: %s Tolerance: %s) %s";
    }
    
    public String toString(){
        return String.format(outputFormat,  testNumber,
                                            failed ? "Failed" : "Passed", 
                                            duration,
                                            input,
                                            expectedOutput,
                                            actualOutput,
                                            score,
                                            tolerance,
                                            exception==null ? "" : "["+exception.getMessage()+"]");
    }
}
