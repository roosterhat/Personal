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
class Operation {
    String operator;
    int weight;
    TwoPartFunction function;
    public Operation(String s,int w,TwoPartFunction f){
        operator = s;
        weight = w;
        function = f;
    }
    
    public String execute(double x,double y){
        return function.execute(x,y).toString();
    }
}

class SingleOperation{
    String operator;
    int weight;
    OnePartFunction function;
    public SingleOperation(String s,int w,OnePartFunction f){
        operator = s;
        weight = w;
        function = f;
    }
    
    public String execute(double x){
        return function.execute(x).toString();
    }
}

interface TwoPartFunction<E>
{      
    public E execute(double x,double y);
}

interface OnePartFunction<E>
{      
    public E execute(double x);
}