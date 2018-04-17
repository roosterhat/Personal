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
public class Homework3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<Person> people = new ArrayList();
        people.add(new Person("John", "123 ln", "1234567890", "john@gmail.com"));
        people.add(new Student("Bob", "124 ln", "1234567891", "bob@gmail.com",Student.SOPHOMORE));
        people.add(new Employee("Brenda", "125 ln", "1234567892", "brenda@gmail.com", "Managment", 12345.99));
        people.add(new Faculty("Richard", "126 ln", "1234567893", "richard@gmail.com", "Faculty", 12345.99,"2-5pm","Professor"));
        people.add(new Staff("Amanda", "127 ln", "1234567893", "amanda@gmail.com", "Staff", 12345.99,"Office Assistant"));
        for(Person p: people)
            System.out.println(p);
        
        
        ArrayList<Account> accounts= new ArrayList();
        accounts.add(new SavingsAccount(123,0.00,0.2));
        accounts.add(new CheckingAccount(321,1000.0,0.0,1234));
        for(Account a: accounts)
            System.out.println(a);
        
        Course cs = new Course("Computer Science");
        cs.addStudent("Erik");
        cs.addStudent("Bob");
        cs.addStudent("test");
        for(String s: cs.getStudents())
            System.out.println(s);
        cs.removeStudent("test");
        for(String s: cs.getStudents())
            System.out.println(s);
        
        ArrayList obj = new ArrayList();
        obj.add(new Loan());
        obj.add(new Circle());
        obj.add(new DateEx());
        
        for(Object o: obj)
            System.out.println(o);
    }
}

class Loan{
    public String toString(){return "Loan";}
}

class Circle{
    public String toString(){return "Circle";}
}

class DateEx{
    public String toString(){return "Date";}
}