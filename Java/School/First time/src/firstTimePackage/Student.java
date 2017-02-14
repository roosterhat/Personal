/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firstTimePackage;

/**
 *
 * @author ostlinja
 */
public class Student {
    private String name,lastname;
    private double GPA;
    private int age;
    public Student(String n,String l,int a)
    {
        name = n;
        lastname = l;
        age = a;
        GPA = 0;
    }
    public Student(String n, String l, double g, int a)
    {
        name = n;
        lastname = l;
        GPA = g;
        age = a;
    }
    public String getFirstName(){return name;}
    public String getLastName(){return lastname;}
    public int getAge(){return age;}
    public double getGPA(){return GPA;}
        
    public void setName(String f,String l){name = f;lastname = l;}
    public void setFirstName(String n){name = n;}
    public void setLastName(String n){lastname = n;}
    public void setAge(int a)
    {
        if(a>0)
            age = a;
    }
    public void setGPA(double g)
    {
        if(g>0)
            GPA = g;
    }
    
    public String toString()
    {
        return name+" "+lastname+ " Age: "+age+" GPA:"+GPA;
    }
    
}
