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
public class MathInterpreter {

    public static void main(String[] args) {
        Equation eq = new Equation("-4--(x!)");
        Equation eq2 = new Equation("(1+(cosx)^2)/(cosx)^2");
        Equation eq3 = new Equation("sin(pi/x)");
        eq3.setDecimalDepth(10);
        Equation eq4 = new Equation("(csclnx*cotlnx)/(2x)");
        Equation eq5 = new Equation("238Ex");
        Equation eq5v2 = new Equation("238*10^(-x-1)");
        eq5.setDecimalDepth(10);
        eq5v2.setDecimalDepth(10);
        Equation eq6 = new Equation("abs((5-15)/2)");
        Equation eq7 = new Equation("2^x^x");
        try{
            System.out.println(eq+" = "+eq.f(3));        
            System.out.println(eq2+" = "+eq2.f(2));
            for(int i=1;i<=6;i++)
                System.out.println(eq3+" = "+eq3.f(i));
            System.out.println(eq4+" = "+eq4.f(3));
            System.out.println(eq5+"         = "+eq5.f(-8));
            System.out.println(eq5v2+" = "+eq5v2.f(7));
            System.out.println(eq6+" = "+eq6.f(3));
            System.out.println(eq7+" = "+eq7.f(3));
            /*Equation test = new Equation("pow[2,x]");
            System.out.println(test.f(4)); 
            Equation test2 = new Equation("-max[2,2x,50+1,6,7,12,8]");       
            System.out.println(test2.f(26));*/
        }catch(Exception e){System.out.println(e);}
        
        try{
            double area = 0;
            double step = 0.001;
            Equation integral = new Equation("sinx");
            long startTime = System.nanoTime();
            for(double i=0;i<100;i+=step)
                area+=integral.fD(i);
            long endTime = System.nanoTime();

            long duration = (endTime - startTime)/1000000;
            System.out.println("Area: "+area*step);
            System.out.println("Time: "+duration);
        }catch(Exception e){System.out.println(e);}
        

       
        
    }
    
    String equation;
    protected String variable = "x";
    private ArrayList<Operation> operators;
    private ArrayList<Function> functions;
    private ArrayList<Pair> pairs;
    private ArrayList parsedEquation;
    public ArrayList extra;
    private StringParser sp;
    
    public MathInterpreter(){
        sp = new StringParser(this);
        operators = new ArrayList();
        functions = new ArrayList();
        pairs = new ArrayList();
        parsedEquation = new ArrayList();
        extra = new ArrayList();
    }
    
    public String getEquation(){return equation;}
    
    //Sets and parses the given equation
    public void setEquation(String eq){
        equation = eq;
        parsedEquation = new ArrayList();
        parseEquation(equation);
        System.out.println(parsedEquation);
    }
    
    public ArrayList getParsedEquation(){
        return (ArrayList)parsedEquation.clone();
    }
    
    public boolean isValidOperation(String op){
        for(Operation o:getOperations())
            if(o.operator.equals(op))
                return true;
        return false;
    }
    
    public void addOperation(Operation op){
        operators.add(op);
        operators.sort((x,y)->((Operation)x).weight-((Operation)y).weight);
    }
    
    public ArrayList<Operation> getOperations(){
        return operators;
    }
    
    public void removeOperator(String op){
        operators.removeIf(o->o.operator.equals(op));
    }
    
    public void clearOperators(){
        operators = new ArrayList<Operation>();
    }
    
    public Operation getOperation(String op)
    {
        for(Operation o:getOperations())
            if(o.operator.equals(op))
                return o;
        return null;
    }
    
    public boolean isValidFunction(String func){
        for(Function o:getFunctions())
            if(func.matches(o.regex))
                return true;
        return false;
    }
    
    public void addFunction(Function op){
        functions.add(op);
        functions.sort((x,y)->((Function)x).weight-((Function)y).weight);
    }
    
    public ArrayList<Function> getFunctions(){
        return functions;
    }
    
    public void removeFunction(String func){
        functions.removeIf(o->o.name.equals(func));
    }
    
    public void clearFunctions(){
        functions = new ArrayList<Function>();
    }
    
    public Function getFunction(String func)
    {
        for(Function o:functions)
            if(func.matches(o.regex))
                return o;
        return null;
    }
    
    
    public void addPair(Pair p){
        pairs.add(p);
        pairs.sort((x,y)->((Pair)y).weight-((Pair)x).weight);
        
    }
    
    public ArrayList<Pair> getPairs(){
        return pairs;
    }
    
    public void removePair(String p){
        pairs.removeIf(o->p.matches(o.regex));
    }
    
    public void clearPairs(){
        pairs = new ArrayList<Pair>();
    }
    
    public boolean isValidPair(String p){
        for(Pair o:getPairs())
            if(o.left.equals(p)||o.right.equals(p))
                return true;
        return false;
    }
    
    public Pair getPair(String p)
    {
        for(Pair o:pairs)
            if(o.left.equals(p)||o.right.equals(p))
                return o;
        return null;
    }
    
    
    //returns a regular expression containing all of the operators
    protected String getOperatorExpression(){
        String reg = "";
        for(Operation o:getOperations())
            reg+=o.regex+"|";
        return reg;
    }
    
    //returns a regular expression containing all of the pairs
    protected String getPairExpression(){
        String reg = "";
        for(Pair p:getPairs())
            reg+=p.regex+"|";
        return reg;
    }
    
    protected String getFunctionExpression(){
        String reg = "";
        for(Function f:getFunctions())
            reg+=f.regex+"|";
        return reg;
    }
    
    protected String getExtraExpression(){
        String reg = "";
        for(String s: (ArrayList<String>)extra)
            reg+="("+s+")|";
        return reg;
    }
    
    //functions the same as the isNegativeNumber but handles ArrayLists
    //used when the negative sign and number are split
    protected boolean isNegativeNumber(ArrayList s,int index){
        if(((String)s.get(index)).equals("-"))
            if((index==0)  ||
                    ((index>0 && isValidOperation((String)s.get(index-1)) && getOperation((String)s.get(index-1)).inputSide!=Operation.LEFT) && 
                    (index<s.size() && isNumber((String)s.get(index+1)))))
                return true;
        return false;
    } 
                 
    //determines if a given string is a number
    public boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
    
    //substitues all elements of the given ArrayList matching the predefined variable identifier with the given value
    public ArrayList subsituteVariable(ArrayList eq,double x){
        for(int i=1;i<eq.size();i++)
            if(((String)eq.get(i)).equals(variable))
                eq.set(i, String.valueOf(x));
        return eq;
    }
    
    //Parses the given equation into its component parts and stores them in parsedEquation
    public void parseEquation(String eq)
    {
        int pos = 0;
        int newpos = 0;
        while(pos<eq.length()){
            newpos = sp.findPart(eq,pos);
            String part = eq.substring(pos, newpos);
            part = part.replaceAll(" ", "");
            if(part.length()>0){
                if(part.equals(variable)){
                    parsedEquation.add("(");
                    parsedEquation.add(part);
                    parsedEquation.add(")");
                }
                else if(isValidOperation(part) && getOperation(part).inputSide==Operation.RIGHT){
                    if(parsedEquation.size()>0 && isNumber((String)parsedEquation.get(parsedEquation.size()-1))){
                        parsedEquation.add("*");
                        parsedEquation.add(part);
                    }
                    else{
                        parsedEquation.add(part);
                    }
                }
                else{
                    parsedEquation.add(part);
                }
            }
            pos = newpos;
        }
    }
    
    //finds the matching Pair to <p> in the ArrayList at the given index
    protected int findMatchingPair(ArrayList eq, Pair p, int index)throws Exception
    {
        int count = 0;
        Pattern pair = Pattern.compile(".*("+p.regex+").*");
        for(int i=index;i<eq.size();i++)
        {
            String part = (String)eq.get(i);
            if(pair.matcher(part).matches())
            {
                if(part.length()>1)
                    throw new Exception("Unknown symbole in '"+part+"' of '"+unparseEquation(eq)+"'");
                if(part.equals(p.right))
                    count--;
                else
                    count++;
            }
            if(count==0)
                return i;
        }
        throw new Exception("Failed to match pair '"+p+"' in '"+unparseEquation(eq)+"'");
    }
    
    //removes and replaces the elements of the ArrayList in the range with the given value
    protected ArrayList condense(ArrayList org, String val, Range r){
        ArrayList res = new ArrayList(org.subList(0,Math.max(r.start,0)));
        res.add(val);
        res.addAll(org.subList(Math.min(r.end+1,org.size()), org.size()));
        return  res;
    }
    
    //functions the sames as condense but replaces the range with an ArrayList of values
    protected ArrayList condense(ArrayList org, ArrayList vals, Range r){
        ArrayList res = new ArrayList(org.subList(0,Math.max(r.start,0)));
        res.addAll(vals);
        res.addAll(org.subList(Math.min(r.end+1,org.size()), org.size()));
        return  res;
    }
    
    //gets the Object in the given value and returns it as its true state
    //meaning returning numbers as Strings, operators as Operation, and pairs and Pairs or null if none are found
    protected Object getAdjacent(ArrayList eq,int index){
        if(index>=0 && index<eq.size()){
            String s = (String)eq.get(index);
            if(isValidOperation(s))
                return getOperation(s);
            else if(isValidFunction(s))
                return getFunction(s);
            else
                return s;
        }
        return null;
    }
    
    //evaluates the given equation for any pairs and evaluates the inner scope of the pair
    protected ArrayList evaluatePair(ArrayList eq,Pair p)throws Exception
    {
        Pattern pattern = Pattern.compile(p.regex);
        while(eq.contains(p.left)){
            int i = eq.indexOf(p.left);
            String part = (String)eq.get(i);
            if(isValidPair(part))
            {
                if(pattern.matcher(part).matches()){
                    int start = i;
                    int end = findMatchingPair(eq,p,i);                 

                    ArrayList temp = new ArrayList();
                    temp.add(evaluate(new ArrayList(eq.subList(start+1, end))));//inside
                    temp.add(getAdjacent(eq,start-1));//left
                    temp.add(getAdjacent(eq,end+1));//right

                    eq = condense(eq,p.execute(temp),new Range(start,end));
                }
            }
        }
        return eq;
    }
    
    private ArrayList functionNames(){
        ArrayList<String> res = new ArrayList();
        for(Function f: getFunctions())
            res.add(f.name);
        return res;
    }
    
    protected ArrayList evaluateFunctionParameters(ArrayList eq,Function f)throws Exception{
        ArrayList res = new ArrayList();
        ArrayList names = functionNames();
        int start = 0;
        int end = 0;
        for(int i=0;i<eq.size();i++){
            if(names.contains(eq.get(i))){
                start = i;
                end = findMatchingPair(eq,getFunction((String)eq.get(i)).bounds,start+1)+1;
                res.add(evaluate(new ArrayList(eq.subList(start, end))));
                i = end;
                start = end+1;
            }
            else if(eq.get(i).equals(f.separator)){
                end = i;
                res.add(evaluate(new ArrayList(eq.subList(start, end))));
                start = ++end;
            }
        }
        if(end!=eq.size())
            res.add(evaluate(new ArrayList(eq.subList(start, eq.size()))));
        return res;
    }
    
    protected ArrayList evaluateFunction(ArrayList eq,Function f)throws Exception{
        Pattern bounds = Pattern.compile(f.bounds.regex);
        while(eq.contains(f.name)){
            int index = eq.indexOf(f.name);
            if(bounds.matcher((String)eq.get(index+1)).matches()){
                int start = index+1;
                int end = findMatchingPair(eq,f.bounds,start);
                ArrayList part = evaluateFunctionParameters(new ArrayList(eq.subList(start+1, end)),f);
                eq = condense(eq,f.execute(part),new Range(index,end));
            }else
                throw new Exception("Missing bounds '"+f.bounds+"' after '"+f.name+"' in '"+unparseEquation(eq)+"'");
        }
        return eq;
    }
    
    //evalutes the equation for any of the given Operation
    protected ArrayList evaluateOperator(ArrayList eq,Operation o)throws Exception
    {
        while(eq.contains(o.operator)){
            int index = eq.indexOf(o.operator);
            if(isNegativeNumber(eq,index)){
                if(index<eq.size()-1){
                    if(isNumber((String)eq.get(index+1)))
                        eq = condense(eq,String.valueOf(-1*Double.valueOf((String)eq.get(index+1))),new Range(index,index+1));
                    else
                        eq = condense(eq,String.valueOf(-1*Double.valueOf(evaluate(new ArrayList(eq.subList(index+1,eq.size()))))),new Range(index,eq.size()));
                    continue;
                }
                else
                    throw new Exception("No Suitable senario found for '-' in '"+unparseEquation(eq)+"'");
            }              
            ArrayList parts = new ArrayList();
            Range r = new Range(index,index);
            if(o.inputSide<=Operation.BOTH){//LEFT
                if(index>0){
                    parts.add(evaluate(new ArrayList(eq.subList(0, index))));
                    r.start = 0;
                }else
                    throw new Exception("Missing left operand for '"+o+"' in '"+unparseEquation(eq)+"'");
            }
            if(o.inputSide>=Operation.BOTH){//RIGHT
                if(index<eq.size()-1){
                    parts.add(evaluate(new ArrayList(eq.subList(index+1, eq.size()))));
                    r.end = eq.size();
                }else
                    throw new Exception("Missing right operand for '"+o+"' in '"+unparseEquation(eq)+"'");
            }
            try{eq = condense(eq,o.execute(parts),r);}
            catch(Exception e){throw new Exception("Failed to execute '"+o+"' in '"+unparseEquation(eq)+"' "+e.getMessage());}
        }
        return eq;
    }
    
    //attemps to evalute the given equation to completion, by evalutating all Pairs and Operations
    protected String evaluate(ArrayList eq)throws Exception
    {
        ArrayList<Task> tasks = new ArrayList();
        tasks.add((Task)(x)->{
            for(Pair p: pairs){
                if(x.contains(p.left))
                    x = evaluatePair(x,p);
            }
            return x;
        });
        tasks.add((Task)(x)->{
            for(Function f: functions){
                if(x.contains(f.name))
                    x = evaluateFunction(x,f);
            }
            return x;
        });
        tasks.add((Task)(x)->{
            for(Operation o: operators){
                if(x.contains(o.operator))
                    x = evaluateOperator(x,o);
            }
            return x;
        });
        for(Task t: tasks){
            if(eq.size()==1)return (String)eq.get(0);
            else
                eq = t.preform(eq);
        }
        if(eq.size()>0)
            return (String)eq.get(0);
        else
            return "";
    }
    
    public String f(double x)throws Exception{
        ArrayList eq = getParsedEquation();
        eq = subsituteVariable(eq,x);
        return evaluate(eq);
    }
    
    public String unparseEquation(ArrayList eq){
        String res = "";
        for(String s: (ArrayList<String>)eq)
            res+=s;
        return res;
    }
    
    public String toString(){
        return equation;
    }
}

interface Task
{
    public ArrayList preform(ArrayList a)throws Exception;
}

