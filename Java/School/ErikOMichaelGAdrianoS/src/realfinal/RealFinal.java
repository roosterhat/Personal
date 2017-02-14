/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realfinal;

import HashTable.*;
import dictionary.IDictionary;
import java.util.Iterator;

/**
 *
 * @author ostlinja
 */
public class RealFinal {

    static Alphabet a = new Alphabet();
    public static void main(String[] args) {
        HashComparator hc = new StringComparator();
        DHash<String,String> dhash = new DHash(10007,hc);
        LPHash<String,String> lphash = new LPHash(10007,hc);
        for(int i=1000;i<10007;i+=1000)
        {
            populate(dhash,1000);
            populate(lphash,1000);
            System.out.println("For DH  "+i+" elements generates: "+dhash.getCollisions()+" collisions");
            System.out.println("For LPH "+i+" elements generates: "+lphash.getCollisions()+" collisions\n");
 
        }
        String test = randomString(12);
        dhash.insert(test,"This is a test");
        System.out.println("["+test+"] = "+dhash.findElement(test));
        System.out.println("Contains \"This is a test\" : "+dhash.contains(test));
        
        
    }
    
    
    //adds 's' elements to a given hash table, each element has a random string of characters for the key and element 
    //Invariance: while the hash table contains the string 'k' and the hash table is not full
    public static void populate(IDictionary<String, String> ht,int s)
    {
        for(int i=0;i<s;i++)
        {
            //ht.insert(randomString((int)(Math.random()*11)+1), randomString(10));
            String k = randomString(12);
            while(ht.contains(k) && !ht.full())
                k = randomString(12);
            ht.insert(k,randomString(12));
        }
    }
    
    //creates a random string of 's' length
    public static String randomString(int s)
    {
        String temp = "";
        for(int i=0;i<s;i++)
        {
            temp+=Character.toString(a.getLetter((int)(Math.random()*a.getSize())));
        }
        return temp;
    }
    
    //prints all of the elements of a given hash table
    //Invariance: while Iterator has next element 
    public static void printHT(IDictionary<String, String> ht)
    { 
        Iterator<String> klooper = ht.keys();
        String key, elem;
        while (klooper.hasNext())
        { 
            key = klooper.next();
            elem = ht.findElement(key);
            if (elem != null)
            {
                System.out.println("The key for element " + elem + " is " + key);
            }
        }
    }   
}
