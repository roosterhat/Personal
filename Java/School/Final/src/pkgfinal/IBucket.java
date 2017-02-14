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
public interface IBucket<X> {
    public void add(X i);
    public void dump(ArrayList<X> a);
    public int length();
    public ArrayList<X> elements();
    public String toString();
}
