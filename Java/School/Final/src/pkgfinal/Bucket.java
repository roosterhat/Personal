/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkgfinal;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class Bucket<X> implements IBucket<X> {
    ArrayList<X> list;
    
    public Bucket()
    {
        list = new ArrayList();
    }
    
    public void add(X i){
        list.add(i);
    }
    
    public void dump(ArrayList<X> a){
        for(X i:list){
            a.add(i);
        }
        list = new ArrayList<X>();
    }
    
    public int length(){
        return list.size();
    }
    
    public ArrayList<X> elements(){
        return list;
    }
    
    public String toString(){
        return list.toString();
    }
    
}
