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
public class BasicParenthesis extends Pair{
    public BasicParenthesis(){
        super("(",")",5,(BasicParenFunction)x->{return (ArrayList)x.subList(0, 0);});
    }
}

interface BasicParenFunction extends FunctionInterface{
    ArrayList execute(ArrayList a);
}
