/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import mathinterpreter.Util.Range;

/**
 *
 * @author eriko
 */
public class FunctionPair extends Pair{
    String seperator;
    public FunctionPair(String seperator){
        super("[","]",5);
        this.seperator = seperator;
    } 
    
    public ArrayList<String> execute(ArrayList<String> array)throws Exception{
        return evaluateContents(getFunctionContents(array));
    } 
    
    private ArrayList<String> evaluateContents(ArrayList<ArrayList<String>> contents)throws Exception{
        ArrayList<String> results = new ArrayList();
        for(ArrayList<String> x : contents)
            results.addAll(_main.processEquation(x));
        return results;
    }
    
    private ArrayList<ArrayList<String>> getFunctionContents(ArrayList<String> array){
        ArrayList<ArrayList<String>> results = new ArrayList();
        int depth = 0;
        Range r = new Range(0,0);
        for(String s: array){
            r.end++;
            if(_main.isValidFunction(s) && !s.equals(seperator))
                depth++;
            if(_main.isValidPair(s)){
                Pair p = (Pair)_main.getOperation(s);
                if(p instanceof FunctionPair && ((FunctionPair)p).close.equals(s))
                    depth--;
            }
            if(s.equals(seperator) && depth==0){
                results.add(new ArrayList(array.subList(r.start, r.end-1)));
                r.start = r.end;
            }
        }
        results.add(new ArrayList(array.subList(r.start, r.end)));
        return results;
    }
    
}
