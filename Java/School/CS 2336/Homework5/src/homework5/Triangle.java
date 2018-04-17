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
public class Triangle extends GeometricObject{
    double side1,side2,side3;
    public Triangle(){
        super();
        side1 = 1;
        side2 = 1;
        side3 = 1;
    }
    
    public Triangle(String color, boolean filled,double side1, double side2, double side3){
        super(color, filled);
        this.side1 = side1;
        this.side2 = side2;
        this.side3 = side3;
    }
    
    public double getArea(){
        double p = (side1+side2+side3)/2;
        double area = Math.sqrt(p*(p-side1)*(p-side2)*(p-side3));
        return area;
    }
    
    public double getPerimeter(){
        return side1 + side2 + side3;
    }
    
    public String toString(){
        return "Triangle: "+super.toString();
    }
}
