/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author eriko
 */
public class BatchTester {
    private IdGenerator testCaseIDGen;
    private IdGenerator batchReportIDGen;
    private ArrayList<TestCase> testCases;
    public ArrayList<BatchReport> reports;
    public OutputStream output;
    
    public BatchTester(){
        testCaseIDGen = new IdGenerator();
        batchReportIDGen = new IdGenerator();
        testCases = new ArrayList();
        reports = new ArrayList();
        output = System.out;
    }
    
    public String addTestCase(TestCase testcase){
        testcase.id = testCaseIDGen.generateID(IdGenerator.DEFAULTLENGTH);
        testCases.add(testcase);
        return testcase.id;
    }
    
    public void runTestCases(){
        long start = System.nanoTime();
        BatchReport report = new BatchReport(testCaseIDGen.generateID(IdGenerator.DEFAULTLENGTH));
        for(TestCase testcase : testCases)
            report.addAll(testcase,testcase.runTestCases());
        long end = System.nanoTime();
        report.setDuration(start, end);
        reports.add(report);
    }
    
    public void displayReports(){
        for(BatchReport report : reports)
            report.printReports();
    }
    
    public ArrayList<BatchReport> getBatchReports(){
        return reports;
    } 
    
    public ArrayList<BatchReport> getBatchReports(Predicate predicate){
        return (ArrayList<BatchReport>)reports.stream().filter(x->predicate.test(x)).collect(Collectors.toList());
    }
}
