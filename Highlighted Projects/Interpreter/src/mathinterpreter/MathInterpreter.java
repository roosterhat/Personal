/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import Interpreter.Equation;
import Interpreter.Interpreter;
import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Operation.Operation;
import mathinterpreter.Operation.Pair;
import mathinterpreter.Operation.UniaryOperation;

/**
 *
 * @author eriko
 */
public class MathInterpreter extends Interpreter{
    public MathInterpreter(){
        super(new MathEquation());
    }
    
    public MathInterpreter(String equation){
        super(new MathEquation(equation));
    }
    
    public MathInterpreter(MathEquation equation){
        super(equation);
    }
    
    public String f(int... arguments)throws Exception{
        double[] res = new double[arguments.length];
        for(int i=0;i<arguments.length;i++)
            res[i] = (double)arguments[i];
        return f(res);
    }
   
    //attempts to solve the equation for the given value
    public String f(double... arguments)throws Exception{
        String[] args = new String[arguments.length];
        for(int i = 0;i<arguments.length;i++)
            args[i] = String.valueOf(arguments[i]);
        return interpret(args);
    }
    
    public double fD(double... args)throws Exception{
        return Double.valueOf(f(args));
    }
    
    public double fD(int... args)throws Exception{
        return Double.valueOf(f(args));
    }

    @Override
    protected void preProcessEquation(Equation equation) {
        Operation op = equation.operations.get(0);
        for(int i=0;i<equation.objectEquation.size();i++){
            String part = equation.parsedEquation.get(i);
            if(equation.variables.contains(part)){
                if((i - 1) >= 0 && !(equation.objectEquation.get(i - 1) instanceof Operation)){
                    equation.parsedEquation.add(i, "*");
                    equation.objectEquation.add(i++, op);
                }
                if((i + 1) < equation.objectEquation.size() && !(equation.objectEquation.get(i + 1) instanceof Operation)){
                    equation.parsedEquation.add(i + 1, "*");
                    equation.objectEquation.add(++i, op);
                }
            }
            else if(isNegativeNumber(equation.parsedEquation,i)){
                equation.parsedEquation.set(i,"-"+equation.parsedEquation.get(i+1));
                equation.objectEquation.set(i++,new String());
                equation.parsedEquation.remove(i);
                equation.objectEquation.remove(i--);
            }
            else if(part.equals("(") && !(equation.objectEquation.get(Math.max(i - 1, 0)) instanceof Operation)){
                equation.parsedEquation.add(i, "*");
                equation.objectEquation.add(i++, op);
            }
            else if(part.equals(")") && !(equation.objectEquation.get(Math.min(i + 1, equation.objectEquation.size() - 1)) instanceof Operation)){
                equation.parsedEquation.add(i + 1, "*");
                equation.objectEquation.add(++i, op);
            }
        }
        equation.setEquation(equation.parsedEquation);
    }
    
    //functions the same as the isNegativeNumber but handles ArrayLists
    //used when the negative sign and number are split
    protected boolean isNegativeNumber(ArrayList<String> s, int index){
        String part = s.get(index);
        if(equation.isValidOperation(part)){
            Operation o = equation.getOperation(s.get(Math.max(index-1, 0)));
            if(part.equals("-"))
                return  (index==0)  ||
                        ((o!=null && 
                        (o instanceof UniaryOperation ? ((UniaryOperation)o).inputSide==UniaryOperation.RIGHT : true) &&
                        (o instanceof Pair ? ((Pair)o).open.equals(s.get(index-1)) : true)) &&
                        (index<s.size() && isNumber(s.get(index+1))));
        }
        else if(isNumber(part))
            return Double.valueOf(part)<0;
        return false;
    } 
                 
    public boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
}

