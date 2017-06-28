/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public class StringParser {
    MathInterpreter _main;
    String numRegex = "\\-?\\d+(\\.\\d*)?";
    
    public StringParser(MathInterpreter m){
        _main = m;
    }
    
    //returns boolean whether or not the String starting at the given index is a negative number
    //has to start at the '-' character at the start of the number
    private boolean isNegativeNumber(String s,int index)
    {
        if(!s.equals(""))
            if(s.charAt(index)=='-')
                if(((index>0 && _main.isValidOperation(""+s.charAt(index-1)) && _main.getOperation(""+s.charAt(index-1)).inputSide!=Operation.LEFT) && 
                    (index<s.length() && isNumber(""+s.charAt(index+1)))))
                    return true;
        return false;
    }
    
    //determines if a given string is a number
    public boolean isNumber(String s){
        return s.matches(numRegex);
    }
    
    private ArrayList getAll(){
        ArrayList all = new ArrayList();
        for(Operation o:_main.getOperations())
            all.add(o.toString());
        for(Function f:_main.getFunctions()){
            all.add(f.name);all.add(f.bounds.left);all.add(f.bounds.right);all.add(f.separator);
        }
        for(Pair p:_main.getPairs()){
            all.add(p.left);all.add(p.right);
        }
        for(String s:(ArrayList<String>)_main.extra)
            all.add(s);
        all.add(_main.variable);
        Set<String> hs = new HashSet<>();
        hs.addAll(all);
        all.clear();
        all.addAll(hs);
        return all;
    }
    
    private ArrayList getRelevant(ArrayList a, String target){
        ArrayList temp = (ArrayList)a.clone();
        temp.removeIf(x->!target.contains((String)x));
        return temp;
    }
    
    private double probabilityOfMatch(String subject, String target){
        double prob = 0;
        double charval = 1.0/target.length();
        boolean consecutive = true;
        Pattern pattern = Pattern.compile("["+Pattern.quote(subject)+"]");
        for(char c:target.toCharArray()){
            if(pattern.matcher(String.valueOf(c)).matches()){
                if(consecutive)
                    prob+=charval;
                else{
                    prob+=charval/2;
                    consecutive = true;
                }
            }
            else
                consecutive = false;
        }
        return prob;
    } 
    
    private ArrayList getPossible(ArrayList all,String target){
        ArrayList temp = (ArrayList)all.clone();
        temp.removeIf(x->!target.matches(".*["+Pattern.quote((String)x)+"].*"));
        return temp;
    }
    
    private ArrayList getMostLikely(ArrayList all, String target,double margin){
        ArrayList<Double> res = new ArrayList();
        ArrayList possible = getPossible(all,target);
        double max = 0;
        for(String s: (ArrayList<String>)possible){
            double val = probabilityOfMatch(s,target);
            max = Math.max(max, val);
            res.add(val);
        }
        final double most = max;
        possible.removeIf(x-> res.get(possible.indexOf(x)) < most-margin);
        return possible;
    }
    
    private String compileRegex(ArrayList a){
        String exp = "["+_main.variable+"]";
        Pattern reserved = Pattern.compile("["+Pattern.quote("\\^$.|?*+/()[]{}")+"]");
        for(String x: (ArrayList<String>)a){
            if(reserved.matcher(x).matches())
                exp+="|"+Pattern.quote(x);
            else
                exp+="|"+x;
        }
        return exp;
    }
    
    //finds the next continuous part of the equation <s> starting at the given index
    //returns the index of the end of the part
    protected int findPart(String s,int index){
        String buffer = "";
        ArrayList all = getRelevant(getAll(),s);
        String expression = compileRegex(all);
        Pattern general = Pattern.compile(".*("+expression+")");
        Pattern number = Pattern.compile(numRegex+".+");
        
        for(int i=index;i<s.length();i++)
        {
            buffer+=s.charAt(i);
            if(!buffer.equals("") && general.matcher(buffer).matches())
            {
                buffer = buffer.replaceAll(" ", "");
                //if(!isNegativeNumber(s,i)){             
                    ArrayList most = getMostLikely(all,buffer,0);
                    if(number.matcher(buffer).matches()){
                        for(int x=1;x<=buffer.length();x++)
                            if(!buffer.substring(0,x).equals(""))
                                if(!isNumber(buffer.substring(0,x)))// && !isNegativeNumber(buffer.substring(0,x),x-1))
                                    return i-(buffer.length()-x);
                    }
                    else
                        for(String p: (ArrayList<String>)most){
                            if(buffer.matches(Pattern.quote(p)))
                                return i+1;
                        }
                //}
            }                
        }
        return s.length();
    }
}
