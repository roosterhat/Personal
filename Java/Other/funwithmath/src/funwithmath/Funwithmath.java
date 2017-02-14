/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package funwithmath;
    import java.util.*;

public class Funwithmath 
{
    private static int number1;
    private static int number2;
    private static Scanner scan = new Scanner(System.in);
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        //sum();
        //System.out.println(squirrelPlay(100,true)?"The squirrels are playing":"The squirrels arent playing");
        System.out.println(goodParty(100,true)?"Its a good party":"Its a bad party");
    }
    public static boolean squirrelPlay(int temp, boolean summer)
    {
        if(summer)
        {
            if(temp>=60 && temp<=100)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if(temp>=60 && temp<=90)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    public static boolean goodParty(int nuts, boolean weekend)
    {
        if(weekend)
        {
            if(nuts>=40)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if(nuts>=40 && nuts<60)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    public static void sum()
    {
        System.out.println("Enter first number");
        number1 = scan.nextInt();
        System.out.println("Enter second number");
        number2 = scan.nextInt();
        
        int sum = number1+number2;
        if(number1==number2)
        {
            sum *= 2;
            System.out.println("("+number1+"+"+number2+")*2="+sum);
        }
        else
        {
            System.out.println(number1+"+"+number2+"="+sum);
        }
        
    }
    
}
