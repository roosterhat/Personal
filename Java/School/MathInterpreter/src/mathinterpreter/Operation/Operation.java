/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.MathInterpreter;
import mathinterpreter.Util.Output;

/**
 *
 * @author ostlinja
 */
public abstract class Operation<T1,T2> {
    public String operator, usage;
    public int weight;
    public Converter<T2> converter;
    protected MathInterpreter _main;
    private final TypeToken<T2> typeToken = new TypeToken<T2>(getClass()) { };
    private final Type type = typeToken.getType(); 

    public Operation(String operator,int weight)
    {
        this(operator,weight,x->x);
    }
    
    public Operation(String operator,int weight, Converter converter)
    {
        this.operator = operator;
        this.weight = weight;
        this.converter = converter;
    }
    
    public void setMain(MathInterpreter m){
        _main = m;
    }
    
    public MathInterpreter getMain(){
        return _main;
    }
    
    public Converter getConverter(){
        return converter;
    }
    
    public void setConverter(Converter<T2> c){
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
    
    public abstract Output<String> processOperation(ArrayList<String> array, int index)throws Exception;
    
    public abstract T1 execute(ArrayList<String> parts)throws Exception;
    
    public ArrayList<String> getTokens(){
        return new ArrayList(Arrays.asList(new String[]{operator}));
    }
    
    public T2 convert(String s)throws Exception{
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
    
    public String getTypeName(){
        String[] t = type.getTypeName().split("\\.");
        return t[t.length-1];
    }
    
    public String getUsage(){
        return usage==null ? operator : usage;
    }
    
    protected String arrayToString(ArrayList<String> array){
        String result = "";
        for(String s: array)
            result += s;
        return result;
    }
}







