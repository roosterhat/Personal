/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public class Parenthesis extends Pair{
    public Parenthesis(){
        super("(",")",5);
    }
    
    public Output processOperation(ArrayList<String> array, int index)throws Exception{
        Range r = getRange(array,index);
        ArrayList<String> result = execute(new ArrayList(array.subList(r.start+1,r.end)));
        String left = array.get(Math.max(r.start-1,0));
        String right = array.get(Math.min(r.end+1,array.size()-1));
        fixEquation(result,left,0,UniaryOperation.LEFT);
        fixEquation(result,right,result.size(),UniaryOperation.RIGHT);
        return new Output(result,r);
    }
    
    private void fixEquation(ArrayList<String> array, String part, int index, int side){
        if(_main.isValidOperation(part)){
            Operation o = _main.getOperation(part);
            if(o instanceof UniaryOperation)
                if(((UniaryOperation)o).inputSide==side)
                    array.add(index,"*");
        }
        else
            array.add(index,"*");
    }
    
    public ArrayList<String> execute(ArrayList<String> array)throws Exception{
        return _main.processEquation(array);
    } 
}

