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
public interface Iposn {
    public Posn move(Integer dx, Integer dy);
    public Integer getX();
    public Integer getY();
    public void setX(Integer a);
    public void setY(Integer a);
    public void mmove(Integer a,Integer b);
}
