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
public class SingleOperation<E> extends Operation{
    public SingleOperation(String o,int w,OnePartFunction<E> f){
        this(o,w,Operation.RIGHT,f);
    }
    public SingleOperation(String o,int w,OnePartFunction<E> f,Converter<E> c){
        this(o,w,Operation.RIGHT,f,c);
    }
    public SingleOperation(String o,int w,int s,OnePartFunction<E> f){
        this(o,w,s,f,x->(E)Double.valueOf(x));
    }
    public SingleOperation(String o,int w,int s,OnePartFunction<E> f,Converter<E> c){
        super(o,w,s,f,c);
    }
    
    @Override
    public String execute(ArrayList<String> parts){
        return ((OnePartFunction)function).execute(converter.convert(parts.get(0)));
    }

}

