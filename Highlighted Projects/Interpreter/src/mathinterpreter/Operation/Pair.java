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
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public abstract class Pair  extends Operation<String>{
    
    public String open, close;
    protected PairAction function;
    public Pair(String open, String close, int weight){
        this(open,close,weight,new DefaultPairFunction());
    }
    
    public Pair(String open, String close, int weight, PairAction function)
    {
        super(open+close,weight);
        this.open = open;
        this.close = close;
        this.function = function;
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        Range range = findRange(equation.parsedEquation, index);
        return new Output(execute(equation.split(new Range(range.start + 1, range.end - 1))),range);
    }
    
    public Range findRange(ArrayList<String> equation, int index){
        try{
            return new Range(index, findClosing(equation,index));
        }catch(Exception e){
            System.out.println(e);
            return new Range(index,index);
        }
    }
    
    protected int findClosing(ArrayList<String> array, int index)throws Exception{
        int count = 0;
        for(int i = index;i<array.size();i++){
            String part = array.get(i);
            if(part.equals(close))
                count--;
            if(part.equals(open))
                count++;
            if(count==0)
                return i;
        }
        throw new Exception("Unable to find closing pair '"+close+"' in '"+String.join("", array)+"'");
    }
    
    public ArrayList<Range> findAllInstances(ArrayList<String> array){
        ArrayList<Range> instances = new ArrayList();
        for(int i = 0; i < array.size();i++){
            if(open.equals(array.get(i))){
                try{
                    int closeIndex = findClosing(new ArrayList(array.subList(i, array.size())),0);
                    instances.add(new Range(i,closeIndex));
                }catch(Exception e){
                    System.out.println(e);
                }
            }
        }
        return instances;
    }
    
    public String execute(Equation equation)throws Exception{
        return function.execute(equation);
    }
    
    public ArrayList<String> getTokens(){
        return new ArrayList(Arrays.asList(new String[]{open,close}));
    }
    
    public boolean equals(Object o){
        if(o instanceof Pair)
            return ((Pair)o).open.equals(open) && ((Pair)o).close.equals(close);
        return o.equals(open) || o.equals(close);
    }
    
    public String toString(){
        return open+close;
    }
    
    public String getUsage(){
        String type = "Double";
        return open+"<"+type+"> ... "+close;
    }
}

class DefaultPairFunction implements PairAction{
    public String execute(Equation equation) throws Exception {
        return Interpreter.evaluate(equation).equation;
    }
    
}
