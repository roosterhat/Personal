/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import mathinterpreter.Util.Equation;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 * @param <IN_TYPE>Type to be converted to from string
 */
public class UniaryOperation<IN_TYPE> extends Operation<IN_TYPE>{
    public static int LEFT = -1;
    public static int RIGHT = 1;
    public int inputSide;
    UniaryFunction<IN_TYPE> function;

    public UniaryOperation(String operation,int weight, UniaryFunction<IN_TYPE> f){
        this(operation,weight,RIGHT,f,x->(IN_TYPE)Double.valueOf(x));
    }
    public UniaryOperation(String operation,int weight, int side, UniaryFunction<IN_TYPE> f){
        this(operation,weight,side,f,x->(IN_TYPE)Double.valueOf(x));
    }
    public UniaryOperation(String operation,int weight, int side ,UniaryFunction<IN_TYPE> f,Converter<IN_TYPE> c){
        super(operation,weight,c);
        inputSide = side;
        function = f;
    }
    
    public String execute(Equation equation)throws Exception{
        return function.execute(converter.convert(equation.parsedEquation.get(0)));
    }

    public Output<String> processOperation(Equation equation, int index) throws Exception {
        if((index == 0 && inputSide == LEFT) || (index == equation.parsedEquation.size() - 1 && inputSide == RIGHT))
            throw new Exception("Missing "+(inputSide==LEFT?"left":"right")+" input for UniaryOperation '"+operator+"' in '"+equation+"'");
        Range range = new Range(Math.min(index+inputSide, index),Math.max(index+inputSide, index));
        return new Output(execute(equation.split(range)),range);
    }

}

