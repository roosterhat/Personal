/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import mathinterpreter.Parser.StringParser;
import mathinterpreter.Operation.Function;
import mathinterpreter.Operation.Pair;
import mathinterpreter.Operation.Operation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mathinterpreter.Util.BinaryTree;
import mathinterpreter.Util.Entry;
import mathinterpreter.Util.Output;

/**
 *
 * @author ostlinja
 */
public abstract class MathInterpreter {
    protected String equation;
    final private ArrayList<String> defaultVariable = new ArrayList(Arrays.asList("x","y","z"));
    protected ArrayList<Operation> operations;
    protected ArrayList<String> parsedEquation;
    protected StringParser sp;
    public ArrayList<String> variables;
    public ArrayList extra;
    public ArrayList<String> illegalCharacters;
    private BinaryTree<Entry<Operation>> operationTree;
    
    public MathInterpreter(){
        variables = new ArrayList(defaultVariable);
        sp = new StringParser();
        operations = new ArrayList();
        parsedEquation = new ArrayList();
        extra = new ArrayList();
        illegalCharacters = new ArrayList();
        illegalCharacters.add("�");
        illegalCharacters.add("∞");
        illegalCharacters.add("NaN");
        operationTree = new BinaryTree();
        operationTree.setComparator((x,y)->(((Entry<Operation>)y).value.weight-((Entry<Operation>)x).value.weight));
        
    }
    
    public String getEquation(){return equation;}
    
    public void setEquation(String eq){
        equation = eq;
        parsedEquation = new ArrayList();
        parseEquation();
    }
    
    public ArrayList getParsedEquation(){
        return parsedEquation;
    }
    
    public boolean isValidOperation(String op){
        return getOperation(op)!=null;
    }
    
    public void addOperation(Operation op){
        op.setMain(this);
        operations.add(op);
    }
    
    public ArrayList<Operation> getOperations(){
        return operations;
    }
    
    public void removeOperation(String op){
        operations.removeIf(o->o.operator.equals(op));
    }
   
    public Operation getOperation(String op){
        for(Operation o:getOperations())
            if(o.equals(op))
                return o;
        return null;
    }
    
    public boolean isValidFunction(String func){
        return getOperation(func) instanceof Function;
    }
    
    public ArrayList<Function> getFunctions(){
        ArrayList<Function> results = new ArrayList();
        for(Operation o: operations)
            if(o instanceof Function)
                results.add((Function)o);
        return results;
    }   
    
    public ArrayList<Pair> getPairs(){
        ArrayList<Pair> results = new ArrayList();
        for(Operation o: operations)
            if(o instanceof Pair)
                results.add((Pair)o);
        return results;
    }
    
    public boolean isValidPair(String p){
        return getOperation(p) instanceof Pair;
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
    
    private ArrayList subsituteVariables(ArrayList eq, Map<String,String> values)throws Exception{
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
    
    private ArrayList getTokens(){
        Set<String> hs = new HashSet();
        for(Operation o: operations)
            hs.addAll(o.getTokens());
        hs.addAll(variables);
        return new ArrayList(hs);
    }
    
    public void parseEquation(){
        sp.tokens = getTokens();
        parsedEquation = sp.parseString(equation);
        parsedEquation.removeIf(x->((String)x).replace(" ","").isEmpty());
    }
    
    protected ArrayList<String> preProcessEquation(ArrayList<String> array){
        return array;
    }
    
    public ArrayList<String> processEquation(ArrayList<String> array)throws Exception{
        return evaluate(array);
    }
    
    protected ArrayList<String> postProcessEquation(ArrayList<String> array){
        return array;
    }
    
    protected BinaryTree<Entry<Operation>> createOperationStack(ArrayList<String> array){
        operationTree.clear();
        int pairCount = 0;
        for(int i = 0;i<array.size();i++){
            String s = array.get(i);
            if(isValidOperation(s)){
                Operation o = getOperation(s);
                if(pairCount<=0)
                    operationTree.add(new Entry(o,i));
                if(o instanceof Pair)
                    pairCount += s.equals(((Pair)o).open)?1:-1;
            }
        }
        return operationTree;
    }

    public ArrayList<String> condense(ArrayList<String> array, Output output){
        ArrayList<String> temp = new ArrayList(array.subList(0, output.range.start));
        if(output.value instanceof ArrayList)
            temp.addAll((ArrayList<String>)output.value);
        else
            temp.add((String)output.value);
        temp.addAll(array.subList(output.range.end+1, array.size()));
        return temp;
    }
    
    //attemps to evalute the given equation <eq> to completion, by evalutating all Pairs, Functions and Operations
    protected ArrayList<String> evaluate(ArrayList<String> array)throws Exception{
        ArrayList<Entry<Operation>> operationStack;
        ArrayList<String> eq = (ArrayList<String>)array.clone();
        while(!(operationStack = createOperationStack(eq).inorder()).isEmpty()){
            Entry<Operation> e = operationStack.get(0);
            Output result = e.value.processOperation(eq, e.index);
            eq = condense(eq,result);
        }
        return eq;
    }
    
    
    //Used to evaluate the equation with the given arguments
    public String f(String... arguments)throws Exception{
        return f(convertArguments(arguments));
    }
    
    private Map<String, String> convertArguments(String[] args){
        Map<String, String> vars = new HashMap();
        ArrayList<String> relaventVars = getRelaventVariables(sp.parseString(equation));
        for(int i=0;i<relaventVars.size();i++)
            if(i<args.length)
                vars.put(relaventVars.get(i), args[i]);
        return vars;
    }
    
    public String f(Map<String,String> arguments)throws Exception{
        ArrayList eq = (ArrayList<String>)getParsedEquation().clone();
        eq = preProcessEquation(eq);
        eq = subsituteVariables(eq,arguments);
        eq = processEquation(eq);
        eq = postProcessEquation(eq);
        return compileArray(eq);
    }
    
    public static String compileArray(ArrayList<String> array){
        String result = "";
        for(String s: array)
            result += s;
        return result;
    }
    
    public String toString(){
        return equation;
    }
}
