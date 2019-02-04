/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import Interpreter.Equation;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 * @param <IN_TYPE> Type to be converted to from string
 */
public abstract class Operation<IN_TYPE> {
    public String operator, usage;
    public int weight;
    public Converter<IN_TYPE> converter;

    public Operation(String operator,int weight){
        this(operator,weight,x->x);
    }
    
    public Operation(String operator, int weight, Converter converter){
        this.operator = operator;
        this.weight = weight;
        this.converter = converter;
    }
    
    public ArrayList<Range> findAllInstances(ArrayList<String> equation){
        ArrayList<Range> instances = new ArrayList();
        for(int i = 0; i < equation.size(); i++)
            if(operator.equals(equation.get(i)))
                instances.add(new Range(i,i));
        return instances;
    }
    
    public Range findRange(ArrayList<String> equation, int index){
        return new Range(index, index);
    }
    
    public Converter getConverter(){
        return converter;
    }
    
    public void setConverter(Converter<IN_TYPE> c){
        converter = c;
    }
    
    public String getOperator(){
        return operator;
    }
    
    public void setOperator(String operator){
        this.operator = operator;
    }
    
    public int getWeight(){
        return weight;
    }
    
    public void setWeight(int weight){
        this.weight = weight;
    }
    
    public abstract Output<String> processOperation(Equation equation, int index)throws Exception;
    
    protected abstract String execute(Equation parts)throws Exception;
    
    public ArrayList<String> getTokens(){
        return new ArrayList(Arrays.asList(new String[]{operator}));
    }
    
    public IN_TYPE convert(String s)throws Exception{
        return converter.convert(s);
    }
    
    public boolean equals(Object o){
        if(o instanceof Operation)
            return ((Operation)o).operator.equals(operator);
        return o.equals(operator);
    }
    
    public String toString(){
        return operator;
    }
    
    public String getUsage(){
        return usage==null ? operator : usage;
    }
    
    /*protected String arrayToString(ArrayList<String> array){
        String result = "";
        for(String s: array)
            result += s;
        return result;
    }*/
}







