/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.regex.Pattern;
import Interpreter.Interpreter;
import Interpreter.Equation;

/**
 *
 * @author eriko
 */
public class Summation extends Function{
    public Summation(Equation equation){
        super("summation", 4, x->"", x->x, equation);
    }
    
    public String execute(Equation equation) throws Exception {
        return evaluateSummation(equation);
    }
    
    private String evaluateSummation(Equation equation) throws Exception{
        if(equation.parsedEquation.size() == 3){
            double start;
            String variable = "i";
            if(Pattern.matches("\\w+=\\(?\\d+[\\.\\d]*\\)?",equation.parsedEquation.get(0))){
                String[] parts = ((String)equation.parsedEquation.get(0)).split("=");
                if(parts.length!=2)
                    throw new Exception("Invalid syntax: '"+equation.parsedEquation.get(0)+"' expected 'var=start_index'");
                variable = parts[0];
                start = Double.valueOf(parts[1].replaceAll("[\\(\\)]", ""));
            }
            else{
                try{
                    start = Double.valueOf(equation.parsedEquation.get(0));
                }catch(Exception e){
                    throw new Exception("Invalid syntax: '"+equation.parsedEquation.get(0)+"' expected form 'var=start_index' or Double");
                }
            }
            double end = Double.valueOf(equation.parsedEquation.get(1));
            double total = 0;
            equation.variables.clear();
            equation.variables.add(variable);
            equation.setEquation(equation.parsedEquation.get(2));
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
            throw new Exception("Invalid Argument count for Summation, expected 3 got "+equation.parsedEquation.size());
    }
}

