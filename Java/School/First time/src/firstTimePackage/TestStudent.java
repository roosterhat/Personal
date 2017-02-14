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
public class TestStudent {
    
    public static void main(String[] args){
        Student s1,s2;
        s1 = new Student("John","Smith",3.5,18);
        s2 = new Student("Rick","Roll",4.0,21);
        
        System.out.println(s1.toString());
        System.out.println(s2.toString());
        System.out.println();
        
        s1.setAge(23);
        s2.setName("Kennith","Bone");
        
        System.out.println(s1.toString());
        System.out.println(s2.toString());
    }
    
}
