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
public class Output<V> {
    public Range range;
    public V value;
    public Output(V value, Range range){
        this.value = value;
        this.range = range;
    }
}
