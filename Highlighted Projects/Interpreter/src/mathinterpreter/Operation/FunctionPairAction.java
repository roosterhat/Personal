/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter.Operation;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public interface FunctionPairAction<TYPE> {
    public String execute(ArrayList<TYPE> equation)throws Exception;
}
