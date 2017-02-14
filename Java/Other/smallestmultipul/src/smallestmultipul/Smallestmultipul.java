/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smallestmultipul;

import java.util.*;
public class Smallestmultipul 
{

    
    public static void main(String[] args) 
    {
        boolean divisible;
        int count = 1;
        while (true)
        {
            divisible = true;
            for(int x=1;x<=20;x++)
            {
                if(count%x!=0)
                {
                    divisible = false;
                    break;
                }
            }
            if(divisible)
            {
                System.out.println(count);
                break;
            }
            count++;
        }
    }
}
