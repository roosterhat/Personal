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
public class Student extends Person{
    final static int FRESHMAN = 0;
    final static int SOPHOMORE = 1;
    final static int JUNIOR = 2;
    final static int SENIOR = 3;
    private int status;
    
    public Student(){
        super();
        setStatus(FRESHMAN);
    }
    
    public Student(String name, String address, String phoneNumber, String emailAddress, int status){
        super(name,address, phoneNumber, emailAddress);
        setStatus(status);
    }
    
    public int getStatus(){return status;}
    
    public void setStatus(int status){
        if(status == FRESHMAN || status == SOPHOMORE || status == JUNIOR || status == SENIOR)
            this.status = status;
        else
            this.status = FRESHMAN;
    }
    
    public String toString(){
        return "Student: "+name;
    }
}
