/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class SSN {
    public static void main(String[] args) {
        new SSN();
    }
    
    Scanner scanner = new Scanner(System.in);
    public SSN(){
        System.out.println("Enter a SSN (###-##-####)");
        String ssn = scanner.next();
        if(Pattern.matches("\\d{3}-\\d{2}-\\d{4}",ssn))//attempts to match pattern with given input
            System.out.println(ssn+" is a vaild social security number");
        else
            System.out.println(ssn+" is an invaild social security number");
    }
}
