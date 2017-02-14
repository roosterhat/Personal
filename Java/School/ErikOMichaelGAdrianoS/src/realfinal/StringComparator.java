/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realfinal;

import HashTable.HashComparator;

/**
 *
 * @author ostlinja
 */

//StringComparator, a hash comparator for strings
public class StringComparator implements HashComparator<String> {
    
    public StringComparator(){}
    //creates a hash index for a given string
    public int hashIndex(String k)
    {
        int prime = 11;
        int slen = k.length();
        int i = -1;
        int code = 0;
        while(i<slen-1)
        {
            i++;
            code = (int)k.charAt(i)+prime*code;
        }
        return Math.abs(code);
    }
    
    //creates a different hash index for a given string
    public int sHash(String k)
    {
        int prime = 7;
        int slen = k.length();
        int i = -1;
        int code = 0;
        while(i<slen-1)
        {
            i++;
            code = (int)k.charAt(i)+prime*code;
        }
        return Math.abs(code);
    }
    
    //compares two keys, for this method it compares two strings
    public boolean keyEqual(String k1, String k2)
    {
        if(k1!=null)
            return k1.equals(k2);
        else
            return false;
    }
}
