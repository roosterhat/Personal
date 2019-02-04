/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batchtester;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class IdGenerator {
    public static final int DEFAULTLENGTH = 12;
    private ArrayList<String> ids;
    
    public IdGenerator(){
        ids = new ArrayList();
    }
    
    public static String generateGenericID(int length){
        String result = new String();
        String alphanumeric = new String();
        for(char c = 'a'; c <= 'z'; c++)
            alphanumeric += c;
        alphanumeric += alphanumeric.toUpperCase();
        for(int i = 0; i <=9; i++)
            alphanumeric += i;
        for(int i=0;i<length;i++)
            result+=alphanumeric.charAt((int)(Math.random()*alphanumeric.length()));
        return result;
    }
    
    public String generateID(int length){
        String alphanumeric = new String();
        for(char c = 'a'; c <= 'z'; c++)
            alphanumeric += c;
        alphanumeric += alphanumeric.toUpperCase();
        for(int i = 0; i <=9; i++)
            alphanumeric += i;
        String result = new String();
        do{
            result = new String();
            for(int i=0;i<length;i++)
                result+=alphanumeric.charAt((int)(Math.random()*alphanumeric.length()));
        }while(ids.contains(result));
        ids.add(result);
        return result;
    } 
}
