/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        Equation eq8 = new Equation("x+2y");
        Equation eq9 = new Equation();
        eq9.variables.add("<x>");
        eq9.setEquation("3<x>+xy");
        try{
            System.out.println(eq+" = "+eq.f(3));        
            System.out.println(eq2+" = "+eq2.f(2));
            for(int i=1;i<=6;i++)
                System.out.println(eq3+" = "+eq3.f(i));
            System.out.println(eq4+" = "+eq4.f(3));
            System.out.println(eq5+"         = "+eq5.f(-8));
            System.out.println(eq5v2+" = "+eq5v2.f(7));
            System.out.println(eq6+" = "+eq6.f(0));
            System.out.println(eq7+" = "+eq7.f(3));
            System.out.println(eq8+" = "+eq8.f(2,3));
            Map values = new HashMap();
            values.put("<x>", 2);
            values.put("x", 3);
            values.put("y", 4);
            System.out.println(eq9+" = "+eq9.f(values));
            
            Equation test = new Equation("max[2,2x,50+3,6,7,12,8]");       
            System.out.println(test+" = "+test.f(26));
            Equation test2 = new Equation("min[2,x,50+1,6,7,12,8,-max[2,2x,6,7,12,8]]");       
            System.out.println(test2+" = "+test2.f(10));
        }catch(Exception e){System.out.println(e);}
        
        
        Equation test = new Equation();
        int tests = 10000;
        long startTime = System.nanoTime();
        for(int i=0;i<tests;i++)
            test.setEquation("(csclnx*cotlnx)/(2x)");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.println("\nParsing Test");
        System.out.println(test+" => "+test.getParsedEquation());
        System.out.println(tests+" times in "+duration+" ms");
        
        
        try{
            double area = 0;
            double start = 0;
            double end = 10;
            double step = 0.001;
            Equation integral = new Equation("e^x");
            startTime = System.nanoTime();
            for(double i=start;i<=end;i+=step)
                area+=integral.fD(i);
            endTime = System.nanoTime();

            duration = (endTime - startTime)/1000000;
            System.out.println(String.format("\nArea of '%s' from %.1f to %.1f",integral,start,end));
            System.out.println("Area: "+area*step);
            System.out.println("Steps: "+(end-start)/step);
            System.out.println("Time: "+duration+" ms");
        }catch(Exception e){System.out.println(e);}        
    }
    
    String equation;
    private ArrayList<String> defaultVariable = new ArrayList(Arrays.asList("x","y","z"));
    private ArrayList<Operation> operators;
    private ArrayList<Function> functions;
    private ArrayList<Pair> pairs;
    private ArrayList parsedEquation;
    private StringParser sp;
    protected ArrayList<String> variables;
    public ArrayList extra;
    public ArrayList<String> illegalCharacters;
    
    public MathInterpreter(){
        variables = new ArrayList();
        variables.addAll(defaultVariable);
        sp = new StringParser(this);
        operators = new ArrayList();
        functions = new ArrayList();
        pairs = new ArrayList();
        parsedEquation = new ArrayList();
        extra = new ArrayList();
        illegalCharacters = new ArrayList();
        illegalCharacters.add("�");
        illegalCharacters.add("∞");
        illegalCharacters.add("NaN");
    }
    
    
    public String getEquation(){return equation;}
    
    //Sets and parses the given equation
    public void setEquation(String eq){
        equation = eq;
        parsedEquation = new ArrayList();
        parseEquation();
        //System.out.println(parsedEquation);
    }
    
    //returns the parsed equation
    //represents all individual components of <equation>
    public ArrayList getParsedEquation(){
        return (ArrayList)parsedEquation.clone();
    }
    
    //returns true/false if given string is an operator
    public boolean isValidOperation(String op){
        return getOperator(op)!=null;
    }
    
    //adds a operator, then sorts the list based on operator weight
    public void addOperation(Operation op){
        operators.add(op);
        operators.sort((x,y)->((Operation)x).weight-((Operation)y).weight);
    }
    
    //returns all operators
    public ArrayList<Operation> getOperations(){
        return operators;
    }
    
    //removes a specific operator
    public void removeOperator(String op){
        operators.removeIf(o->o.operator.equals(op));
    }
    
    //clears all the operators
    public void clearOperators(){
        operators = new ArrayList<Operation>();
    }
    
    //returns a Operation object for the given String
    public Operation getOperator(String op)
    {
        for(Operation o:getOperations())
            if(o.operator.equals(op))
                return o;
        return null;
    }
    
    //returns true/false if string is a function
    public boolean isValidFunction(String func){
        return getFunction(func)!=null;
    }
    
    //adds a functions, then sorts list by function weight
    public void addFunction(Function op){
        functions.add(op);
        functions.sort((x,y)->((Function)x).weight-((Function)y).weight);
    }
    
    //returns all functions
    public ArrayList<Function> getFunctions(){
        return functions;
    }
    
    //removes a specific function
    public void removeFunction(String func){
        functions.removeIf(o->o.name.equals(func));
    }
    
    //clears all function
    public void clearFunctions(){
        functions = new ArrayList<Function>();
    }
    
    //Returns a Function object for the given String
    public Function getFunction(String func)
    {
        for(Function o:functions)
            if(func.matches(o.regex))
                return o;
        return null;
    }
    
    //Adds a Pair then sorts the list by weights
    public void addPair(Pair p){
        pairs.add(p);
        pairs.sort((x,y)->((Pair)y).weight-((Pair)x).weight);
        
    }
    
    //returns an ArrayList of all Pairs
    public ArrayList<Pair> getPairs(){
        return pairs;
    }
    
    //removes a specific Pair
    public void removePair(String p){
        pairs.removeIf(o->p.matches(o.regex));
    }
    
    //clears all Pairs
    public void clearPairs(){
        pairs = new ArrayList<Pair>();
    }
    
    //return true/false if given string is a Pair
    public boolean isValidPair(String p){
        return getPair(p)!=null;
    }
    
    //Returns the Pair object for the given String
    //can be either side of the pair
    public Pair getPair(String p)
    {
        for(Pair o:pairs)
            if(o.left.equals(p)||o.right.equals(p))
                return o;
        return null;
    }
    
    //functions the same as the isNegativeNumber but handles ArrayLists
    //used when the negative sign and number are split
    protected boolean isNegativeNumber(ArrayList s,int index){
        if(((String)s.get(index)).equals("-"))
            if((index==0)  ||
                    ((index>0 && isValidOperation((String)s.get(index-1)) && getOperator((String)s.get(index-1)).inputSide!=Operation.LEFT) && 
                    (index<s.size() && isNumber((String)s.get(index+1)))))
                return true;
        return false;
    } 
                 
    //determines if a given string is a number
    public boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
    
    
    private ArrayList<String> getRelaventVariables(ArrayList eq){
        Set<String> temp = new HashSet();
        for(String part: (ArrayList<String>)eq)
            if(variables.contains(part))
                temp.add(part);
        ArrayList res = new ArrayList(temp);
        res.sort((x,y)->variables.indexOf(x)-variables.indexOf(y));
        return res;
    }
    
    //substitues all elements of the given ArrayList matching the predefined variable identifiers with the given values
    //Uses the values in terms of order of defined variables
    //You only need to provide values for the variables that are used
    //i.e. 'z+y' using {x,y,z} variables, example inputs are f(2,1) => '1+2'
    protected ArrayList subsituteVariables(ArrayList eq,ArrayList values)throws Exception{
        ArrayList<String> vars = getRelaventVariables(eq);
        for(String var: vars)
            while(eq.contains(var)){
                if(values.size()>vars.indexOf(var))
                    eq.set(eq.indexOf(var), String.valueOf(values.get(vars.indexOf(var))));
                else
                    throw new Exception("No value specified for variable '"+var+"'");
            }
        return eq;
    }
    
    protected ArrayList subsituteVariables(ArrayList eq,Map<String,String> values)throws Exception{
        ArrayList<String> vars = getRelaventVariables(eq);
        for(String var: values.keySet()){
            vars.removeIf(x->var.equals(x) && eq.contains(var));
            while(eq.contains(var))
                eq.set(eq.indexOf(var), String.valueOf(values.get(var)));
        }
        if(vars.size()>0)
            throw new Exception("No value specified for variable(s) "+vars);
        return eq;
    }
    
    private ArrayList getAll(){
        Set<String> hs = new HashSet<>();
        for(Operation o:(ArrayList<Operation>)getOperations())
            hs.add(o.toString());
        for(Function f: (ArrayList<Function>)getFunctions()){
            hs.add(f.name);hs.add(f.bounds.left);hs.add(f.bounds.right);hs.add(f.separator);
        }
        for(Pair p: (ArrayList<Pair>)getPairs()){
            hs.add(p.left);hs.add(p.right);
        }
        for(String s:(ArrayList<String>)extra)
            hs.add(s);
        hs.addAll(variables);
        return new ArrayList(hs);
    }
    
    public void parseEquation(){
        sp.tokens = getAll();
        parsedEquation = sp.parseString(equation);
        parsedEquation.removeIf(x->x.equals(" "));
        for(int i=0;i<parsedEquation.size();i++){
            String part = (String)parsedEquation.get(i);
            if(variables.contains(part)){
                parsedEquation.add(i+1, ")");
                parsedEquation.add(i, "(");
                i+=2;
            }
            else if(isValidOperation(part) && getOperator(part).inputSide==Operation.RIGHT){
                if(i>0 && isNumber((String)parsedEquation.get(i-1))){
                    parsedEquation.add(i,"*");
                    i+=1;
                }
            }
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
                return getOperator(s);
            else if(isValidFunction(s))
                return getFunction(s);
            else
                return s;
        }
        return null;
    }
    
    //evaluates the given equation <eq> for the given Pair and evaluates the inner scope of the Pair
    //Pair structure: <Pair bound> arguments <Pair bound>
    //i.e. (1+2) => 3
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
    
    
    //evaluates the given equation for functions separators and functions and evaluates each, then returns them in a list
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
    
    //evaluates the given function over the given equation <eq>
    //executes each functions with its corresponding arguments
    //function structure: <function name><function bound> arg1, arg2, ... , argN <function bound>
    //i.e. max[1,2,3,4] => 4
    protected ArrayList evaluateFunction(ArrayList eq,Function f)throws Exception{
        Pattern bounds = Pattern.compile(f.bounds.regex);
        while(eq.contains(f.name)){
            int index = eq.indexOf(f.name);
            if(bounds.matcher((String)eq.get(index+1)).matches()){
                int start = index+1;
                int end = findMatchingPair(eq,f.bounds,start);
                ArrayList part = evaluateFunctionParameters(new ArrayList(eq.subList(start+1, end)),f);
                try{eq = condense(eq,f.execute(part),new Range(index,end));}
                catch(Exception e){throw new Exception("Function '"+f.name+"' failed , Exception: "+e);}
            }else
                throw new Exception("Missing bounds '"+f.bounds+"' after '"+f.name+"' in '"+unparseEquation(eq)+"'");
        }
        return eq;
    }
    
    //evalutes the equation <eq> for the given Operation
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
                    throw new Exception("No possible senario found for '-' in '"+unparseEquation(eq)+"'");
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
            catch(Exception e){throw new Exception("Failed to execute '"+o+"' in '"+unparseEquation(eq)+"': "+e.getMessage());}
        }
        return eq;
    }
    
    private ArrayList getRelavent(ArrayList subject, ArrayList target){
        ArrayList res = (ArrayList)subject.clone();
        res.removeIf(x->!target.contains(x.toString()));
        return res;
    }
    
    private ArrayList<Task> getTasks(){
        ArrayList<Task> tasks = new ArrayList();
        tasks.add((Task)(x)->{
            for(Pair p: getPairs()){
                if(x.contains(p.left))
                    x = evaluatePair(x,p);
            }
            return x;
        });
        tasks.add((Task)(x)->{
            for(Function f: (ArrayList<Function>)getRelavent(getFunctions(),x)){
                if(x.contains(f.name))
                    x = evaluateFunction(x,f);
            }
            return x;
        });
        tasks.add((Task)(x)->{
            for(Operation o: (ArrayList<Operation>)getRelavent(getOperations(),x)){
                if(x.contains(o.operator))
                    x = evaluateOperator(x,o);
            }
            return x;
        });
        return tasks;
    }
    
    //attemps to evalute the given equation <eq> to completion, by evalutating all Pairs, Functions and Operations
    protected String evaluate(ArrayList eq)throws Exception
    {
        for(Task t: getTasks()){
            if(eq.size()==1)
                break;
            else
                eq = t.preform(eq);
        }
        if(eq.size()>0)
            if(!illegalCharacters.contains(((String)eq.get(0))))
                return (String)eq.get(0);
            else
                throw new Exception("Illegal Character: "+(String)eq.get(0));
        else
            return "";
    }
    
    //Used to evaluate the equation with the given arguments
    public String f(String... arguments)throws Exception{
        ArrayList eq = getParsedEquation();
        eq = subsituteVariables(eq,new ArrayList(Arrays.asList(arguments)));
        return evaluate(eq);
    }
    
    public String f(Map<String,String> arguments)throws Exception{
        ArrayList eq = getParsedEquation();
        eq = subsituteVariables(eq,arguments);
        return evaluate(eq);
    }
    
    //returns a string representation of the given equation
    public String unparseEquation(ArrayList eq){
        String res = "";
        for(String s: (ArrayList<String>)eq)
            res+=s;
        return res;
    }
    
    //returns the equation 
    public String toString(){
        return equation;
    }
}

interface Task
{
    public ArrayList preform(ArrayList a)throws Exception;
}

