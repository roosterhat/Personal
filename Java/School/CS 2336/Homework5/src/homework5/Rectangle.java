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
public class Rectangle extends GeometricObject{
    double length,width;
    public Rectangle(String color, boolean filled, double length, double width){
        super(color,filled);
        this.length = length;
        this.width = width;
    }
    
    public double getPerimeter(){
        return 2*(length+width);
    }
    
    public double getArea(){
        return length*width;
    }
    
    public String toString(){
        return "Rectangle: "+super.toString();
    }
}
