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
public interface LList<x> {
    public LList<x> cons(x val);
    public int length();
    public boolean isEmpty();
    public x first() throws Exception;
    public LList<x> rest() throws Exception;
    public x listref(int i) throws Exception;
    public LList<x> append(LList<x> l);
    public LList<x> reverse();
    public <r> LList<r> map(lFun<x,r> f);
    public LList<x> filter(lPred<x> p);
    public <r> r foldl(lFun2<x,r> f, r rest);
    public <r> r foldr(lFun2<x,r> f, r rest);
    public <r> r visit(LListvisitor<x,r> v);
}
