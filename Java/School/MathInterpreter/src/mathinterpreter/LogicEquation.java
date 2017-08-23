/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

/**
 *
 * @author ostlinja
 */
public class LogicEquation extends MathInterpreter{
    public static void main(String[] args) {
        LogicEquation eq = new LogicEquation("~((x&y)|z)");
        //try{System.out.println(eq.f(true,true,false));}catch(Exception e){System.out.println(e.getMessage());}
    }
    public LogicEquation()
    {
        this("");
    }
    public LogicEquation(String eq)
    {
        super();
        variables.clear();
        for(char c='a';c<='z';c++)
            variables.add(String.valueOf(c));
        addPair(new BasicParenthesis());
        setOperations();   
        setEquation(eq);
    }
    
    public void setOperations(){
        addOperation(new BinaryOperation<Boolean>("&",0,(x,y)->{
            return String.valueOf(x && y);
        },x->Boolean.valueOf(x)));
        addOperation(new BinaryOperation<Boolean>("|",0,(x,y)->{
            return String.valueOf(x || y);
        },x->Boolean.valueOf(x)));
        addOperation(new UniaryOperation<Boolean>("~",0,x->{
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
