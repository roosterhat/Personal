/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author eriko
 */
public class Employee extends Person{
    protected String office;
    protected double salary;
    final protected Calendar dateHired;
    
    public Employee(){
        super();
        office = "";
        salary = 0.0;
        dateHired = Calendar.getInstance();
    }
    
    public Employee(String name, String address, String phoneNumber, String emailAddress, String office, double salary){
        super(name, address, phoneNumber, emailAddress);
        this.office = office;
        this.salary = salary;
        this.dateHired = Calendar.getInstance();
    }
    
    public String getOffice(){return office;}
    public double getSalary(){return salary;}
    public Calendar getDateHired(){return dateHired;}
    
    public void setOffice(String office){
        this.office = office;
    }
    
    public void setSalary(double salary){
        this.salary = salary;
    }
    
    public String toString(){
        return "Employee: "+name;
    }
    
}
