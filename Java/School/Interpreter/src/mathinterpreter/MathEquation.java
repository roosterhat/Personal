/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import mathinterpreter.Operation.Parenthesis;
import mathinterpreter.Operation.UniaryOperation;
import mathinterpreter.Operation.BinaryOperation;
import mathinterpreter.Operation.Operation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import mathinterpreter.Operation.Pair;
import mathinterpreter.Operation.Summation;
import Interpreter.Equation;
import mathinterpreter.Operation.Converter;
import mathinterpreter.Operation.Function;
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
        //addOperation(new BinaryOperation<Double>("+",1,(x,y)->df.format(x+y)));
        //addOperation(new BinaryOperation<Double>("-",1,(x,y)->df.format(x-y)));
        addOperation(new Minus(df,this));
        addOperation(new Plus(df,this));
        addOperation(new BinaryOperation<Double>("*",2,(x,y)->df.format(x*y), new DoubleConverter()));
        addOperation(new BinaryOperation<Double>("/",2,(x,y)->{
                if(y==0)
                    throw new Exception("Divide by zero Error");
                return df.format(x/y);
            },new DoubleConverter()
        ));
        addOperation(new BinaryOperation<Double>("%",2,(x,y)->df.format(x%y),new DoubleConverter()));
        addOperation(new BinaryOperation<Double>("^",3,(x,y)->df.format(Math.pow(x,y)),new DoubleConverter()));
        addOperation(new E(df));
        addOperation(new UniaryOperation<Double>("sin",4,x->df.format(Math.sin(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("cos",4,x->df.format(Math.cos(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("tan",4,x->df.format(Math.tan(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("csc",4,x->df.format(1/Math.sin(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("sec",4,x->df.format(1/Math.cos(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("cot",4,x->df.format(1/Math.tan(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("asin",4,x->df.format(Math.asin(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("acos",4,x->df.format(Math.acos(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("atan",4,x->df.format(Math.atan(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("acsc",4,x->df.format(1/Math.asin(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("asec",4,x->df.format(1/Math.acos(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("acot",4,x->df.format(1/Math.atan(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("log",4,x->{
                if(x<0)
                    throw new Exception("Cannot preform 'log' on negative number: '"+x+"'");
                return df.format(Math.log(x));
            }, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("ln",4,x->{
                if(x<0)
                    throw new Exception("Cannot preform 'ln' on negative number: '"+x+"'");
                return df.format(Math.log(x));
            }, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("abs",4,x->df.format(Math.abs(x)),new DoubleConverter()));
        addOperation(new UniaryOperation<Double>("sqrt",4,x->{
                if(x<0)
                    throw new Exception("Cannot preform 'sqrt' on negative number: '"+x+"'");
                return df.format(Math.sqrt(x));
            }, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("!",2,UniaryOperation.LEFT,
            x->{
                double res = 1;
                for(int i=x.intValue();i>0;i--)
                    res*=i;
                return String.valueOf(res);
            }, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("round",0,UniaryOperation.RIGHT,
            x->{return String.valueOf(Math.round(x));}, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("deg",4,UniaryOperation.RIGHT,
            x->{return df.format(Math.toDegrees(x));}, new DoubleConverter()
        ));
        addOperation(new UniaryOperation<Double>("rad",4,UniaryOperation.RIGHT,
            x->{return df.format(Math.toRadians(x));}, new DoubleConverter()
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
            }, new DoubleConverter()
        ));
    }
       
    private void setFunctions(){
        addOperation(new Function("max",3,
            x->{
                if(x.isEmpty())
                    return "";
                double max = (Double)x.get(0);
                for(double n: (ArrayList<Double>)x)
                    max = Math.max(n, max);
                return String.valueOf(max);
            },x->Double.valueOf(x),this
        ));
        
        addOperation(new Function("min",3,
            x->{
                if(x.isEmpty())
                    return "";
                double min = (double)x.get(0);
                for(double n: (ArrayList<Double>)x)
                    min = Math.min(n, min);
                return String.valueOf(min);
            },x->Double.valueOf(x),this
        ));
        
        addOperation(new Function("sum",3,
            x->{
                double sum = 0;
                for(double n: (ArrayList<Double>)x)
                    sum += n;
                return String.valueOf(sum);
            },x->Double.valueOf(x),this
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
            },x->Double.valueOf(x),this
        ));
        
        addOperation(new Summation(this));
        
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

}
    
class Constant extends Operation<String>
{
    ConstantFunction function;
    public Constant(String constant,ConstantFunction function)
    {
        super(constant,10);
        this.function = function;
    }
    
    public Output<String> processOperation(Equation equation, int index) throws Exception {
        return new Output(execute(equation),new Range(index,index));
    }
    
    public String execute(Equation equaiton){return function.execute();}
   
}

interface ConstantFunction{
    public String execute();
}

class Minus extends BinaryOperation<Double>{
    MathEquation equation;
    public Minus(DecimalFormat df, MathEquation equation){
        super("-",0,(x,y)->df.format(x-y), new DoubleConverter());
        this.equation = equation;
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        if(isNegativeNumber(equation.parsedEquation,index))
            return new Output("-"+equation.parsedEquation.get(index+1),new Range(index,index+1));
        return super.processOperation(equation, index);
    } 
    
    private boolean isNegativeNumber(ArrayList<String> s, int index){
        String part = s.get(index);
        if(equation.isValidOperation(part)){
            Operation o = equation.getOperation(s.get(Math.max(index-1, 0)));
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
    
    private boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
}

class Plus extends BinaryOperation<Double>{
    MathEquation equation;
    public Plus(DecimalFormat df, MathEquation equation){
        super("+",0,(x,y)->df.format(x+y), new DoubleConverter());
        this.equation = equation;
    }
    
    public Output processOperation(Equation equation, int index)throws Exception{
        ArrayList<String> temp = (ArrayList<String>)equation.parsedEquation.clone();
        temp.set(index, "-");
        if(isNegativeNumber(temp,index))
            return new Output(equation.parsedEquation.get(index+1),new Range(index,index+1));
        return super.processOperation(equation, index);
    } 
    
    private boolean isNegativeNumber(ArrayList<String> s, int index){
        String part = s.get(index);
        if(equation.isValidOperation(part)){
            Operation o = equation.getOperation(s.get(Math.max(index-1, 0)));
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
    
    private boolean isNumber(String s){
        return s.matches("\\-?\\d+(\\.\\d+)?");
    }
}

class E extends BinaryOperation<Double>{
    public E(DecimalFormat df){
        super("E",2,(x,y)->df.format(x*Math.pow(10, y)),new DoubleConverter());
    }
    
    public Output<String> processOperation(Equation equation, int index)throws Exception{
        if(equation.objectEquation.get(Math.max(0, index-1)) instanceof Operation){
            equation.parsedEquation.add(index, "1");
            equation.setEquation(equation.parsedEquation);
        }
        return super.processOperation(equation, index);
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

class DoubleConverter implements Converter<Double>{
    public Double convert(String s)throws Exception{
        return Double.valueOf(s);
    }
}
