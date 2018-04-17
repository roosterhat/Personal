/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class CreditCard {
    public static void main(String[] args) {
        new CreditCard();
    }
    
    Scanner scanner = new Scanner(System.in);
    public CreditCard(){
        System.out.print("Enter credit card number: ");
        if(isValid(scanner.nextLong()))
            System.out.println("Number is valid");
        else
            System.out.println("Number is invalid");
    }
    
    public static boolean isValid(long number){
        return (sumOfDoubleEvenPlace(number)+sumOfOddPlace(number))%10==0; //returns true if the sum is divisible by 10
    }
    
    public static int sumOfDoubleEvenPlace(long number){//sums the doubled value of everyother digit, if the product of the digit is greater than 
        int result = 0;                                 //10 then the sum of the digits of that product are used instead 
        String n = String.valueOf(number);
        for(int i=0;i<n.length();i+=2)
            result+=getDigit(Integer.valueOf(n.charAt(i))*2);
        return result;
    }
    
    public static int getDigit(int num){//returns the sum of all digits in a number
        String n = String.valueOf(num);
        int sum = 0;
        for(int i=0;i<n.length();i++)
            sum+=Integer.valueOf(n.charAt(i));
        return sum;
    }
    
    public static int sumOfOddPlace(long number){//returns the sum of all odd digits
        int result = 0;
        String n = String.valueOf(number);
        for(int i=1;i<n.length();i+=2)
            result+=Integer.valueOf(n.charAt(i));
        return result;
    }
   
}
