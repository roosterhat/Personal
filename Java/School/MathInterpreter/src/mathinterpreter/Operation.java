/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public abstract class Operation {
    public String operator;
    public String regex;
    public int weight;
    public int inputSide;
    public static int LEFT = -1;
    public static int RIGHT = 1;
    public static int BOTH = 0;
    public FunctionInterface function;
    public Converter converter;

    public Operation(String operator,int weight,int inputSide,FunctionInterface function)
    {
        this(operator,weight,inputSide,function,x->x);
    }
    
    public Operation(String operator,int weight,int inputSide,FunctionInterface function, Converter converter)
    {
        this.operator = operator;
        this.weight = weight;
        this.inputSide = inputSide;
        this.function = function;
        this.converter = converter;
        regex = genRegex(operator);
    }
    
    private String genRegex(String s){
        if(s.matches("["+Pattern.quote("\\^$.|?*+/")+"]"))
            s = "\\"+s;
        return "("+s+")";
    }
    
    public String execute(ArrayList<String> parts)throws Exception{
        return "";
    }
    
    public String toString(){
        return operator;
    }
}







