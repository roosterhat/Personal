/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import Interpreter.Equation;
import Interpreter.Interpreter;
import java.util.ArrayList;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public class Function<IN_TYPE> extends Operation<IN_TYPE>{
    public FunctionPair bounds;
    public FunctionAction function;
    
    public Function(String name, int weight, FunctionAction<IN_TYPE> function, Converter<IN_TYPE> converter, Equation equation){
        this(name, weight, function, converter, new FunctionPair(weight - 1), equation);
    }
    
    public Function(String name, int weight, FunctionAction<IN_TYPE> function, Converter<IN_TYPE> converter, FunctionPair bounds, Equation equation){
        super(name, weight, converter);
        this.function = function;
        this.bounds = bounds; //ensure that the weight of the FunctionPair is less than the function
        equation.addOperation(bounds);
        equation.addOperation(bounds.seperator);
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        Object obj = equation.objectEquation.get(Math.min(index + 1, equation.objectEquation.size() - 1));
        if(obj instanceof FunctionPair){
            Range range = ((FunctionPair) obj).findRange(equation.parsedEquation, index + 1);
            return new Output(execute(Interpreter.evaluate(equation.split(range))), new Range(index,range.end));
        }
        throw new Exception("Expected FunctionPair adjacent to Function '"+super.operator+"' in '"+equation+"'");
        
    }
    
    public String execute(Equation equation)throws Exception{
        return function.execute(convertInput(equation.parsedEquation.get(0)));
    }
    
    private ArrayList<IN_TYPE> convertInput(String input)throws Exception{
        ArrayList<IN_TYPE> res = new ArrayList();
        for(String s : input.split(bounds.seperator.operator))
            res.add(converter.convert(s));
        return res;
    }
    
    public ArrayList<String> getTokens(){
        ArrayList temp = super.getTokens();
        temp.addAll(bounds.getTokens());
        return temp;
    }

    public String toString(){
        return operator+bounds;
    }
}