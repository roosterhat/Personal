/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester.Errors;

/**
 *
 * @author eriko
 */
public class IncompatableErrorException extends Exception{
    public IncompatableErrorException(Exception expected, Exception actual){
        super(String.format("Incorrect Exception '%s' doesnt match '%s'", actual.toString(), expected.toString()));
    }
    
    public IncompatableErrorException(String s){
        super(s);
    }
}
