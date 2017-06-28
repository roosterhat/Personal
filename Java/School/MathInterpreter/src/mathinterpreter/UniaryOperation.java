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
public class UniaryOperation<E> extends Operation{
    public UniaryOperation(String o,int w,UniaryFunction<E> f){
        this(o,w,Operation.RIGHT,f);
    }
    public UniaryOperation(String o,int w,UniaryFunction<E> f,Converter<E> c){
        this(o,w,Operation.RIGHT,f,c);
    }
    public UniaryOperation(String o,int w,int s,UniaryFunction<E> f){
        this(o,w,s,f,x->(E)Double.valueOf(x));
    }
    public UniaryOperation(String o,int w,int s,UniaryFunction<E> f,Converter<E> c){
        super(o,w,s,f,c);
    }
    
    @Override
    public String execute(ArrayList<String> parts){
        return ((UniaryFunction)function).execute(converter.convert(parts.get(0)));
    }

}

