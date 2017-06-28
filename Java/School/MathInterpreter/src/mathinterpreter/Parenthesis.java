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
public class Parenthesis extends Pair{
    public Parenthesis(){
        super("(",")",5,(ParenFunction)x->{
            ArrayList res = new ArrayList();
            if(x.get(1) instanceof Operation){
               if(((Operation)x.get(1)).inputSide==Operation.LEFT)
                   res.add("*");
            }
            else if(x.get(1) instanceof String)
                res.add("*");
            res.add(x.get(0));
            if(x.get(2) instanceof Operation){
               if(((Operation)x.get(2)).inputSide==Operation.RIGHT)
                   res.add("*");
            }
            else if(x.get(2) instanceof String)
                res.add("*");
            return res;
        });
    }
    
    public ArrayList execute(ArrayList a){
        return ((ParenFunction)function).execute(a);
    }
}

interface ParenFunction extends FunctionInterface{
    ArrayList execute(ArrayList a);
}
