/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public abstract class TestCase<I,O>{
    protected String type = "TestCase";
    public String id,title;
    public Test<I,O> test;
    
    public TestCase(){
        id = IdGenerator.generateGenericID(IdGenerator.DEFAULTLENGTH);
    }
    
    public TestCase(Test<I,O> test){
        this.test = test;
        this.id = IdGenerator.generateGenericID(IdGenerator.DEFAULTLENGTH);
    }
    
    public String getType(){
        return type;
    }
    
    public abstract ArrayList<? extends TestReport> runTestCases();
}
