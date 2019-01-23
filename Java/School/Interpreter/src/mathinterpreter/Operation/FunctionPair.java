/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Interpreter;
import mathinterpreter.Util.Equation;
import mathinterpreter.Util.ObjectInstance;
import mathinterpreter.Util.Range;

/**
 *
 * @author eriko
 */
public class FunctionPair<IN_TYPE> extends Pair{
    String seperator = ",";
    FunctionPairFunction function;
    
    public FunctionPair(int weight, FunctionPairFunction<IN_TYPE> function){
        super("[","]",Math.max(weight + 1, 5));
        this.function = function;
        this.converter = x->x;
    } 
    
    public FunctionPair(int weight, FunctionPairFunction<IN_TYPE> function, Converter converter){
        super("[","]",Math.max(weight + 1, 5));
        this.function = function;
        this.converter = converter;
    }
    
    public String execute(Equation equation)throws Exception{
        return function.execute(convert(evaluateContents(getFunctionContents(equation))));
    } 
    
    protected ArrayList<IN_TYPE> convert(ArrayList<String> a)throws Exception{
        ArrayList res = new ArrayList();
        for(String s: a)
            res.add(converter.convert(s));
        return res;
    }
    
    protected ArrayList<String> evaluateContents(ArrayList<Equation> contents)throws Exception{
        ArrayList<String> results = new ArrayList();
        for(Equation eq : contents)
            if(!eq.equation.isEmpty())
                results.add(Interpreter.evaluate(eq).equation);
        return results;
    }
    
    protected ArrayList<Equation> getFunctionContents(Equation equation){
        ArrayList<Equation> results = new ArrayList();
        int marker = 0;
        for(ObjectInstance instance: equation.segmentedObjectEquation){
            if(instance.object.equals(seperator)){
                results.add(equation.split(new Range(marker,instance.range.end - 1)));
                marker = instance.range.end + 1;
            }
        }
        results.add(equation.split(new Range(marker,equation.objectEquation.size())));
        return results;
    }
    
    public ArrayList<String> getTokens(){
        return new ArrayList(Arrays.asList(new String[]{open,close,seperator}));
    }
    
    public boolean equals(Object o){
        return open.equals(o) || close.equals(o) || seperator.equals(o);
    }
    
    public String toString(){
        return open+seperator+close;
    }
    
}
