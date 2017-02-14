/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Hashing;

import java.util.Iterator;

/**
 *
 * @author ostlinja
 */
public class HashEX {
    public static void populate(LPHash<String, String> ht)
    {
        ht.insert("Marco", "Morazan");
        ht.insert("Nick", "Olson");
        ht.insert("Jean Luc", "Picard");
        ht.insert("Cathryn", "Janeway");
    }
    
    public static void printHT(LPHash<String, String> ht)
    { 
        Iterator<String> klooper = ht.keys();
        String key, elem;
        while (klooper.hasNext())
        { 
            key = klooper.next();
            elem = ht.findElement(key);
            if (elem != null)
            {
                System.out.println("For key " + key + " the element is " + elem);
            }
        }
    }
    
    public static void main(String[] args)
    { 
        HashComparator<String> scomp = new StringComparator();
        LPHash<String, String> ht = new LPHash<String, String>(101, scomp);
        populate(ht);
        printHT(ht);
        System.out.println();
        ht.delete("Marco");
        ht.delete("Jean Luc");
        ht.insert("Professor", "X");
        printHT(ht);
        System.out.println(scomp.hashIndex("Nick")%101);
        System.out.println(ht.findElement("Nick"));
    }
}
