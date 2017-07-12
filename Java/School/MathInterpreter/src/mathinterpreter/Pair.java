/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public abstract class Pair  {
    
    String left;
    String right;
    String regex;
    int weight;
    FunctionInterface function;
    public Pair(String l, String r, int w, FunctionInterface f)
    {
        left = l;
        right = r;
        weight = w;
        function = f;
        regex = genRegex(l,r);
    }
    
    
    private String genRegex(String left,String right){
        return "(\\"+left+")|(\\"+right+")";
    }
    
    public ArrayList execute(ArrayList args){//[args]: inside, left, right
        return new ArrayList(args.subList(0, 1));
    }
    
    public String toString(){
        return left+right;
    }
}
