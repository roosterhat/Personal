/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import mathinterpreter.Operation.Parenthesis;
import mathinterpreter.Operation.Function;
import mathinterpreter.Operation.UniaryOperation;
import mathinterpreter.Operation.BinaryOperation;
import mathinterpreter.Operation.Operation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Operation.BinaryFunction;
import mathinterpreter.Operation.Converter;
import mathinterpreter.Operation.Pair;
import mathinterpreter.Operation.Summation;
import mathinterpreter.Operation.UniaryFunction;
import mathinterpreter.Util.Equation;
import mathinterpreter.Util.Output;
import mathinterpreter.Util.Range;

/**
 *
 * @author ostlinja
 */
public class MathEquation extends Equation{
    protected DecimalFormat df;
    
    public MathEquation(){
        this("");
    }
    
    public MathEquation(String s){
        super();
        setDecimalDepth(15);
        setOperators();
        setFunctions();
        setPairs();
        setConstants();
        setEquation(s);
    }
        
    private void setOperators(){
        addOperation(new Plus(df,this));
        addOperation(new Minus(df,this));
        addOperation(new BinaryOperation<Double>("*",1,(x,y)->df.format(x*y)));
        addOperation(new BinaryOperation<Double>("/",1,(x,y)->{
                if(y==0)
                    throw new Exception("Divide by zero Error");
                return df.format(x/y);
            }
        ));
        addOperation(new BinaryOperation<Double>("%",1,(x,y)->df.format(x%y)));
        addOperation(new BinaryOperation<Double>("^",2,(x,y)->df.format(Math.pow(x,y))));
        addOperation(new E(this));
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
            }
        ));
        addOperation(new UniaryOperation<Double>("ln",3,x->{
                if(x<0)
                    throw new Exception("Cannot preform 'ln' on negative number: '"+x+"'");
                return df.format(Math.log(x));
            }
        ));
        addOperation(new UniaryOperation<Double>("abs",3,x->df.format(Math.abs(x))));
        addOperation(new UniaryOperation<Double>("sqrt",3,x->{
                if(x<0)
                    throw new Exception("Cannot preform 'sqrt' on negative number: '"+x+"'");
                return df.format(Math.sqrt(x));
            }
        ));
        addOperation(new UniaryOperation<Double>("!",2,UniaryOperation.LEFT,
            x->{
                double res = 1;
                for(int i=x.intValue();i>0;i--)
                    res*=i;
                return String.valueOf(res);
            }
        ));
        addOperation(new UniaryOperation<Double>("round",0,UniaryOperation.RIGHT,
            x->{return String.valueOf(Math.round(x));}
        ));
        addOperation(new UniaryOperation<Double>("deg",4,UniaryOperation.RIGHT,
            x->{return df.format(Math.toDegrees(x));}
        ));
        addOperation(new UniaryOperation<Double>("rad",4,UniaryOperation.RIGHT,
            x->{return df.format(Math.toRadians(x));}
        ));
        addOperation(new UniaryOperation<Double>("frac",0,UniaryOperation.RIGHT,
            x->{
                String sign = "";
                if (x < 0){
                    sign = "-"; 
                    x*=-1;
                }
                double tolerance = 1.0E-6;
                double h1=1; double h2=0;
                double k1=0; double k2=1;
                double b = x;
                do {
                    double a = Math.floor(b);
                    double aux = h1; h1 = a*h1+h2; h2 = aux;
                    aux = k1; k1 = a*k1+k2; k2 = aux;
                    b = 1/(b-a);
                } while (Math.abs(x-h1/k1) > x*tolerance);

                return sign+(int)h1+(k1==1 ? "" : "/"+(int)k1);
            }
        ));
    }
       
    private void setFunctions(){
        addOperation(new Function("max",3,
            x->{
                if(x.isEmpty())
                    return "";
                double max = (double)x.get(0);
                for(double n: (ArrayList<Double>)x)
                    max = Math.max(n, max);
                return String.valueOf(max);
            }
        ));
        
        addOperation(new Function("min",3,
            x->{
                if(x.isEmpty())
                    return "";
                double min = (double)x.get(0);
                for(double n: (ArrayList<Double>)x)
                    min = Math.min(n, min);
                return String.valueOf(min);
            }
        ));
        
        addOperation(new Function("sum",3,
            x->{
                double sum = 0;
                for(double n: (ArrayList<Double>)x)
                    sum += n;
                return String.valueOf(sum);
            }
        ));
        
        addOperation(new Function("rand",3,
            x->{
                if(x.size()==1)
                    return String.valueOf(Math.random()*(Double)x.get(0));
                else if(x.size()==2){
                    double start = Math.min((Double)x.get(0),(Double)x.get(1));
                    double end = Math.max((Double)x.get(0),(Double)x.get(1));
                    return String.valueOf(start+(Math.random()*(end-start)));
                }
                else
                    return String.valueOf(Math.random());
            }
        ));
        
        addOperation(new Summation(MathEquation.class, new Object[]{}));
        
    }
    
    private void setPairs(){
        addOperation(new Parenthesis());
        addOperation(new Literal());
    }
    
    private void setConstants(){
        addOperation(new Constant("pi",()->String.valueOf(Math.PI)));
        addOperation(new Constant("e",()->String.valueOf(Math.E)));
    }
    
    public void setDecimalDepth(int d){
        if(d>=0)
            df = new DecimalFormat("#."+String.format("%0" + d + "d", 0).replace("0","#"));
    }
    
    public void setDecimalFormat(DecimalFormat f){
        df = f;
    }

    public String f(int... arguments)throws Exception{
        double[] res = new double[arguments.length];
        for(int i=0;i<arguments.length;i++)
            res[i] = (double)arguments[i];
        return f(res);
    }
   
    //attempts to solve the equation for the given value
    public String f(double... arguments)throws Exception{
        String[] args = new String[arguments.length];
        for(int i = 0;i<arguments.length;i++)
            args[i] = String.valueOf(arguments[i]);
        return interpret(args);
    }
    
    public double fD(double... args)throws Exception{
        return Double.valueOf(f(args));
    }
    
    public double fD(int... args)throws Exception{
        return Double.valueOf(f(args));
    }

    @Override
    protected ArrayList<String> preProcessEquation(ArrayList<String> array) {
        for(int i=0;i<array.size();i++){
            String part = array.get(i);
            if(variables.contains(part)){
                array.remove(i);
                array.addAll(i, new ArrayList(Arrays.asList(new String[]{"(",part,")"})));
                i+=2;
            }
            else if(isNegativeNumber(array,i)){
                array.set(i,"-"+array.get(i+1));
                array.remove(i+1);
            }
        }
        return array;
    }
    
    //functions the same as the isNegativeNumber but handles ArrayLists
    //used when the negative sign and number are split
    protected boolean isNegativeNumber(ArrayList<String> s, int index){
        String part = s.get(index);
        if(isValidOperation(part)){
            Operation o = getOperation(s.get(Math.max(index-1, 0)));
            if(part.equals("-"))
                return  (index==0)  ||
                        ((o!=null && 
                        (o instanceof UniaryOperation ? ((UniaryOperation)o).inputSide==UniaryOperation.RIGHT : true) &&
                        (o instanceof Pair ? ((Pair)o).open.equals(s.get(index-1)) : true)) &&
                        (index<s.size() && isNumber(s.get(index+1))));
        }
        else if(isNumber(part))
            return Double.valueOf(part)<0;
        return false;
    } 
                 
    public boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
}

class Constant extends Operation<String,String>
{
    ConstantFunction function;
    public Constant(String constant,ConstantFunction function)
    {
        super(constant,10);
        this.function = function;
    }
    
    public Output<String> processOperation(ArrayList<String> array, int index) throws Exception {
        return new Output(execute(null),new Range(index,index));
    }
    
    public String execute(ArrayList<String> array)throws Exception{
        return function.execute();
    }    
}

interface ConstantFunction{
    public String execute();
}

class Minus extends BinaryOperation<Double>{
    MathEquation _main;
    public Minus(DecimalFormat df, MathEquation m){
        super("-",0,(x,y)->df.format(x-y));
        _main = m;
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        if(_main.isNegativeNumber(array,index))
            return new Output("-"+array.get(index+1),new Range(index,index+1));
        return super.processOperation(array, index);
    } 
}

class Plus extends BinaryOperation<Double>{
    MathEquation _main;
    public Plus(DecimalFormat df, MathEquation m){
        super("+",0,(x,y)->df.format(x+y));
        _main = m;
    }
    
    public Output processOperation(ArrayList<String> array, int index)throws Exception{
        ArrayList<String> temp = (ArrayList<String>)array.clone();
        temp.set(index, "-");
        if(_main.isNegativeNumber(temp,index))
            return new Output(array.get(index+1),new Range(index,index+1));
        return super.processOperation(array, index);
    } 
}

class E extends BinaryOperation<Double>{
    MathEquation _main;
    public E(MathEquation m){
        super("E",2,(x,y)->m.df.format(x*Math.pow(10, y)));
        this._main = m;
    }
    
    public Output<String> processOperation(ArrayList<String> array, int index)throws Exception{
        if(_main.isValidOperation(array.get(Math.max(0, index-1))))
            array.add(index++, "1");
        return super.processOperation(array, index);
    }
}

class Literal extends Pair
{
    public Literal(){
        super("\"'","'\"",10);
    }
    
    public ArrayList<String> execute(ArrayList<String> array){
        return new ArrayList(Arrays.asList(new String[]{String.join("", array)}));
    }
}

