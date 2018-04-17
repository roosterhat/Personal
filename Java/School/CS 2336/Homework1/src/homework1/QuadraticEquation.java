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
public class QuadraticEquation {
    public static void main(String[] args) {
        new QuadraticEquation();
    }
    
    Scanner scanner = new Scanner(System.in);
    double a,b,c;
    QuadraticEquation(){
        System.out.print("Enter A: ");
        a = scanner.nextDouble();
        System.out.print("Enter B: ");
        b = scanner.nextDouble();
        System.out.print("Enter C: ");
        c = scanner.nextDouble();
        
        double interior = Math.pow(b,2)-4*a*c;                  //b^2 - 4ac
        System.out.print("The equation has ");
        if(interior>0){
            double root1 = (-b+Math.pow(interior,0.5))/(2*a);   //-b+sqrt(b^2 - 4ac)/2a
            double root2 = (b+Math.pow(interior,0.5))/(2*a);    //b+sqrt(b^2 - 4ac)/2a
            System.out.print("two roots "+root1+", "+root2);
        }            
        else if(interior==0){
            double root = (-b+Math.pow(interior,0.5))/(2*a);    //-b+sqrt(b^2 - 4ac)/2a
            System.out.print("one root "+root);
        }
        else
            System.out.println("no real roots");
    }
}
