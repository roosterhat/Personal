/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.regex.Pattern;
import mathinterpreter.Interpreter;
import mathinterpreter.Util.Equation;

/**
 *
 * @author eriko
 */
public class Summation extends Function{
    public Summation(){
        super("summation",3,new SummationPair(3));
        usage = "summation[start, end, equation], default variable: 'i'";
    }
}

class SummationPair extends FunctionPair{
    public SummationPair(int weight){
        super(weight, x->"", x->x);
    }
    
    public String execute(Equation equation) throws Exception {
        return evaluateSummation(convert(evaluateContents(getFunctionContents(equation))),equation);
    }
    
    private String evaluateSummation(ArrayList<String> array, Equation equation) throws Exception{
        if(array.size() == 3){
            double start;
            String variable = "i";
            if(Pattern.matches("\\w+=\\(?\\d+[\\.\\d]*\\)?",array.get(0))){
                String[] parts = ((String)array.get(0)).split("=");
                if(parts.length!=2)
                    throw new Exception("Invalid syntax: '"+array.get(0)+"' expected 'var=start_index'");
                variable = parts[0];
                start = Double.valueOf(parts[1].replaceAll("[\\(\\)]", ""));
            }
            else{
                try{
                    start = Double.valueOf(array.get(0));
                }catch(Exception e){
                    throw new Exception("Invalid syntax: '"+array.get(0)+"' expected form 'var=start_index' or Double");
                }
            }
            double end = Double.valueOf(array.get(1));
            double total = 0;
            equation.variables.clear();
            equation.variables.add(variable);
            equation.setEquation(array.get(2));
            for(double i = start; i<=end; i++){
                try{
                    String res = new Interpreter(equation).interpret(String.valueOf(i));
                    total += Double.valueOf(res);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return Double.toString(total);
        }
        else
            throw new Exception("Invalid Argument count for Summation, expected 3 got "+array.size());
    }
}

