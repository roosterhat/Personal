/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

/**
 *
 * @author eriko
 */
public class Output<E> {
    E value;
    Range range;
    public Output(E value, Range range){
        this.value = value;
        this.range = range;
    }
}
