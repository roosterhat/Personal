/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class Equation extends MathInterpreter{
    private ArrayList<Operation> constants;
    private DecimalFormat df;
    
    public Equation(){
        this("");
    }
    
    public Equation(String s){
        super();
        setDecimalDepth(15);
        setOperators();
        setFunctions();
        setPairs();
        constants = new <BinaryOperation>ArrayList();
        setConstants();
        setEquation(s);
    }
    
    private void setOperators(){
        addOperation(new BinaryOperation<Double>("+",0,(x,y)->df.format(x+y)));
        addOperation(new BinaryOperation<Double>("-",0,(x,y)->df.format(x-y)));
        addOperation(new BinaryOperation<Double>("*",1,(x,y)->df.format(x*y)));
        addOperation(new BinaryOperation<Double>("/",1,(x,y)->{
                        if(y==0)
                            throw new Exception("Divide by zero Error");
                        return df.format(x/y);
                    }));
        addOperation(new BinaryOperation<Double>("^",2,(x,y)->df.format(Math.pow(x,y))));
        addOperation(new BinaryOperation<Double>("E",2,(x,y)->df.format(x*Math.pow(10, y)),
                x->{
                    if(x.equals(""))
                        return 1.0;
                    else
                        return Double.valueOf(x);
                }));
        
        addOperation(new UniaryOperation<Double>("sin",3,x->df.format(Math.sin(x))));
        addOperation(new UniaryOperation<Double>("cos",3,x->df.format(Math.cos(x))));
        addOperation(new UniaryOperation<Double>("tan",3,x->df.format(Math.tan(x))));
        addOperation(new UniaryOperation<Double>("csc",3,x->df.format(1/Math.sin(x))));
        addOperation(new UniaryOperation<Double>("sec",3,x->df.format(1/Math.cos(x))));
        addOperation(new UniaryOperation<Double>("cot",3,x->df.format(1/Math.tan(x))));
        addOperation(new UniaryOperation<Double>("asin",3,x->df.format(Math.asin(x))));
        addOperation(new UniaryOperation<Double>("acos",3,x->df.format(Math.acos(x))));
        addOperation(new UniaryOperation<Double>("atan",3,x->df.format(Math.atan(x))));
        addOperation(new UniaryOperation<Double>("acsc",3,x->df.format(1/Math.asin(x))));
        addOperation(new UniaryOperation<Double>("asec",3,x->df.format(1/Math.acos(x))));
        addOperation(new UniaryOperation<Double>("acot",3,x->df.format(1/Math.atan(x))));
        addOperation(new UniaryOperation<Double>("log",3,x->{
                        if(x<0)
                            throw new Exception("Cannot preform 'log' on negative number: '"+x+"'");
                        return df.format(Math.log(x));
                    }));
        addOperation(new UniaryOperation<Double>("ln",3,x->{
                        if(x<0)
                            throw new Exception("Cannot preform 'ln' on negative number: '"+x+"'");
                        return df.format(Math.log(x));
                    }));
        addOperation(new UniaryOperation<Double>("abs",3,x->df.format(Math.abs(x))));
        addOperation(new UniaryOperation<Double>("sqrt",3,x->{
                        if(x<0)
                            throw new Exception("Cannot preform 'sqrt' on negative number: '"+x+"'");
                        return df.format(Math.sqrt(x));
                    }));
        addOperation(new UniaryOperation<Double>("!",2,Operation.LEFT,
                x->{
                    double res = 1;
                    for(int i=((Double)x).intValue();i>0;i--)
                        res*=i;
                    return String.valueOf(res);
                }));
        
    }
       
    private void setFunctions(){
        addFunction(new Function("max",3,x->{
            double max = (double)x.get(0);
            for(double n: (ArrayList<Double>)x)
                max = Math.max(n, max);
            return String.valueOf(max);
        }));
        addFunction(new Function("min",3,x->{
            double min = (double)x.get(0);
            for(double n: (ArrayList<Double>)x)
                min = Math.min(n, min);
            return String.valueOf(min);
        }));
    }
    
    private void setPairs(){
        addPair(new Parenthesis());
    }
    
    private void setConstants(){
        constants.add(new ConstantOperation("pi",()->String.valueOf(Math.PI)));
        constants.add(new ConstantOperation("e",()->String.valueOf(Math.E)));
    }
    
    public ArrayList<Operation> getConstants(){
        return constants;
    }
    
    public Operation getConstant(String c){
        for(Operation o:getConstants())
            if(o.operator.equals(c))
                return o;
        return null;
    }
    
    public String getConstantExpression(){
        String res = "";
        for(Operation c: constants)
            res+=c.regex+"|";
        return res.substring(0, res.length()-1);
    }
    
    public ArrayList<Operation> getOperations(){
        ArrayList<Operation> temp = (ArrayList<Operation>)getConstants().clone();
        temp.addAll(super.getOperations());
        return temp;
    }
    
    public void addConstant(Operation o){
        constants.add(o);
        constants.sort((x,y)->((Operation)y).weight-((Operation)x).weight);
    }
    
    public void clearConstants(){
        constants = new ArrayList<Operation>();
    }
    
    public void setDecimalDepth(int d){
        if(d>=0)
            df = new DecimalFormat("#."+String.format("%0" + d + "d", 0).replace("0","#"));
    }
    
    public void setFormat(DecimalFormat f){
        df = f;
    }
    
    public boolean isValidOperation(String op){
        for(Operation o: constants)
            if(o.operator.equals(op))
                return true;
        return super.isValidOperation(op);
    }

    //replaces all constants with their respective values
    private ArrayList substituteConstants(ArrayList eq)throws Exception
    {
        String exp = getConstantExpression();
        for(int i=0;i<eq.size();i++){
            String part = (String)eq.get(i);
            if(part.matches(exp))
                eq.set(i, ((ConstantOperation)getConstant(part)).execute());
        }
        return eq;
    }
    
    public String f(int... arguments)throws Exception{
        double[] res = new double[arguments.length];
        for(int i=0;i<arguments.length;i++)
            res[i] = (double)arguments[i];
        return f(res);
    }
   
    //attempts to solve the equation for the given value
    public String f(double... arguments)throws Exception{
        ArrayList args = new ArrayList();
        for(double val:arguments)
            args.add(val);
        ArrayList eq = getParsedEquation();
        eq = subsituteVariables(eq,args);
        eq = substituteConstants(eq);
        return evaluate(eq);
    }
    
    public double fD(double... args)throws Exception{
        return Double.valueOf(f(args));
    }
    
    public double fD(int... args)throws Exception{
        return Double.valueOf(f(args));
    }
}

class ConstantOperation extends Operation
{
    public ConstantOperation(String o,ConstantFunction f)
    {
        super(o,0,Operation.RIGHT,f);
    }
    
    public String execute()throws Exception{
        return ((ConstantFunction)function).execute();
    }
}

interface ConstantFunction extends FunctionInterface{
    public String execute();
}
