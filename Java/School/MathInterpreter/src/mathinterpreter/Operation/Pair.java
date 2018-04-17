/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public abstract class Pair  extends Operation<ArrayList<String>,String>{
    
    public String open;
    public String close;
    public String regex;
    protected PairFunction<ArrayList<String>> function;
    public Pair(String open, String close, int weight){
        this(open,close,weight,x->x);
    }
    
    public Pair(String open, String close, int weight, PairFunction<ArrayList<String>> function)
    {
        super(open+close,weight);
        this.open = open;
        this.close = close;
        this.function = function;
    }
    
    public Output processOperation(ArrayList<String> array, int index)throws Exception{
        Range range = getRange(array,index);
        return new Output(execute(new ArrayList(array.subList(range.start+1,range.end))),range);
    }
    
    protected Range getRange(ArrayList<String> array, int index){
        int open = index;
        int close = findClosing(new ArrayList(array.subList(index, array.size())))+index;
        return new Range(open,close);
    }
    
    protected int findClosing(ArrayList<String> array){
        int count = 1;
        for(int i = 1;i<array.size();i++){
            String s = array.get(i);
            if(s.equals(open))
                count++;
            if(s.equals(close))
                count--;
            if(count==0)
                return i;
        }
        return -1;
    }
    
    public ArrayList<String> execute(ArrayList<String> array)throws Exception{
        return function.execute(array);
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
        String type = getTypeName();
        return open+" <"+type+"> ... "+close;
    }
}
