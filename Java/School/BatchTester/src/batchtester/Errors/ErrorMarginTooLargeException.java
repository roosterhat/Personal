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
public class ErrorMarginTooLargeException extends Exception{
    public ErrorMarginTooLargeException(double expected, double actual){
        super(String.format("Error Too Large, Margin: %f Actual: %f", expected, actual));
    }
    public ErrorMarginTooLargeException(String s){
        super(s);
    }
}
