/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class Function<IN_TYPE> extends UniaryOperation<String>{
    public FunctionPair bounds;
    
    public Function(String name,int weight, FunctionPairFunction<IN_TYPE> function){
        this(name,weight,function,x->Double.valueOf(x));
    }
    
    public Function(String name, int weight, FunctionPairFunction<IN_TYPE> function, Converter converter){
        super(name,weight,x->x);
        this.bounds = new FunctionPair(weight,function,converter);
    }
    
    public Function(String name, int weight, FunctionPair bounds){
        super(name, weight, x->x);
        this.bounds = bounds;
    }
    
    public ArrayList<String> getTokens(){
        ArrayList temp = super.getTokens();
        temp.addAll(bounds.getTokens());
        return temp;
    }
    
    public boolean equals(Object o){
        return operator.equals(o) || bounds.seperator.equals(o);
    }
    
    public String toString(){
        return operator+bounds;
    }


    
}
