/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hashing;

/**
 *
 * @author ostlinja
 */
public class StringComparator implements HashComparator<String> {
    int a = 11;
    public StringComparator(){}
    public int hashIndex(String k)
    {
        int slen = k.length();
        int i = -1;
        int code = 0;
        while(i<slen-1)
        {
            i++;
            code = (int)k.charAt(i)+a*code;
        }
        return Math.abs(code);
    }
    
    public boolean keyEqual(String k1, String k2)
    {
        return k1.equals(k2);
    }
}
