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
public interface Test<I,O> {
    public O run(I arg) throws Exception;
}
