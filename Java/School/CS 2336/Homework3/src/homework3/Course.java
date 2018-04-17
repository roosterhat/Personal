/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework3;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class Course {
    private String title;
    private ArrayList<String> students;
    
    public Course(String title){
        this.title = title;
        students = new ArrayList();
    }
    
    public void addStudent(String student){
        students.add(student);
    }
    
    public boolean removeStudent(String student){
        return students.remove(student);
    }
    
    public ArrayList<String> getStudents(){
        return students;
    }
    
    public int getNumberOfStudents(){
        return students.size();
    }
    
    public String getTitle(){
        return title;
    }
}
