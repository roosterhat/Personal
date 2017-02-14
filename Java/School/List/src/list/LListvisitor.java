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
public interface LListvisitor<x,r> {
    public r emptylist();
    public r conslist(x f,r res);
}
