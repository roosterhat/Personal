/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
class Equation {
    private ArrayList<Operation> operators;
    private ArrayList<SingleOperation> functions;
    private ArrayList<Operation> constants;
    private DecimalFormat df;
    String equation;
    String processedEquation;
    String variable = "x";

    public Equation(String s){
        setDecimalDepth(5);
        equation = s;
        operators = new ArrayList();
        getOperators();
        functions = new ArrayList();
        getFunctions();
        constants = new ArrayList();
        getConstants();
    }
    
    private void getOperators(){
        operators.add(new Operation("+",0,(x,y)->x+y));
        operators.add(new Operation("-",0,(x,y)->x-y));
        operators.add(new Operation("*",1,(x,y)->x*y));
        operators.add(new Operation("/",1,(x,y)->df.format(x/y)));
        operators.add(new Operation("^",2,(x,y)->df.format(Math.pow(x,y))));
        operators.add(new Operation("E",2,(x,y)->df.format(x*Math.pow(10, y))));
        
        operators.sort((x,y)->((Operation)y).weight-((Operation)x).weight);
    }
       
    private void getFunctions(){
        functions.add(new SingleOperation("sin",0,x->df.format(Math.sin(x))));
        functions.add(new SingleOperation("cos",0,x->df.format(Math.cos(x))));
        functions.add(new SingleOperation("tan",0,x->df.format(Math.tan(x))));
        functions.add(new SingleOperation("csc",0,x->df.format(1/Math.sin(x))));
        functions.add(new SingleOperation("sec",0,x->df.format(1/Math.cos(x))));
        functions.add(new SingleOperation("cot",0,x->df.format(1/Math.tan(x))));
        functions.add(new SingleOperation("asin",0,x->df.format(Math.asin(x))));
        functions.add(new SingleOperation("acos",0,x->df.format(Math.acos(x))));
        functions.add(new SingleOperation("atan",0,x->df.format(Math.atan(x))));
        functions.add(new SingleOperation("acsc",0,x->df.format(1/Math.asin(x))));
        functions.add(new SingleOperation("asec",0,x->df.format(1/Math.acos(x))));
        functions.add(new SingleOperation("acot",0,x->df.format(1/Math.atan(x))));
        functions.add(new SingleOperation("log",0,x->df.format(Math.log(x))));
        functions.add(new SingleOperation("ln",0,x->df.format(Math.log(x))));
        functions.add(new SingleOperation("abs",0,x->df.format(Math.abs(x))));
        functions.add(new SingleOperation("sqrt",0,x->df.format(Math.sqrt(x))));
        functions.add(new SingleOperation("!",0,x->{
                double res = 1;
                for(int i=(int)x;i>0;i--)
                    res*=i;
                return res;
        }));
    }
    
    private void getConstants(){
        constants.add(new Operation("pi",0,(x,y)->Math.PI));
        constants.add(new Operation("e",0,(x,y)->Math.E));
    }
    
    public void setDecimalDepth(int d){
        if(d>=0)
            df = new DecimalFormat("#."+String.format("%0" + d + "d", 0).replace("0","#"));
    }
    
    //finds the values on either side of the index
    //used to find the numbers on either side of a operator
    private Range findPart(String s,int index){
        int start = Math.max(findSubPart(s,index-1,-1),0);
        int end = Math.min(findSubPart(s,index+1,1),s.length());
        return new Range(start,end+1);
    }
    
    //finds a number based on index and direction
    //direction is either -1 or 1 (left or right)
    //i.e. findSubPart("12+34",2,-1) = 12
    private int findSubPart(String s,int index,int dir){
        if(index>=0 && index<s.length()){
            if(String.valueOf(s.charAt(index)).matches("[()]"))
                return findMatchingParen(s,index,dir);
            else{
                int pos = 0;
                for(int i=index;i!=Math.max(-1, (dir*s.length())%(s.length()+1));i+=dir){
                    if((""+s.charAt(i)).matches("[\\d\\.]"))
                        pos = i;
                    else if(s.charAt(i)=='-'){
                        if(findNegativeNumber(s,i))
                            pos = i;
                    }
                    else
                        return pos;
                }
                return pos;
            }
        }
        return index;
    }
    
    //determines if there is a negative number at the given index
    private boolean findNegativeNumber(String s,int index)
    {
        if(s.charAt(index)=='-'){
            Range r = findPart(s,index);
            if(r.start==0 || !(""+s.charAt(r.start-1)).matches("[\\d\\.]"))
                return true;
        }
        return false;
    }
    
    //returns a regular expression containing all of the operators
    private String getOperatorExpression(){
        String reg = "";
        for(Operation o:operators){
            if(!o.operator.matches("[\\w&&\\D]"))
                reg+="\\";
            reg+=o.operator;
        }
        return "["+reg+"]";
    }
    
    //returns the index of the matching parenthesis to the given parenthesis
    //direction functions the same as in findSubPart, must be -1 or 1 
    private int findMatchingParen(String s,int index,int dir){
        int parens = 0;
        for(int i=index;i!=Math.max(-1, (dir*s.length())%(s.length()+1));i+=dir)
        {
            char c = s.charAt(i);
            if(c=='(')
                parens+=dir;
            else if(c==')')
                parens-=dir;
            if(parens==0)
                return i;
        }
        return -1;
    }
    
    //see method name
    public boolean isNumber(String s){
        return s.matches("\\d+(\\.\\d+)?");
    }
    
    //replaces all variables in equation with given value 'x'
    private String substituteVariable(String s,double x)
    {
        if(s.contains(variable))
            s = s.replaceAll("["+variable+"]", "("+x+")");
        return s;
    }
    
    //replaces al constants with their respective values
    private String substituteConstants(String s)
    {
        for(Operation o: constants){
            if(s.contains(o.operator)){
                int start = s.indexOf(o.operator);
                int end = start+o.operator.length();
                s = s.substring(0, start) + o.execute(0, 0) + s.substring(end);
            }
        }
        return s;
    }
    
    //processes parenthesis pairs
    //given an equation, a range representing the parenthesis pair and value
    //computes the value of equation contained in the parenthesis pair
    private String reduce(String s,Range r,double x)
    {
        try{
            String temp = s.substring(0,r.start);
            if(r.start>0 && !Character.toString(s.charAt(r.start-1)).matches(getOperatorExpression()) &&
                            !containsFunction(s.substring(Math.max(r.start-3, 0), r.start)))
                temp += "*";
            temp += evaluate(s.substring(r.start+1, r.end),x);
            if(r.end<s.length()-1 && !Character.toString(s.charAt(r.end+1)).matches(getOperatorExpression())&&
                                     !containsFunction(s.substring(r.end+1, Math.min(r.end+3, s.length()))))
                temp += "*";
            temp += s.substring(r.end+1);
            return temp;
        }catch(Exception e){System.out.println("Parenthesis Mismatch in "+s);}
        return "";
    }
    
    //determines if a given string contains one of the predefined functions
    public boolean containsFunction(String s)
    {
        for(SingleOperation o: functions){
            if(s.contains(o.operator))
                return true;
        }
        return false;
    }
   
    //searches for the given operator and evaulates any found in the given equation
    //operators require two values to operate on
    //negatives are replaced if they are determined to be negating numbers rather than subtracting
    public String evaluateOperator(String eq,Operation o,double x)
    {
        try{
            while(eq.contains(o.operator)){
                //System.out.println(o.operator);
                Range range = findPart(eq,eq.indexOf(o.operator));
                String result;
                if(o.operator.equals("-") && eq.substring(range.start, range.end).indexOf(o.operator)==0)
                    result = "_"+eq.substring(range.start+1, range.end);
                else{
                    String reg = o.operator;
                    if(!o.operator.matches("[\\w&&\\D]"))
                        reg = "\\"+reg;
                    String[] numbers = eq.substring(range.start, range.end).split(reg);
                    result = o.execute(Double.valueOf(numbers[0]), Double.valueOf(numbers[1]));
                }
                eq = eq.substring(0, range.start) + result + eq.substring(range.end);
            }
            eq = eq.replaceAll("[_]", "-");
            return eq;
        }catch(Exception e){System.out.println("Failed to evaluate "+o.operator+ " in "+eq +"\n"+e);}
        return "";
    }
    
    //searches for the given function and evaluates any found in the equation
    //functions require one value to operate on
    public String evaluateFunction(String eq,SingleOperation o,double x)
    {
        try{
            while(eq.contains(o.operator)){
                //System.out.println(o.operator);
                int start = eq.indexOf(o.operator);
                int end = start+o.operator.length();
                if(o.operator.equals("!")){
                    Range value = new Range(findSubPart(eq,start,-1),start);
                    String result = o.execute(Double.valueOf(eq.substring(value.start, value.end)));
                    eq = eq.substring(0, value.start) + result + eq.substring(end);
                }
                else{
                    Range value = new Range(end,findSubPart(eq,end,1));
                    String result = o.execute(Double.valueOf(eq.substring(value.start, value.end+1)));
                    eq = eq.substring(0, start) + result + eq.substring(value.end+1);
                }
            }
            return eq;
        }catch(Exception e){System.out.println("Failed to evaluate function "+o.operator+" in "+eq);}
        return "";
        
    }
    
    //evaluates the given equation by searching and evaluating its various components
    public String evaluate(String eq,double x)
    {
        while(eq.contains("(")){
            Range range = new Range(eq.indexOf("("),findMatchingParen(eq,eq.indexOf("("),1)); 
            eq = reduce(eq,range,x);
        }
        for(SingleOperation o: functions)
            eq = evaluateFunction(eq,o,x);
        for(Operation o: operators)
            eq = evaluateOperator(eq,o,x);
        return eq;
    }
    
    
    public String f(int x){
        return f((double)x);
    }
    
    //attempts to solve the equation for the given value
    public String f(double x){
        String eq = equation;
        eq = substituteVariable(eq,x);
        eq = substituteConstants(eq);
        System.out.println(eq);
        return evaluate(eq,x);
    }
}

class Range{
    int start;
    int end;
    public Range(){
        this(0,0);
    }
    public Range(int s,int e){
        start = s;
        end = e;
    }
    public String toString(){
        return start+","+end;
    }
}