/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

/**
 *
 * @author ostlinja
 */
public class Matricies {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainForm main = new MainForm();
        main.setVisible(true);
        Matrix a = new Matrix(2,2);
        Matrix b = new Matrix(2,2);
        Matrix c = new Matrix(2,2);
        a.set(1, 0, 0);
        a.set(2, 0, 1);
        a.set(3, 1, 0);
        a.set(4, 1, 1);
        
        c.set(5, 0, 0);
        c.set(6, 0, 1);
        c.set(7, 1, 0);
        c.set(8, 1, 1);
        b.fill(1);
        System.out.println(a);
        a.multiply(2);
        System.out.println(a);
        try{a.add(b);}catch(Exception e){}
        System.out.println(a);
        b.fill(2);
        try{a.multiply(b);}catch(Exception e){}
        System.out.println(a);
        Matrix dot = new Matrix();
        try{dot = a.dot(c);}catch(Exception e){}
        System.out.println(dot);
    }
    
}
