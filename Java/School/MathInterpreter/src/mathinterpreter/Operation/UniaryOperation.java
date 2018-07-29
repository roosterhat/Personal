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
public class UniaryOperation<T> extends Operation<String,T>{
    public static int LEFT = -1;
    public static int RIGHT = 1;
    public int inputSide;
    UniaryFunction<T,String> function;

    public UniaryOperation(String operation,int weight, UniaryFunction<T,String> f){
        this(operation,weight,RIGHT,f,x->(T)Double.valueOf(x));
    }
    public UniaryOperation(String operation,int weight, int side, UniaryFunction<T,String> f){
        this(operation,weight,side,f,x->(T)Double.valueOf(x));
    }
    public UniaryOperation(String operation,int weight, int side ,UniaryFunction<T,String> f,Converter<T> c){
        super(operation,weight,c);
        inputSide = side;
        function = f;
    }
    
    @Override
    public String execute(ArrayList<String> parts)throws Exception{
        return function.execute(converter.convert(parts.get(0)));
    }

    @Override
    public Output<String> processOperation(ArrayList<String> array, int index) throws Exception {
        if(index<0)
            throw new Exception("UniaryOperation '"+this.operator+"' is not found in ArrayList "+array);
        ArrayList<String> parts = new ArrayList();
        if((index==0&&inputSide==LEFT)||(index==array.size()-1&&inputSide==RIGHT))
            throw new Exception("Missing "+(inputSide==LEFT?"left":"right")+" input for UniaryOperation '"+operator+"'");
        parts.add(array.get(index+inputSide));
        return new Output(execute(parts),new Range(Math.min(index+inputSide, index),Math.max(index+inputSide, index)));
    }
    
    public String getUsage(){
        String type = getTypeName();
        return inputSide==UniaryOperation.LEFT ? "<"+type+"> "+operator : operator+" <"+type+">";
    }

}

