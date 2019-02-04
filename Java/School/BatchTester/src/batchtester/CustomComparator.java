/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

/**
 *
 * @author eriko
 */
public abstract class CustomComparator<T> {
    public abstract double compare(T t1, T t2);
}
