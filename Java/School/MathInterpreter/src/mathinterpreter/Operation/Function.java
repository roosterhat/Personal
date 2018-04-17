/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import mathinterpreter.MathInterpreter;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public class Function<T> extends Operation<String,String>{
    public FunctionPair bounds;
    public PairFunction<String> function;
    public String seperator;
    
    public Function(String name,int weight, PairFunction<String> function){
        this(name,weight,function,x->Double.valueOf(x));
    }
    
    public Function(String name, int weight, PairFunction<String> function, Converter converter){
        super(name,weight,converter);
        this.seperator = ",";
        this.bounds = new FunctionPair(seperator);
        this.function = function;
        
    }
    
    public void setMain(MathInterpreter m){
        _main = m;
        bounds.weight = weight-1;
        _main.addOperation(bounds);
    }
    
    protected ArrayList<T> convert(ArrayList<String> a)throws Exception{
        ArrayList res = new ArrayList();
        for(String s: a)
            res.add(converter.convert(s));
        return res;
    }
    
    @Override
    public Output processOperation(ArrayList<String> array, int index) throws Exception {
        int start,end;
        String next = array.get(Math.min(index+1, array.size()-1));
        if(!_main.isValidPair(next) && 
            ((FunctionPair)_main.getOperation(next)).open.equals(next))
            throw new Exception("No vaild opening Pair for "+operator);
        start = index+1;
        
        end = findClosingBound(array,start);
        
        String closing = array.get(Math.min(end, array.size()-1));
        if(!_main.isValidPair(closing) && 
            ((FunctionPair)_main.getOperation(closing)).close.equals(closing))
            throw new Exception("No vaild closing Pair for "+operator);
        
        return new Output(execute((ArrayList<String>)bounds.processOperation(array,start).value),new Range(index,end));
    }
        
    private int findClosingBound(ArrayList<String> array, int open){
        int count = 0;
        for(int i = open;i<array.size();i++){
            if(array.get(i).equals(bounds.open))
                count++;
            if(array.get(i).equals(bounds.close))
                count--;
            if(count==0)
                return i;
        }
        return array.size()-1;
    }
    
    public String execute(ArrayList<String> parts)throws Exception{
        return String.valueOf(function.execute(convert(parts)));
    }
    
    public ArrayList<String> getTokens(){
        ArrayList temp = super.getTokens();
        temp.addAll(bounds.getTokens());
        temp.add(",");
        return temp;
    }
    
    public boolean equals(Object o){
        return operator.equals(o) || seperator.equals(o);
    }
    
    public String toString(){
        return operator+seperator+bounds;
    }
    
    public String getUsage(){
        return operator+bounds.open+"<"+bounds.getTypeName()+">"+seperator+" ..."+bounds.close;
    }

    
}
