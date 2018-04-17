/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework5;
import java.util.Scanner;
/**
 *
 * @author eriko
 */
public class Homework5 {

    public static void main(String[] args) {
        //createTriangle();
        compareObjects();
    }
    
    public static void compareObjects(){
        createTriangle();
        Rectangle r1 = new Rectangle("White",false, 10, 20);
        Rectangle r2 = new Rectangle("Black",false, 15, 25);
        Circle c1 = new Circle("White", false, 20);
        Circle c2 = new Circle("Black", false, 15);
        System.out.println("Larger Object: "+GeometricObject.max(r1, r2));
        System.out.println("Larger Object: "+GeometricObject.max(c1, c2));
    }
    
    public static void createTriangle(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Color: ");
        String color = scanner.next();
        System.out.print("Enter Side 1: ");
        double side1 = scanner.nextDouble();
        System.out.print("Enter Side 2: ");
        double side2 = scanner.nextDouble();
        System.out.print("Enter Side 3: ");
        double side3 = scanner.nextDouble();
        System.out.print("Enter Filled: ");
        boolean filled = scanner.nextBoolean();
        System.out.println(new Triangle(color,filled,side1,side2,side3));
    }
    
}
