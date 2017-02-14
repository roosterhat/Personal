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
public class consList<x> implements LList<x> {
    private x f;
    private LList<x> r;
    public consList(x a, LList<x> rest)
    {
        f = a;
        r = rest;
    }
    
    public LList<x> cons(x val)
    {
        return new consList<x>(val, this);
    }
    
    public x first(){return f;}
    public LList<x> rest(){return r;}
    public int length(){return 1+r.length();}
    public boolean isEmpty(){return false;}
    public x listref(int i)throws Exception
    {
        if(i==0){return this.first();}
        else
        {
            try{return this.rest().listref(i-1);}
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }
    public LList<x> append(LList<x> l)
    {
        return this.rest().append(l).cons(this.first());
    }
    
    public LList<x> revHelper(LList<x> l,LList<x> res)
    {
        try{
            if(l.isEmpty())
            {
                return res;
            }
            else
            {
                return revHelper(l.rest(),res.cons(l.first()));
            }
        }catch(Exception e)
        {
            System.out.println("Reverse failed");
            return null;
        }
    }
    
    public LList<x> reverse()
    {
        return revHelper(this, new emptyList<x>());
    }
    public <r> LList<r> map(lFun<x,r> mapper)
    {
        return new consList<r>(mapper.f(this.first()),this.rest().map(mapper));
    }
    public LList<x> filter(lPred<x> pred)
    {
        if(pred.p(this.first()))
        {
            return this.rest().filter(pred).cons(this.first());
        }
        else
        {
            return this.rest().filter(pred);
        }
    }
    public <r> r foldl(lFun2<x,r> fn, r res)
    {
        return this.rest().foldl(fn,fn.f(this.first(),res));
    }
    public <r> r foldr(lFun2<x,r> fn, r res)
    {
        return this.reverse().foldl(fn, res);
    }
    public <r> r visit(LListvisitor<x,r> v)
    {
        return v.conslist(this.first(),this.rest().visit(v));
    }
}
