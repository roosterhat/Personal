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
public class Faculty extends Employee{
    private String officeHours, rank;
    
    public Faculty(){
        super();
        officeHours = "";
        rank = "";
    }
    
    public Faculty(String name, String address, String phoneNumber, String emailAddress, String office, double salary, String officeHours, String rank){
        super(name, address, phoneNumber, emailAddress, office, salary);
        this.officeHours = officeHours;
        this.rank = rank;
    }
    
    public String getOfficeHours(){return officeHours;}
    public String getRank(){return rank;}
    
    public void setOfficeHours(String officeHours){
        this.officeHours = officeHours;
    }
    
    public void setRank(String rank){
        this.rank = rank;
    }
    
    public String toString(){
        return "Faculty: "+name;
    }
}
