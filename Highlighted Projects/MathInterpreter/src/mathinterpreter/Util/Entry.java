/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Util;

/**
 *
 * @author eriko
 */
public class Entry<V> {
    public V value;
    public int index;
    public Entry(V value, int index){
        this.value = value;
        this.index = index;
    }
}
