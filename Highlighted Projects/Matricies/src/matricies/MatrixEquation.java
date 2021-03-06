/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

/**
 *
 * @author ostlinja
 */
import mathinterpreter.Operation.Converter;
import mathinterpreter.Operation.Function;
import mathinterpreter.Operation.UniaryOperation;
import java.util.ArrayList;
import java.util.Map;
import mathinterpreter.*;
import mathinterpreter.Operation.BinaryOperation;
import mathinterpreter.Operation.PairFunction;
import mathinterpreter.Operation.Summation;
import mathinterpreter.Operation.UniaryFunction;

public class MatrixEquation extends Equation{
    MainForm _main;
    MatrixConverter mc;
    public MatrixEquation(MainForm m){
        this("",m);
        variables = new ArrayList();
    }
    
    public MatrixEquation(String s,MainForm m){
        _main = m;
        mc = new MatrixConverter(_main.matricies);
        setOperators();
        setFunctions();
        setEquation(s);
        setDecimalDepth(10);
    }

    
    private void setOperators(){       
        addOperation(new MatrixBinaryOperation("dot",1,(x,y)->{
            Matrix temp = mc.convert(y);
            Matrix m = x.dot(temp);
            _main.createNew(m);
            return mc.revert(m);
        },mc));      
        
        addOperation(new MatrixUniaryOperation("det",2,UniaryOperation.RIGHT,x->String.valueOf(x.determinate()),mc));
        
        addOperation(new MatrixUniaryOperation("trans",2,UniaryOperation.RIGHT,x->{
            x.transpose();
            return mc.revert(x);
        },mc));
        
        addOperation(new MatrixUniaryOperation("gj",2,UniaryOperation.RIGHT,x->{
            Matrix m = x.gaussJordan();
            _main.createNew(m);
            return mc.revert(m);
        },mc));
    }
    
    private void setFunctions(){
        addOperation(new Function("add",0,x->{
            Matrix m = mc.convert((String)x.get(0));
            String val = (String)x.get(1);
            if(isNumber(val))
                m.add(Double.valueOf(val));
            else{
                Matrix temp = mc.convert(val);
                m.add(temp);
            }
            return mc.revert(m);
        },x->x));  
        
        addOperation(new Function("sub",0,x->{
            Matrix m = mc.convert((String)x.get(0));
            String val = (String)x.get(1);
            if(isNumber(val))
                m.add(-1*Double.valueOf(val));
            else{
                Matrix temp = mc.convert(val);
                Matrix t = temp.clone();
                t.multiply(-1);
                m.add(t);
            }
            return mc.revert(m);
        },x->x));
        
        addOperation(new Function("mult",0,x->{
            Matrix m = mc.convert((String)x.get(0));
            String val = (String)x.get(1);
            if(isNumber(val))
                m.multiply(Double.valueOf(val));
            else{
                Matrix temp = mc.convert(val);
                m.multiply(temp);
            }
            return mc.revert(m);
        },x->x));
        
        addOperation(new Function("div",0,x->{
            Matrix m = mc.convert((String)x.get(0));
            String val = (String)x.get(1);
            if(isNumber(val))
                m.multiply(1/Double.valueOf(val));
            else{
                Matrix temp = mc.convert(val);
                m.multiply(temp.getInverted());
            }
            return mc.revert(m);
        },x->x));
       
        
        addOperation(new Function("new",5,x->{
            int r = x.size()>=2 ? (int)Double.parseDouble((String)x.get(0)) : 2;
            int c = x.size()>=2 ? (int)Double.parseDouble((String)x.get(1)) : 2;
            Matrix m = new Matrix(r,c);
            if(x.size()==3)
                return _main.createNew(m,String.valueOf(x.get(2)));
            else
                return _main.createNew(m);
        },x->x));
        
        addOperation(new Function("id",5,x->{
            int r = x.size()>=2 ? (int)Double.parseDouble((String)x.get(0)) : 2;
            int c = x.size()>=2 ? (int)Double.parseDouble((String)x.get(1)) : 2;
            Matrix m = new Matrix();
            if(x.size()==3)
                return _main.createNew(m.newIdentity(r, c),(String)x.get(2));
            else
                return _main.createNew(m.newIdentity(r, c));
        },x->x));
        
        addOperation(new Function("set",5,x->{
            Matrix m = _main.matricies.get((String)x.get(0));
                int r = (int)Double.parseDouble((String)x.get(1));
                int c = (int)Double.parseDouble((String)x.get(2));
                double val = Double.valueOf((String)x.get(3));
                m.set(val, r, c);
            return (String)x.get(0);
        },x->x));
        
        addOperation(new Function("resize",5,x->{
            String name = (String)x.get(0);
            Matrix m = _main.matricies.get(name);
                int r = (int)Double.parseDouble((String)x.get(1));
                int c = (int)Double.parseDouble((String)x.get(2));
                m.resize(r, c);
                _main.resize(name, r, c);
            return name;
        },x->x));
        ((Summation)this.getOperation("summation")).setEquation(this.getClass(),new Object[]{_main});
    }
}

class MatrixBinaryOperation extends BinaryOperation<Matrix>{
    Converter<Double> doubleConverter = x->Double.valueOf(x);
    MatrixFunction function;

    public MatrixBinaryOperation(String operation, int weight, MatrixFunction f, Converter c){
        super(operation, weight, (x,y)->"", c);
        function = f;
    }
    
    public boolean isNumber(String s){return s.matches("\\-?\\d+(\\.\\d*)?");}
        
    public String execute(ArrayList<String> parts)throws Exception{
        if(isNumber(parts.get(0)))
            return "";
        else 
            return ((MatrixFunction)function).execute((Matrix)converter.convert(parts.get(0)),parts.get(1));
    }
}

class MatrixUniaryOperation extends UniaryOperation<Matrix>{
    public MatrixUniaryOperation(String operation, int weight, int side, UniaryFunction<Matrix,String> function, MatrixConverter converter){
        super(operation,weight,side,function,converter);
    }
}

class MatrixConverter implements Converter<Matrix>
{
    Map<String,Matrix> names;
    public MatrixConverter(Map<String,Matrix> n){
        names = n;
    }
    
    public Matrix convert(String s){
        return names.get(s.trim());
    }
    
    public String revert(Matrix m){
        for(Map.Entry<String,Matrix> en:names.entrySet())
            if(en.getValue()==m)
                return en.getKey();
        return "";
    }
}


interface MatrixFunction{
    public String execute(Matrix x, String y)throws Exception;
}

interface LiteralFunction extends PairFunction{
    public ArrayList execute(ArrayList a);
}
