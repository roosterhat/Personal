/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework5;

/**
 *
 * @author eriko
 */
public class Circle extends GeometricObject{
    double radius;
    public Circle(String color, boolean filled, double radius){
        super(color,filled);
        this.radius = radius;
    }
    
    public double getArea(){
        return Math.PI * Math.pow(radius, 2);
    }
    
    public double getPerimeter(){
        return Math.PI * radius * 2;
    }
    
    public String toString(){
        return "Circle: "+super.toString();
    }
}
