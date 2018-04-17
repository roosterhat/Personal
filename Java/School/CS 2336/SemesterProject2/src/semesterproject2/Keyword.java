/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.lang.reflect.Type;

/**
 *
 * @author eriko
 */
public class Keyword<E> {
    String keyword;
    String type;
    E object;
    public Keyword(String keyword, String type){
        this.keyword = keyword;
        this.type = type;
    }
}
