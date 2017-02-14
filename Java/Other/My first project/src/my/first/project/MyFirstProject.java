/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.first.project;
import java.util.*;
/**
 *
 * @author Wonder-Space 3
 */
public class MyFirstProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter Your Name");
        String name = scan.nextLine();
        String temp = name.substring(0,1);
        name = name.substring(1, name.length());
        name = temp.toUpperCase()+name;
        System.out.println("How Old Are You");
        int age = scan.nextInt();
        System.out.println("Enter Your Credit Card Number");
        scan.nextLine();
        String CreditNumber = scan.nextLine();
        System.out.println("Hello "+name+" You Are "+age+" Years Old");
        char[] CCNumber = CreditNumber.toCharArray();
        System.out.print("Your Credit Card Number: ");
        for(int x=1;x<=CCNumber.length-4;x++)
        {
            System.out.print("*");
        }
        for(int x=CCNumber.length-4;x<CCNumber.length;x++)
        {
            System.out.print(CCNumber[x]);
        }
        System.out.println();
        //System.out.println("Your Credit Card Number: "+CreditNumber);
    }
}
