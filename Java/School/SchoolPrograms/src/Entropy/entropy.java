/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Entropy;

import java.util.ArrayList;
import schoolprograms.SchoolProgramsCommon;

/**
 *
 * @author erik
 */
public class entropy implements SchoolProgramsCommon
{
    public static void main(String args[])
    {
        new entropy().run();
    }
    
    String name = "Entropy";
    int[] coins = new int[20]; 
    double totalHeads=20,totalTails=0;
    double trials=10000;
    public void run()
    {
        
        for(int x=0;x<trials;x++)
        {
            int i = (int)(Math.random()*20);
            if(coins[i]==0)
            {
                coins[i] = 1;
            }
            else
            {
                coins[i] = 0;
            }
            int c=0;
            for(int a=0;a<20;a++)
            {
                
                if(coins[a]==0)
                {
                    c++;
                }
            }
            totalHeads+=c;
            totalTails+=20-c;
        }
        System.out.print("Avg. Heads: "+totalHeads/trials);
        System.out.println("  Avg. Tails: "+totalTails/trials);
    }
    public String getName()
    {
        return name;
    }
}
