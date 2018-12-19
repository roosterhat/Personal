/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public class BinaryOperation<T> extends Operation<String,T>{
    BinaryFunction<T,String> function;
   
    public BinaryOperation(String o,int w,BinaryFunction<T,String> f){
        this(o,w,f,x->(T)Double.valueOf(x));
    }
    public BinaryOperation(String o,int w,BinaryFunction<T,String> f,Converter<T> c){
        super(o,w,c);
        function = f;
    }
    
    public Output<String> processOperation(ArrayList<String> array, int index)throws Exception{
        if(index<0)
            throw new Exception("BinaryOperation '"+this.operator+"' is not found in ArrayList "+array);
        ArrayList<String> parts = new ArrayList();
        if(index==0)
            throw new Exception("Missing left input for BinaryOperation '"+operator+"'");
        parts.add(array.get(index-1));
        if(index==array.size()-1)
            throw new Exception("Missing right input for BinaryOperation '"+operator+"'");
        parts.add(array.get(index+1));
        return new Output(execute(parts),new Range(index-1,index+1));
    }
    
    public String execute(ArrayList<String> parts)throws Exception{
        return function.execute(converter.convert(parts.get(0)), converter.convert(parts.get(1)));
    }
        
    public String getUsage(){
        String type = getTypeName();
        return "<"+type+"> "+operator+" <"+type+">";
    }
    
}
