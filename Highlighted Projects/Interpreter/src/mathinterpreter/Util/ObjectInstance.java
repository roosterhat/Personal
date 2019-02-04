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
public class ObjectInstance {
    public Object object;
    public Range range;
    public ObjectInstance(Object object, Range range){
        this.object = object;
        this.range = range;
    }
}
