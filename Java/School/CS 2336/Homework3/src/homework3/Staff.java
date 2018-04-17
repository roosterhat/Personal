/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

import java.util.Date;

/**
 *
 * @author eriko
 */
public class Staff extends Employee{
    private String title;
    
    public Staff(){
        super();
        title = "";
    }
    
    public Staff(String name, String address, String phoneNumber, String emailAddress, String office, double salary, String title){
        super(name, address, phoneNumber, emailAddress, office, salary);
        this.title = title;
    }
    
    public String getTitle(){return title;}
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String toString(){
        return "Staff: "+name;
    }
}
