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
 * @param <IN_TYPE> Type to be converted to from string
 */
public class BinaryOperation<IN_TYPE> extends Operation<IN_TYPE>{
    BinaryOperationAction<IN_TYPE> function;
   

    public BinaryOperation(String operation, int weight, BinaryOperationAction<IN_TYPE> function, Converter<IN_TYPE> converter){
        super(operation, weight, converter);
        this.function = function;
    }
    
    public Output<String> processOperation(Equation equation, int index)throws Exception{
        Range r = new Range();
        if(index<0)
            throw new Exception("BinaryOperation '"+this.operator+"' is not found in ArrayList "+equation);
        if(index==0)
            throw new Exception("Missing left input for BinaryOperation '"+operator+"'");
        r.start = index - 1;
        if(index==equation.parsedEquation.size()-1)
            throw new Exception("Missing right input for BinaryOperation '"+operator+"'");
        r.end = index + 1;
        return new Output(execute(equation.split(r)),r);
    }
    
    public String execute(Equation parts)throws Exception{
        return function.execute(converter.convert(parts.parsedEquation.get(0)), converter.convert(parts.parsedEquation.get(2)));
    }
    
}
