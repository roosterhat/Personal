/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import mathinterpreter.Operation.Operation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mathinterpreter.Util.BinaryTree;
import mathinterpreter.Util.Entry;
import mathinterpreter.Util.Equation;
import mathinterpreter.Util.ObjectInstance;
import mathinterpreter.Util.OperationInstance;
import mathinterpreter.Util.Output;

/**
 *
 * @author ostlinja
 */
public class Interpreter {
    private static BinaryTree<Entry<Operation>> operationTree;
    public Equation equation;
    
    public Interpreter(){
        equation = new Equation();
        operationTree = new BinaryTree();
        operationTree.setComparator((x,y)->(((Entry<Operation>)y).value.weight-((Entry<Operation>)x).value.weight));
    }
    
    public Interpreter(Equation equation){
        this.equation = equation;
        operationTree = new BinaryTree();
        operationTree.setComparator((x,y)->(((Entry<Operation>)y).value.weight-((Entry<Operation>)x).value.weight));
    }
    
    public Equation getEquation(){return equation;}    
    
    private void subsituteVariables(Equation eq, Map<String,String> values)throws Exception{
        ArrayList<String> vars = eq.getRelaventVariables();
        ArrayList<String> temp = (ArrayList<String>)eq.parsedEquation.clone();
        for(String var: values.keySet()){
            vars.removeIf(x->var.equals(x) && temp.contains(var));
            while(temp.contains(var))
                temp.set(temp.indexOf(var), String.valueOf(values.get(var)));
        }
        if(vars.size()>0)
            throw new Exception("No value specified for variable(s) "+vars);
        eq.setEquation(temp);
    }
        
    protected void preProcessEquation(Equation equation){
    }
    
    public void processEquation(Equation equation)throws Exception{
        evaluate(equation);
    }
    
    protected void postProcessEquation(Equation equation){
    }
    
    public static BinaryTree<Entry<Operation>> createBinaryOperationTree(Equation equation){
        operationTree.clear();
        for(ObjectInstance o : equation.segmentedObjectEquation)
            if(o instanceof OperationInstance)
                    operationTree.add(new Entry(((OperationInstance) o).object, ((OperationInstance) o).range.start));
        return operationTree;
    }

    public static void condense(Equation equation, Output output){
        ArrayList<String> temp = new ArrayList(equation.parsedEquation.subList(0, output.range.start));
        if(output.value instanceof ArrayList)
            temp.addAll((ArrayList<String>)output.value);
        else
            temp.add((String)output.value);
        temp.addAll(equation.parsedEquation.subList(output.range.end+1, equation.parsedEquation.size()));
        equation.setEquation(temp);
    }
    
    //attemps to evalute the given equation <eq> to completion, by evalutating all Pairs, Functions and Operations
    public static Equation evaluate(Equation eq)throws Exception{
        ArrayList<Entry<Operation>> operationStack;
        while(!(operationStack = createBinaryOperationTree(eq).inorder()).isEmpty()){
            Entry<Operation> e = operationStack.get(0);
            Output result = e.value.processOperation(eq, e.index);
            condense(eq,result);
        }
        return eq;
    }
    
    //Used to evaluate the equation with the given arguments
    public String interpret(String... arguments)throws Exception{
        return interpret(convertArguments(arguments));
    }
    
    private Map<String, String> convertArguments(String[] args){
        Map<String, String> vars = new HashMap();
        ArrayList<String> relaventVars = equation.getRelaventVariables();
        for(int i=0;i<Math.min(args.length,relaventVars.size());i++)
            vars.put(relaventVars.get(i), args[i]);
        return vars;
    }
    
    public String interpret(Map<String,String> arguments)throws Exception{
        Equation eq = equation.clone();
        preProcessEquation(eq);
        subsituteVariables(eq,arguments);
        processEquation(eq);
        postProcessEquation(eq);
        return eq.toString();
    }
    
    public String toString(){
        return equation.equation;
    }
}


