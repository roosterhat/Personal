/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import mathinterpreter.Operation.BasicParenthesis;
import mathinterpreter.Operation.UniaryOperation;
import mathinterpreter.Operation.BinaryOperation;

/**
 *
 * @author ostlinja
 */
public class LogicEquation extends MathInterpreter{
    public static void main(String[] args) {
//        LogicEquation eq = new LogicEquation("~((x&y)|A)");
//        try{System.out.println(eq.f(true,true,false));}catch(Exception e){System.out.println(e.getMessage());}
    }
    public LogicEquation()
    {
        this("");
    }
    public LogicEquation(String eq)
    {
        super();
        variables.clear();
//        for(char c='a';c<='z';c++)
//            variables.add(String.valueOf(c));
//        for(char c='A';c<='Z';c++)
//            variables.add(String.valueOf(c));
        addOperation(new BasicParenthesis());
        setOperations();   
        setEquation(eq);
    }
    
    public void setEquation(String eq){
        super.setEquation(eq);
        for(String s: parsedEquation)
            if(!isValidOperation(s))
                variables.add(s);
    }
    
    public void setOperations(){
        addOperation(new BinaryOperation<Boolean>("&",0,(x,y)->{
            return String.valueOf(x && y);
        },x->Boolean.valueOf(x)));
        addOperation(new BinaryOperation<Boolean>("|",0,(x,y)->{
            return String.valueOf(x || y);
        },x->Boolean.valueOf(x)));
        addOperation(new BinaryOperation<Boolean>("xor",0,(x,y)->{
            return String.valueOf(!(x==y));
        },x->Boolean.valueOf(x)));
        addOperation(new BinaryOperation<Boolean>("bi",0,(x,y)->{
            return String.valueOf(x==y);
        },x->Boolean.valueOf(x)));
        addOperation(new BinaryOperation<Boolean>("->",0,(x,y)->{
            return String.valueOf(!(x&&!y));
        },x->Boolean.valueOf(x)));
        addOperation(new UniaryOperation<Boolean>("~",0,UniaryOperation.RIGHT,x->{
            return String.valueOf(!x);
        },x->Boolean.valueOf(x)));
    }
    
    public String f(Boolean... args)throws Exception{
        String[] res = new String[args.length];
        for(int i=0;i<args.length;i++)
            res[i] = String.valueOf(args[i]);
        return f(res);
    }
}
