/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package whatever;
import java.util.*;
/**
 *
 * @author Wonder-Space 3
 */
public class Whatever 
{
    private static Scanner scan = new Scanner(System.in);
    private static int total =0;
    private static ArrayList<Integer> numbers = new ArrayList();
    private static int length;
      
    public static void main(String[] args) 
    {     
        userinput();
        addnumbers();
    }
    public static void userinput()
    {
        System.out.println("How many numbers do you want to input?");
        length = scan.nextInt();      
        for(int x=1;x<=length;x++)
        {
            System.out.println("Enter a number");
            numbers.add(scan.nextInt());
        }
    }
    public static void addnumbers()
    {
        for(int x=0;x<length;x++)
        {
            total += numbers.get(x);
        }
        System.out.println("The sum is "+total);
    }
}
