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
public interface Iname {
    public Name rename(String n,String l);
    public String getName();
    public String getLastname();
    public void setName(String n);
    public void setLastname(String l);
    public void mrename(String n,String l);
    
}
