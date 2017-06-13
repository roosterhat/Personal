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
    String operator;
    String regex;
    int weight;
    int inputSide;
    static int LEFT = -1;
    static int RIGHT = 1;
    static int BOTH = 0;
    FunctionInterface function;
    Converter converter;

    public Operation(String operator,int weight,int inputSide,FunctionInterface function)
    {
        this(operator,weight,inputSide,function,null);
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
    
    public String execute(ArrayList<String> parts){
        return "";
    }
    
    public String toString(){
        return operator;
    }
}







