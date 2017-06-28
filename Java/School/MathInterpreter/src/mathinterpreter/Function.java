/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public class Function {//Similar to Operaitons but takes multiple arguments enclosed in the given Pair
    public Pair bounds;
    public String name;
    public FunctionFunction function;
    public Converter converter;
    public int weight;
    public String regex;
    public String separator = ",";
    
    public Function(String name,int weight, FunctionFunction function){
        this(name,weight,function,x->Double.valueOf(x));
    }
    
    public Function(String name,int weight, FunctionFunction function, Converter converter){
        this(name,weight,function,converter,new FunctionPair());
    }
    
    public Function(String name, int weight, FunctionFunction function, Converter converter, Pair bounds){
        this.name = name;
        this.bounds = bounds;
        this.weight = weight;
        this.function = function;
        this.converter = converter;
        this.regex = genRegex(name);
    }
    
    private String genRegex(String s){
        if(s.matches(".*["+Pattern.quote("\\^$.|?*+/")+"]"))
            s = Pattern.quote(s);
        return "("+s+")|"+bounds.regex+"|("+separator+")";
    }
    
    public ArrayList convert(ArrayList<String> a){
        ArrayList res = new ArrayList();
        for(String s: a)
            res.add(converter.convert(s));
        return res;
    }
    
    public String execute(ArrayList<String> parts)throws Exception{
        return function.execute(convert(parts));
    }
    
    public String toString(){
        return name;
    }
}

class FunctionPair extends Pair{
    public FunctionPair(){
        super("[","]",5,(PairFunction)x->{return (ArrayList)x.subList(0, 0);});
    }
}

interface PairFunction extends FunctionInterface{
    ArrayList execute(ArrayList a)throws Exception;
}