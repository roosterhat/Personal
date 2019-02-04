/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import Interpreter.Equation;
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
    UniaryOperationAction<IN_TYPE> function;
    
    public UniaryOperation(String operation, int weight, UniaryOperationAction<IN_TYPE> function, Converter<IN_TYPE> converter){
        this(operation, weight, RIGHT, function, converter);
    }
    
    public UniaryOperation(String operation,int weight, int side, UniaryOperationAction<IN_TYPE> function, Converter<IN_TYPE> converter){
        super(operation,weight,converter);
        inputSide = side;
        this.function = function;
    }
    
    public String execute(Equation equation)throws Exception{
        return function.execute(converter.convert(equation.parsedEquation.get(inputSide < 0 ? 0 : 1)));
    }

    public Output<String> processOperation(Equation equation, int index) throws Exception {
        if((index == 0 && inputSide == LEFT) || (index == equation.parsedEquation.size() - 1 && inputSide == RIGHT))
            throw new Exception("Missing "+(inputSide==LEFT?"left":"right")+" input for UniaryOperation '"+operator+"' in '"+equation+"'");
        Range range = new Range(Math.min(index+inputSide, index),Math.max(index+inputSide, index));
        return new Output(execute(equation.split(range)),range);
    }

}

