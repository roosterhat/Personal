/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework8;

/**
 *
 * @author eriko
 */
public class Student {
    long id;
    LinkedList<Class> classes;
    public Student(long id){
        this.id = id;
        classes = new LinkedList(new ClassComparator());
    }
    
    public Student(long id, LinkedList classes){
        this.id = id;
        this.classes = classes;
    }
    
    public void addClass(Class newClass){
        if(!registeredFor(newClass))
            classes.add(newClass);
    }
    
    public void removeClass(Class c){
        classes.delete(c);
    }
    
    public boolean registeredFor(Class c){
        return classes.contains(c);
    }
    
    public String toString(){
        return "ID: "+id+", Classes: "+classes;
    }
    
    
}
