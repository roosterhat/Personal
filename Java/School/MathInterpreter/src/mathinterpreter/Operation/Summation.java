/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import java.util.regex.Pattern;
import mathinterpreter.MathInterpreter;

/**
 *
 * @author eriko
 */
public class Summation extends Function{
    public Summation(){
        this(new Object[]{});
    }
    
    public Summation(Object[] args){
        this(null,args);
    }
    
    public Summation(Class classType, Object[] args) {
        super("summation",3,new SummationFunction(classType, args),x->x);
        usage = "summation[start, end, equation], default variable: 'i'";
        if(classType==null)
            setEquation(this._main.getClass(),args);
    }
    
    public void setEquation(Class equationType, Object[] args){
        ((SummationFunction)this.function).setEquation(equationType, args);
    }
    
}

class SummationFunction implements PairFunction<Double>{
    Class classType;
    Object[] arguments;
    public SummationFunction(Class equationClass, Object[] args){
        classType = equationClass;
    }
    
    public Double execute(ArrayList a) throws Exception {
        ArrayList<String> array = (ArrayList<String>)a;
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
            Class[] paramTypes = new Class[arguments.length];
            for(int i = 0; i < arguments.length; i++)
                paramTypes[i] = arguments[i].getClass();
            MathInterpreter equation = (MathInterpreter)classType.getConstructor(paramTypes).newInstance(arguments);
            equation.variables.clear();
            equation.variables.add(variable);
            equation.setEquation(array.get(2));
            for(double i = start; i<=end; i++){
                try{
                    String res = equation.interpret(String.valueOf(i));
                    total += Double.valueOf(res);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            return total;
        }
        else
            throw new Exception("Invalid Argument count for Summation, expected 3 got "+array.size());
    }

    public void setEquation(Class equationType, Object[] args){
        classType = equationType;
        arguments = args;
    }
}
