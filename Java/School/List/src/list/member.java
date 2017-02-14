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
public class member<x> implements LListvisitor<x,Boolean>{
    x searchval;
    public member(x s)
    {
        searchval = s;
    }
    public Boolean emptylist(){return false;}
    public Boolean conslist(x f,Boolean r)
    {
        return f.equal(searchval)||r;
    }
}
