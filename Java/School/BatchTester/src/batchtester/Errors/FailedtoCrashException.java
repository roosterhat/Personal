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
public class FailedtoCrashException extends Exception{
    public FailedtoCrashException(){
        super("Method failed to crash when expected");
    }
    
    public FailedtoCrashException(String s){
        super(s);
    }
    
}
