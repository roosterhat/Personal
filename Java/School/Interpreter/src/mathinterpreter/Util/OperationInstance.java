/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Util;

import mathinterpreter.Operation.Operation;

/**
 *
 * @author eriko
 */
public class OperationInstance extends ObjectInstance{
    public Operation operation;
    public OperationInstance(Operation operation, Range range){
        super(operation,range);
        this.operation = operation;
    }
}
