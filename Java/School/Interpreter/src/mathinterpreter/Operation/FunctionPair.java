/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import Interpreter.Interpreter;
import Interpreter.Equation;
import mathinterpreter.Util.ObjectInstance;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author eriko
 */
public class FunctionPair<IN_TYPE> extends Pair{
    Seperator seperator = new Seperator(",");
    String postSeperator = ";";
    FunctionPairAction function;
    
    public FunctionPair(int weight){
        this(weight, x->x);
    } 
    
    public FunctionPair(int weight, Converter converter){
        this(weight, converter, new DefaultFunction());
    } 
    
    public FunctionPair(int weight, Converter converter, FunctionPairAction<IN_TYPE> function){
        super("[","]",weight);
        this.function = function;
        this.converter = converter;
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        Range range = findRange(equation.parsedEquation, index);
        return new Output(execute(equation.split(new Range(range.start + 1, range.end - 1))),range);
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
            if(instance.object != null && instance.object.equals(seperator)){
                results.add(equation.split(new Range(marker,instance.range.end - 1)));
                marker = instance.range.end + 1;
            }
        }
        if(marker < equation.objectEquation.size())
            results.add(equation.split(new Range(marker,equation.objectEquation.size()-1)));
        return results;
    }
    
    public ArrayList<String> getTokens(){
        return new ArrayList(Arrays.asList(new String[]{open,close}));
    }
    
    public boolean equals(Object o){
        return open.equals(o) || close.equals(o);
    }
    
    public String toString(){
        return open+seperator+close;
    }
    
}

class Seperator extends Operation{
    public Seperator(String seperator){
        super(seperator, 0, x->x);
    }

    @Override
    public Output processOperation(Equation equation, int index) throws Exception {
        return new Output("",new Range(index,index));
    }

    @Override
    protected String execute(Equation parts) throws Exception {
        return "";
    }
}

class DefaultFunction<TYPE> implements FunctionPairAction<TYPE>{
    public String execute(ArrayList<TYPE> array){
        String res = "";
        for(int i = 0; i <= array.size()-1; i++)
            res += array.get(i)+";";
//        if(!res.isEmpty())
//            res = res.substring(0, res.length()-1);
        return res;
    }
}
