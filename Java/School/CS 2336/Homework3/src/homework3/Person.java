/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

/**
 *
 * @author eriko
 */
public class Person {
    protected String name,address,phoneNumber,emailAddress;
    
    public Person(){
        name = "";
        address = "";
        phoneNumber = "";
        emailAddress = "";
    }
    
    public Person(String n, String a, String p, String e){
        name = n;
        address = a;
        phoneNumber = p;
        emailAddress = e;
    }
    
    public String getName(){return name;}
    public String getAddress(){return address;}
    public String getPhoneNumber(){return phoneNumber;}
    public String getEmailAddress(){return emailAddress;}
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setAddress(String address){
        this.address = address;
    }
    
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    
    public void setEmailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    
    public String toString(){
        return "Person: "+name;
    }
}
