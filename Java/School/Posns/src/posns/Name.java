/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posns;

/**
 *
 * @author ostlinja
 */
public class Name extends Pair<String,String> implements Iname{
    String name,lastname;
    public Name(String n,String l)
    {
        name = n;
        lastname = l;
    }
    
    protected Name add(String n,String l)
    {
        return new Name(left+n,right+l);
    }
    
    protected void madd(String n,String l)
    {
        setName(left+n);
        setLastname(right+l);
    }
    
    public String getName(){return getLeft();}
    public String getLastname(){return getRight();}
    
    public Name rename(String n,String l){return add(n,l);}
    
    public void setName(String n){left = n;}
    public void setLastname(String l){right = l;}
    
    public void mrename(String n,String l)
    {
        madd(n,l);
    }
}
