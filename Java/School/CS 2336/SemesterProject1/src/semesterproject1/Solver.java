/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject1;

import mathinterpreter.Equation;
import mathinterpreter.Operation.BinaryOperation;
import mathinterpreter.Operation.Converter;
import mathinterpreter.Operation.BasicParenthesis;

/**
 *
 * @author eriko
 */
public class Solver extends Equation{
    public Solver(){
        setOperations();
        addOperation(new BasicParenthesis());
    }
    
    public void setOperations(){
        Converter lc = x->Long.valueOf(x);
        addOperation(new BinaryOperation<Long>("Mod",1,(x,y)->String.valueOf(x%y),lc));
    }
    
    public void setEquation(String eq){
        super.setEquation(eq.replaceAll(" ", ""));
    }
    
    public long fL()throws Exception{
        return Long.valueOf(f("").replaceAll(" ", ""));
    }
}
