/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mathinterpreter.Operation.Operation;
import mathinterpreter.Parser.StringParser;
import mathinterpreter.Util.ObjectInstance;
import mathinterpreter.Util.OperationInstance;
import mathinterpreter.Util.Range;

/**
 *
 * @author eriko
 */
public class Equation {
    final public ArrayList<String> defaultVariables = new ArrayList(Arrays.asList("x","y","z"));
    public ArrayList<String> illegalCharacters = new ArrayList(Arrays.asList("�","∞","NaN"));
    public ArrayList<String> variables;
    public ArrayList<String> extra;
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
        this.operations = new ArrayList();
        this.parser = new StringParser();
        this.extra = new ArrayList();
    }
    
    public Equation(String equation, ArrayList<Operation> operations){
        setEquation(equation);
        this.operations = operations;
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
        this.extra = new ArrayList();
    }
    
    public Equation(ArrayList<String> parsedEquation, ArrayList<Operation> operations){
        setEquation(parsedEquation);
        this.operations = operations;
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
        this.extra = new ArrayList();
    }
    
    public Equation(String equation, ArrayList<String> paredEquation, ArrayList objectEquation, ArrayList<ObjectInstance> SOE, ArrayList<Operation> operations){
        this.equation = equation;
        this.parsedEquation = paredEquation;
        this.objectEquation = objectEquation;
        this.segmentedObjectEquation = SOE;
        this.operations = operations;
        this.variables = new ArrayList(defaultVariables);
        this.parser = new StringParser();
        this.extra = new ArrayList();
    }
    
    public void reconstructEquation(){
        setEquation(equation);
    }
    
    private ArrayList<String> parseEquation(String equation){
        parser.tokens = getTokens();
        ArrayList parsedResult = parser.parseString(equation);
        parsedResult.removeIf(x->((String)x).replace(" ","").isEmpty());
        return parsedResult;
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
                if(obj!=null){
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
    
    private void resetEquation(){
        this.equation = "";
        this.parsedEquation = new ArrayList();
        this.objectEquation = new ArrayList();
        this.segmentedObjectEquation = new ArrayList();
    }
    
    public void setEquation(String equation){
        resetEquation();
        this.equation = equation;
        this.parsedEquation = parseEquation(equation);
        objectify(parsedEquation);
    }
    
    public void setEquation(ArrayList<String> parsedEquation){
        resetEquation();
        this.equation = String.join("", parsedEquation);
        this.parsedEquation = parsedEquation;
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
        hs.addAll(extra);
        return new ArrayList(hs);
    }
    
    public Equation split(Range range){
        Equation eq = new Equation();
        eq.operations = operations;
        eq.variables = variables;
        eq.parser = parser;
        eq.illegalCharacters = illegalCharacters;
        eq.setEquation((ArrayList<String>)new ArrayList(parsedEquation.subList(range.start, range.end+1)));
        return eq;
    }
    
    public Object clone(){
        Equation eq = new Equation();
        eq.operations = operations;
        variables.forEach(x->eq.variables.add(x));
        eq.parser = parser;
        illegalCharacters.forEach(x->eq.illegalCharacters.add(x));
        eq.setEquation(parsedEquation);
        return eq;
    }
    
    public String toString(){
        return equation;
    }
}