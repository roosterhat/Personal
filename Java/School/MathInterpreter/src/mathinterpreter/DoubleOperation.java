/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class DoubleOperation<E> extends Operation{
    public DoubleOperation(String o,int w,TwoPartFunction<E> f){
        this(o,w,f,x->(E)Double.valueOf(x));
    }
    public DoubleOperation(String o,int w,TwoPartFunction<E> f,Converter<E> c){
        super(o,w,Operation.BOTH,f,c);
    }
    
    @Override
    public String execute(ArrayList<String> parts){
        return ((TwoPartFunction)function).execute(converter.convert(parts.get(0)), converter.convert(parts.get(1)));
    }
    
}
