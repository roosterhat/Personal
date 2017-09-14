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
        super("(",")",5);
        externalFunction = x->
        {
            ArrayList res = new ArrayList();
            if(x.get(0) instanceof Operation && ((Operation)x.get(0)).inputSide==Operation.LEFT)
                res.add("*");
            else if(x.get(0) instanceof String)
                res.add("*");
            else
                res.add("");
            
            if(x.get(1) instanceof Operation && ((Operation)x.get(1)).inputSide==Operation.RIGHT)
                res.add("*");
            else if(x.get(1) instanceof String)
                res.add("*");
            else
                res.add("");
            
            return res;
        };
    }
}

