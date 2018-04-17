/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.text.NumberFormat;
import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Payroll {
    public static void main(String[] args) {
        new Payroll();
    }
    
    Scanner scanner = new Scanner(System.in);
    double state, federal;
    public Payroll(){
        System.out.print("Enter employee name: ");
        String name = scanner.next();
        System.out.print("Enter number of hours worked: ");
        double hours = scanner.nextDouble();
        System.out.print("Enter hourly wage: ");
        double wage = scanner.nextDouble();
        
        Employee e = new Employee(name,hours,wage);
        
        System.out.print("Enter federal tax: ");
        federal = scanner.nextDouble();
        System.out.print("Enter state tax: ");
        state = scanner.nextDouble();
        System.out.println();
        
        displayInformation(e);
               
    }
    
    public void displayInformation(Employee e){
        NumberFormat f = NumberFormat.getCurrencyInstance(); //number formatter
        double g = e.hours*e.wage;      //gross wage
        double t = (federal+state)*g;   //total deductions
        String gross = f.format(g);
        String federalDeductions = f.format(g*federal);
        String stateDeductions = f.format(g*state);
        String totalDeductions = f.format(t);
        String net = f.format(g-t);     //net pay
       
        System.out.println(e);
        System.out.println("Gross salary: "+gross);
        System.out.println("Dedections");
        System.out.println("    Federal Tax: "+federalDeductions);
        System.out.println("    State Tax: "+stateDeductions);
        System.out.println("    Total: "+totalDeductions);
        System.out.println("NetPay: "+net);
    }
}

class Employee{
    String name;
    double hours;
    double wage;
    public Employee(String n, double h, double w){
        name = n;
        hours = h;
        wage = w;
    }
    
    public String toString(){
        return "Employee Name: "+name+"\n"
                + "Hours worked: "+hours+"\n"
                + "Hourly wage: "+NumberFormat.getCurrencyInstance().format(wage);
    }
}
