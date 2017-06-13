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
        super(s);
        setDecimalDepth(5);
        setOperators();
        setFunctions();
        setPairs();
        constants = new <DoubleOperation>ArrayList();
        setConstants();
        parseEquation(equation);
    }
    
    private void setOperators(){
        addOperation(new DoubleOperation<Double>("+",0,(x,y)->df.format(x+y)));
        addOperation(new DoubleOperation<Double>("-",0,(x,y)->df.format(x-y)));
        addOperation(new DoubleOperation<Double>("*",1,(x,y)->df.format(x*y)));
        addOperation(new DoubleOperation<Double>("/",1,(x,y)->df.format(x/y)));
        addOperation(new DoubleOperation<Double>("^",2,(x,y)->df.format(Math.pow(x,y))));
        addOperation(new DoubleOperation<Double>("E",2,(x,y)->df.format(x*Math.pow(10, y)),
                x->{
                    if(x.equals(""))
                        return 1.0;
                    else
                        return Double.valueOf(x);
                }));
        
    }
       
    private void setFunctions(){
        addOperation(new SingleOperation<Double>("sin",3,x->df.format(Math.sin(x))));
        addOperation(new SingleOperation<Double>("cos",3,x->df.format(Math.cos(x))));
        addOperation(new SingleOperation<Double>("tan",3,x->df.format(Math.tan(x))));
        addOperation(new SingleOperation<Double>("csc",3,x->df.format(1/Math.sin(x))));
        addOperation(new SingleOperation<Double>("sec",3,x->df.format(1/Math.cos(x))));
        addOperation(new SingleOperation<Double>("cot",3,x->df.format(1/Math.tan(x))));
        addOperation(new SingleOperation<Double>("asin",3,x->df.format(Math.asin(x))));
        addOperation(new SingleOperation<Double>("acos",3,x->df.format(Math.acos(x))));
        addOperation(new SingleOperation<Double>("atan",3,x->df.format(Math.atan(x))));
        addOperation(new SingleOperation<Double>("acsc",3,x->df.format(1/Math.asin(x))));
        addOperation(new SingleOperation<Double>("asec",3,x->df.format(1/Math.acos(x))));
        addOperation(new SingleOperation<Double>("acot",3,x->df.format(1/Math.atan(x))));
        addOperation(new SingleOperation<Double>("log",3,x->df.format(Math.log(x))));
        addOperation(new SingleOperation<Double>("ln",3,x->df.format(Math.log(x))));
        addOperation(new SingleOperation<Double>("abs",3,x->df.format(Math.abs(x))));
        addOperation(new SingleOperation<Double>("sqrt",3,x->df.format(Math.sqrt(x))));
        addOperation(new SingleOperation("!",3,Operation.LEFT,
                x->{
                    double res = 1;
                    for(int i=((Double)x).intValue();i>0;i--)
                        res*=i;
                    return String.valueOf(res);
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
    
    public ArrayList<Operation> getOperators(){
        ArrayList<Operation> temp = (ArrayList<Operation>)getConstants().clone();
        temp.addAll(super.getOperators());
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
        return super.isValidOperator(op);
    }

    //replaces all constants with their respective values
    private ArrayList substituteConstants(ArrayList eq)
    {
        String exp = getConstantExpression();
        for(int i=0;i<eq.size();i++){
            String part = (String)eq.get(i);
            if(part.matches(exp))
                eq.set(i, ((ConstantOperation)getConstant(part)).execute());
        }
        return eq;
    }
    
    public String f(int x){
        return f((double)x);
    }
    
    //attempts to solve the equation for the given value
    public String f(double x){
        ArrayList eq = getParsedEquation();
        eq = subsituteVariable(eq,x);
        eq = substituteConstants(eq);
        return evaluate(eq);
    }
    
    public double fD(double x){
        return Double.valueOf(f(x));
    }
    
    public double fD(int x){
        return Double.valueOf(f(x));
    }
}

class ConstantOperation extends Operation
{
    public ConstantOperation(String o,ConstantFunction f)
    {
        super(o,0,Operation.BOTH,f);
    }
    
    public String execute(){
        return ((ConstantFunction)function).execute();
    }
}

interface ConstantFunction extends FunctionInterface
{
    public String execute();
}