/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public interface CommandFunction {
    public String execute(ArrayList<Keyword> args)throws Exception;
}
