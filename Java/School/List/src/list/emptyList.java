/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package list;

/**
 *
 * @author ostlinja
 */
public class emptyList<x> implements LList<x>{
    public emptyList(){}
    public LList<x> cons(x val){return new consList<x>(val, this);}
    public int length(){return 0;}
    public boolean isEmpty(){return false;}
    public x first()throws Exception{throw new Exception("This list is empty");}
    public LList<x> rest() throws Exception{throw new Exception("This list is empty");}
    public x listref(int i)throws Exception{throw new Exception("This list is empty");}
    public LList<x> append(LList<x> l){return l;}
    public LList<x> reverse(){return this;}
    public <r> LList<r> map(lFun<x,r> f){return new emptyList<r>();}
    public LList<x> filter(lPred<x> p){return this;}
    public <r> r foldl(lFun2<x,r> f, r rest){return rest;}
    public <r> r foldr(lFun2<x,r> f, r rest){return rest;}
    public <r> r visit(LListvisitor<x,r> v){return v.emptylist();}
}
