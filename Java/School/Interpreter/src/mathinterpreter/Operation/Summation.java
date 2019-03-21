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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eriko
 */
public class Summation extends Function{
    Interpreter interpreter;
    public Summation(Interpreter interpreter){
        super("summation", 4, x->"", x->x, interpreter.equation);
        this.interpreter = interpreter;
    }
    
    public String execute(Equation equation) throws Exception {
        return evaluateSummation(equation);
    }
    
    private String evaluateSummation(Equation equation) throws Exception{
        ArrayList<String> args = new ArrayList(Arrays.asList(equation.parsedEquation.get(0).split(";")));
        if(args.size() == 3){
            double start;
            String variable = "i";
            if(Pattern.matches("\\w+=\\d+[\\.\\d*]?",args.get(0))){
                String[] parts = ((String)args.get(0)).split("=");
                if(parts.length!=2)
                    throw new Exception("Invalid syntax: '"+args.get(0)+"' expected 'var=start_index'");
                variable = parts[0];
                start = Double.valueOf(parts[1]);
            }
            else{
                try{
                    start = Double.valueOf(args.get(0));
                }catch(Exception e){
                    throw new Exception("Invalid syntax: '"+args.get(0)+"' expected form 'var=start_index' or Double");
                }
            }
            double end = Double.valueOf(args.get(1));
            double total = 0;
            Equation eq = (Equation)equation.clone();
            eq.variables.clear();
            eq.variables.add(variable);
            eq.setEquation(args.get(2));
            interpreter.setEquation(eq);
            Map<String,String> map = new HashMap();
            map.put(variable,"0");
            for(double i = start; i<=end; i++){
                //interpreter.setEquation(eq); 
                try{
                    map.put(variable, String.valueOf(i));
                    String res = interpreter.interpret(map);
                    total += Double.valueOf(res);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return Double.toString(total);
        }
        else
            throw new Exception("Invalid Argument count for Summation, expected 3 got "+args.size());
    }
}

