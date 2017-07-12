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
public class BinaryOperation<E> extends Operation{
    public BinaryOperation(String o,int w,BinaryFunction<E> f){
        this(o,w,f,x->(E)Double.valueOf(x));
    }
    public BinaryOperation(String o,int w,BinaryFunction<E> f,Converter<E> c){
        super(o,w,Operation.BOTH,f,c);
    }
    
    @Override
    public String execute(ArrayList<String> parts)throws Exception{
        return ((BinaryFunction)function).execute(converter.convert(parts.get(0)), converter.convert(parts.get(1)));
    }
    
}
