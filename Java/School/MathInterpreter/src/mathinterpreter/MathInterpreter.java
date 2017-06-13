/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.Comparator;
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
        Equation eq5 = new Equation("1Ex");
        Equation eq6 = new Equation("abs((5-15)/2)");
        System.out.println(eq+" = "+eq.f(3));
        System.out.println(eq2+" = "+eq2.f(2));
        for(int i=1;i<=6;i++)
            System.out.println(eq3+" = "+eq3.f(i));
        System.out.println(eq4+" = "+eq4.f(3));
        System.out.println(eq5+" = "+eq5.f(-3));
        System.out.println(eq6+" = "+eq6.f(3));
    }
    
    String equation;
    private String variable = "x";
    private ArrayList<Operation> operators;
    private ArrayList<Pair> pairs;
    private ArrayList parsedEquation;
    
    public MathInterpreter(String s){
        operators = new ArrayList();
        pairs = new ArrayList();
        parsedEquation = new ArrayList();
        equation = s;
    }
    
    public String getEquation(){return equation;}
    
    public void setEquation(String eq){
        equation = eq;
        parseEquation(equation);
    }
    
    public ArrayList getParsedEquation(){
        return (ArrayList)parsedEquation.clone();
    }
    
    public boolean isValidOperator(String op){
        for(Operation o:getOperators())
            if(o.operator.equals(op))
                return true;
        return false;
    }
    
    public void addOperation(Operation op){
        if(!op.operator.matches(Pattern.quote("()"))){
            operators.add(op);
            operators.sort((x,y)->((Operation)x).weight-((Operation)y).weight);
        }
    }
    
    public ArrayList<Operation> getOperators(){
        return operators;
    }
    
    public void removeOperator(String op){
        operators.removeIf(o->o.operator.equals(op));
    }
    
    public void clearOperators(){
        operators = new ArrayList<Operation>();
    }
    
    private Operation getOperation(String op)
    {
        for(Operation o:operators)
            if(o.operator.equals(op))
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
    
    private Pair getPair(String p)
    {
        for(Pair o:pairs)
            if(o.left.equals(p)||o.right.equals(p))
                return o;
        return null;
    }
    
    
    //returns a regular expression containing all of the operators
    private String getOperatorExpression(){
        String reg = "";
        for(Operation o:getOperators())
            reg+=o.regex+"|";
        return reg.substring(0,reg.length()-1);
    }
    
    //returns a regular expression containing all of the pairs
    private String getPairExpression(){
        String reg = "";
        for(Pair p:getPairs())
            reg+=p.regex+"|";
        return reg.substring(0,reg.length()-1);
    }
    
    //returns boolean whether or not the String starting at the given index is a negative number
    //has to start at the '-' at the start of the number
    private boolean isNegativeNumber(String s,int index)
    {
        if(s.charAt(index)=='-')
            if(index==0 || !isNumber(""+s.charAt(index-1)))
                return true;
        return false;
    }
    
    //functions the same as the isNegativeNumber but handles ArrayLists
    //used when the negative sign and number are split
    private boolean isNegativeNumber(ArrayList s,int index){
        if(((String)s.get(index)).equals("-"))
            if(index==0 || !isNumber((String)s.get(index-1)))
                return true;
        return false;
    }
    
    //finds the next continuous part of the equation <s> starting at the given index
    private int findPart(String s,int index){
        String buffer = "";
        String exp = getOperatorExpression()+"|"+getPairExpression()+"|["+variable+"]";
        for(int i=index;i<s.length();i++)
        {
            buffer+=s.charAt(i);
            if(!buffer.equals("") && buffer.matches(".*("+exp+")"))
            {
                if(!isNegativeNumber(s,i)){
                    for(int x=0;x<=i;x++)
                        if(buffer.substring(x).matches(exp))
                        {
                            if(x==0)
                                return i+1;
                            else
                                return i-(buffer.length()-x)+1;
                        }
                }
            }                
        }
        return s.length();
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
        String exp = getOperatorExpression()+getPairExpression();
        int pos = 0;
        int newpos = 0;
        while(pos<eq.length()){
            newpos = findPart(eq,pos);
            String part = eq.substring(pos, newpos);
            if(!part.contains(" ")){
                if(part.equals(variable)){
                    parsedEquation.add("(");
                    parsedEquation.add(part);
                    parsedEquation.add(")");
                }
                else{
                    parsedEquation.add(part);
                }
            }
            pos = newpos;
        }
    }
    
    //finds the matching Pair to <p> in the ArrayList at the given index
    public int findMatchingPair(ArrayList eq, Pair p, int index){
        int count = 0;
        String exp = getPairExpression();
        for(int i=index;i<eq.size();i++)
        {
            String part = (String)eq.get(i);
            if(part.matches(p.regex))
            {
                if(part.equals(p.right))
                    count--;
                else
                    count++;
            }
            if(count==0)
                return i;
        }
        return -1;
    }
    
    //removes and replaces the elements of the ArrayList in the range with the given value
    public ArrayList condense(ArrayList org, String val, Range r){
        ArrayList res = new ArrayList(org.subList(0,Math.max(r.start-1,0)));
        res.add(val);
        res.addAll(org.subList(Math.min(r.end+1,org.size()), org.size()));
        return  res;
    }
    
    //functions the sames as condense but replaces the range with an ArrayList of values
    public ArrayList condense(ArrayList org, ArrayList vals, Range r){
        ArrayList res = new ArrayList(org.subList(0,Math.max(r.start-1,0)));
        res.addAll(vals);
        res.addAll(org.subList(Math.min(r.end+1,org.size()), org.size()));
        return  res;
    }
    
    //gets the Object in the given value and returns it as its true state
    //meaning returning numbers as Strings, operators as Operation, and pairs and Pairs
    private Object getAdjacent(ArrayList eq,int index){
        if(index>0 && index<eq.size()){
            String s = (String)eq.get(index);
            if(isValidOperator(s))
                return getOperation(s);
            else
                return s;
        }
        return null;
    }
    
    //evaluates the given equation for any pairs and evaluates the inner scope of the pair
    public ArrayList evaluatePair(ArrayList eq,Pair p){
        for(int i=0;i<eq.size();i++){
            String part = (String)eq.get(i);
            if(isValidPair(part))
            {
                if(part.matches(p.regex)){
                    int start = i+1;
                    int end = findMatchingPair(eq,p,i);                 
     
                    ArrayList temp = new ArrayList();
                    temp.add(evaluate(new ArrayList(eq.subList(start, end))));//inside
                    temp.add(getAdjacent(eq,start-2));//left
                    temp.add(getAdjacent(eq,end+1));//right
                    
                    eq = condense(eq,p.execute(temp),new Range(start,end));
                }
            }
        }
        return eq;
    }
    
    //evalutes the equation for any of the given Operation
    public ArrayList evaluateOperator(ArrayList eq,Operation o)
    {
        while(eq.contains(o.operator)){
            int index = eq.indexOf(o.operator);
            if(isNegativeNumber(eq,index)){
                eq = condense(eq,String.valueOf(-1*Double.valueOf((String)eq.get(index+1))),new Range(index,index+1));
                continue;
            }              
            ArrayList parts = new ArrayList();
            Range r = new Range(index,index);
            if(o.inputSide<=0 && index>0){//LEFT
                parts.add(evaluate(new ArrayList(eq.subList(0, index))));
                r.start = 0;
            }
            if(o.inputSide>=0 && index<eq.size()-1){//RIGHT
                parts.add(evaluate(new ArrayList(eq.subList(index+1, eq.size()))));
                r.end = eq.size();
            }
            eq = condense(eq,o.execute(parts),r);
        }
        return eq;
    }
    
    //attemps to evalute the given equation to completion, by evalutating all Pairs and Operations
    public String evaluate(ArrayList eq)
    {
        if(eq.size()==1 && isNumber((String)eq.get(0)))
            return (String)eq.get(0);
        for(Pair p: pairs){
            try{eq = evaluatePair(eq,p);}
            catch(Exception e){System.out.println(e+"\nFailed to evaluate Pair: '"+p+"' in '"+unparseEquation(eq)+"'"); return"";}
        }
        for(Operation o: operators){
            try{eq = evaluateOperator(eq,o);}
            catch(Exception e){System.out.println(e+"\nFailed to evaluate Operation: '"+o+"' in '"+unparseEquation(eq)+"'"); return"";}
        }
        
        if(eq.size()>0)
            return (String)eq.get(0);
        else
            return "";
    }
    
    public String f(double x){
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
