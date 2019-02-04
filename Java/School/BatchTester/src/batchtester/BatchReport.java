/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eriko
 */
public class BatchReport {
    public Map<String, ArrayList<TestReport>> reports;
    private ArrayList<TestCase> index;
    private String ID; 
    private Date timeRan;
    private long duration;
    public String headerFormat;
    public String testCaseFormat;
    public String footerFormat;
    
    public BatchReport(String id){
        ID = id;
        reports = new HashMap();
        index = new ArrayList();
        timeRan = Calendar.getInstance().getTime();
        headerFormat = "Batch Report: ID: %s created %tc";
        testCaseFormat = "%s ID: %s %s";
        footerFormat = "%d Test Cases, %d reports (%d success, %d failures) Runtime: %d ms";
    }
    
    public void setDuration(long duration){
        this.duration = duration;
    }
    
    public void setDuration(long start, long end){
        this.duration = (end-start)/1000000;
    }
    
    public int getNumberSuccess(){
        int success = 0;
        for(ArrayList<TestReport> array :reports.values())
            for(TestReport report: array)
                if(!report.failed)
                    success++;
        return success;
    }
    
    public int getNumberFailed(){
        int failed = 0;
        for(ArrayList<TestReport> array :reports.values())
            for(TestReport report: array)
                if(report.failed)
                    failed++;
        return failed;
    }
    
    public void printFailedReports(){
        System.out.println(String.format(headerFormat, ID, timeRan));
        ArrayList<TestCase> failed = new ArrayList();
        for(ArrayList<TestReport> array :reports.values())
            for(TestReport report: array)
                if(report.failed)
                    failed.add(report.testCase);
        displayReports(failed);
        int failure = getNumberFailed();
        System.out.println(String.format(footerFormat, reports.size(), failure,0,failure,duration )+"\n");
    }
    
    public void printReports(){
        System.out.println(String.format(headerFormat, ID, timeRan));
        displayReports(index);
        int success = getNumberSuccess();
        int failure = getNumberFailed();
        System.out.println(String.format(footerFormat, reports.size(), success+failure,success,failure,duration )+"\n");
    }
    
    private void displayReports(ArrayList<TestCase> cases){
        for(TestCase test : cases){
            String id = test.id;
            String[] temp = test.getClass().getName().split("\\.");
            String type = test.getType();
            System.out.println(String.format(testCaseFormat, type, id, test.title==null ? "" : "["+test.title+"]"));
            for(TestReport report: reports.get(id))
                System.out.println("\t"+report);
        }
    }
    
    public void add(TestCase test, TestReport report){
        String id = test.id;
        if(reports.containsKey(id))
            reports.get(id).add(report);
        else{
            reports.put(id, new ArrayList(Arrays.asList(new TestReport[]{report})));
            index.add(test);
        }
    }
    
    public void addAll(TestCase test, ArrayList<? extends TestReport> reports){
        String id = test.id;
        if(this.reports.containsKey(id))
            this.reports.get(id).addAll(reports);
        else{
            this.reports.put(id, (ArrayList<TestReport>)reports);
            index.add(test);
        }
    }
    
    public String toString(){
        int success = getNumberSuccess();
        int failure = getNumberFailed();
        return String.format(headerFormat, ID, timeRan) +" {"+String.format(footerFormat, reports.size(), success+failure,success,failure,duration )+"}";
    }
}
