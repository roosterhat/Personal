/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author ostlinja
 */
public abstract class Pair  {
    
    String left;
    String right;
    String regex;
    int weight;
    protected PairFunctionInterface<ArrayList> internalFunction;
    protected PairFunctionInterface<ArrayList> externalFunction;
    public Pair(String l, String r, int w)
    {
        left = l;
        right = r;
        weight = w;
        regex = genRegex(l,r);
        internalFunction = x->x;
        externalFunction = x->{
            ArrayList res = new ArrayList();
            res.add(null);
            res.add(null);
            return res;
        };
    }
    
    
    private String genRegex(String left,String right){
        return "(\\"+left+")|(\\"+right+")";
    }
    
    public ArrayList executeInternal(ArrayList args)throws Exception{
        return internalFunction.execute(args);
    }
    
    public ArrayList executeExternal(ArrayList args)throws Exception{
        return externalFunction.execute(args);
    }
    
    public String toString(){
        return left+right;
    }
}
