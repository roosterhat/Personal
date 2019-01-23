/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mathinterpreter.Operation.Operation;
import mathinterpreter.Parser.StringParser;

/**
 *
 * @author eriko
 */
public class Equation {
    final public ArrayList<String> defaultVariables = new ArrayList(Arrays.asList("x","y","z"));
    public ArrayList<String> illegalCharacters = new ArrayList(Arrays.asList("�","∞","NaN"));
    public ArrayList<String> variables;
    public String equation;
    public ArrayList<String> parsedEquation;
    public ArrayList objectEquation;
    public ArrayList<ObjectInstance> segmentedObjectEquation;
    public ArrayList<Operation> operations;
    private StringParser parser;
    
    
    public Equation(){
        this.equation = "";
        this.parsedEquation = new ArrayList();
        this.objectEquation = new ArrayList();
        this.segmentedObjectEquation = new ArrayList();
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
    }
    
    public Equation(String equation){
        setEquation(equation);
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
    }
    
    public Equation(ArrayList<String> parsedEquation){
        setEquation(parsedEquation);
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
    }
    
    public Equation(String equation, ArrayList<String> paredEquation, ArrayList objectEquation, ArrayList<ObjectInstance> SOE){
        this.equation = equation;
        this.parsedEquation = paredEquation;
        this.objectEquation = objectEquation;
        this.segmentedObjectEquation = SOE;
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
    }
    
    private ArrayList<String> parseEquation(String equation){
        parser.tokens = getTokens();
        ArrayList parsedResult = parser.parseString(equation);
        parsedResult.removeIf(x->((String)x).replace(" ","").isEmpty());
        return parsedResult;
    }
    
    private ArrayList createObjectEquation(ArrayList<String> parsedEquation){
        ArrayList result = new ArrayList(parsedEquation.size());
        for(String s : parsedEquation){
            Operation o = getOperation(s);
            result.add(o==null ? s : o);
        }
        return result;
    }
    
    private ArrayList<ObjectInstance> segmentObjectEquation(ArrayList objectEquation){
        ArrayList<ObjectInstance> segmentedEquation = new ArrayList();
        for(int i = 0; i < objectEquation.size(); i++){
            Object o = objectEquation.get(i);
            if(o instanceof Operation){
                Operation op = (Operation)o;
                Range range = op.findRange(objectEquation, i);
                segmentedEquation.add(new OperationInstance(op,range));
                i += range.length()+1;
            }
            else
                segmentedEquation.add(new ObjectInstance(o,new Range(i,i)));
        }
        return segmentedEquation;
    }
    
    private void objectify(ArrayList<String> parsedEquation){
        objectEquation = new ArrayList();
        segmentedObjectEquation = new ArrayList();
        int marker = 0;
        for(int i = 0; i < parsedEquation.size(); i++){
            String part = parsedEquation.get(i);
            Operation obj = getOperation(part);
            objectEquation.add(obj==null ? part : obj);
            if(marker <= i){
                if(obj instanceof Operation){
                    Operation op = (Operation)obj;
                    Range range = op.findRange(parsedEquation, i);
                    segmentedObjectEquation.add(new OperationInstance(op,range));
                    marker = i + range.length() + 1;
                }
                else
                    segmentedObjectEquation.add(new ObjectInstance(obj,new Range(i,i)));
            }
        }
    }
    
    public void setEquation(String equation){
        this.equation = equation;
        this.parsedEquation = parseEquation(equation);
        //this.objectEquation = createObjectEquation(parsedEquation);
        //this.segmentedObjectEquation = segmentObjectEquation(parsedEquation);
        objectify(parsedEquation);
    }
    
    public void setEquation(ArrayList<String> parsedEquation){
        this.equation = String.join("", parsedEquation);
        this.parsedEquation = parsedEquation;
        //this.objectEquation = createObjectEquation(parsedEquation);
        //this.segmentedObjectEquation = segmentObjectEquation(parsedEquation);
        objectify(parsedEquation);
    }
    
    public ArrayList<String> getRelaventVariables(){
        Set<String> temp = new HashSet();
        for(String part: parsedEquation)
            if(variables.contains(part))
                temp.add(part);
        ArrayList res = new ArrayList(temp);
        res.sort((x,y)->variables.indexOf(x)-variables.indexOf(y));
        return res;
    }
    
    public ArrayList<String> getRelaventVariables(ArrayList<String> eq){
        Set<String> temp = new HashSet();
        for(String part: eq)
            if(variables.contains(part))
                temp.add(part);
        ArrayList res = new ArrayList(temp);
        res.sort((x,y)->variables.indexOf(x)-variables.indexOf(y));
        return res;
    }
    
    public ArrayList<Operation> getOperations(){
        return operations;
    }
    
    public Operation getOperation(String op){
        for(Operation o:getOperations())
            if(o.equals(op))
                return o;
        return null;
    }
    
    public boolean isValidOperation(String op){
        return getOperation(op)!=null;
    }
    
    public void addOperation(Operation op){
        operations.add(op);
    }
    
    public void removeOperation(String op){
        operations.removeIf(o->o.operator.equals(op));
    }
    
    private ArrayList getTokens(){
        Set<String> hs = new HashSet();
        for(Operation o: operations)
            hs.addAll(o.getTokens());
        hs.addAll(variables);
        return new ArrayList(hs);
    }
    
    public Equation split(Range range){
        Equation eq = new Equation((ArrayList<String>)new ArrayList(parsedEquation.subList(range.start, range.end)));
        eq.operations = operations;
        eq.variables = variables;
        eq.parser = parser;
        eq.illegalCharacters = illegalCharacters;
        return eq;
    }
    
    public Equation clone(){
        Equation eq = new Equation(equation, parsedEquation, objectEquation, segmentedObjectEquation);
        eq.operations = operations;
        eq.variables = variables;
        eq.parser = parser;
        eq.illegalCharacters = illegalCharacters;
        return eq;
    }
    
    public String toString(){
        return equation;
    }
}