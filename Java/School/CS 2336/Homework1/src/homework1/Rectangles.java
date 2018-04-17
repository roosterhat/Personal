/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Rectangles {
    public static void main(String[] args) {
        new Rectangles();
    }
    
    Scanner scanner = new Scanner(System.in);
    public Rectangles(){
        System.out.println("Create rectangle 1");
        Rectangle r1 = new Rectangle(scanner);
        System.out.println("Create rectangle 2");
        Rectangle r2 = new Rectangle(scanner);
        if(r1.contains(r2.center))
            System.out.println("r1 inside r2");
        else if(r1.contains(r2) || r2.contains(r1))
            System.out.println("r1 overlaps r2");
        else
            System.out.println("r1 does not overlap r2");
    }
}

class Rectangle{
    Point center;
    Point TR,TL,BR,BL;//Top Right, Top Left, Botton Right, Botton Left
    double width,height;
    public Rectangle(Scanner sc){
        System.out.print("Enter x: ");
        double x = sc.nextDouble();
        System.out.print("Enter y: ");
        double y = sc.nextDouble();
        center = new Point(x,y);
        System.out.print("Enter width: ");
        width = sc.nextDouble();
        System.out.print("Enter height: ");
        height = sc.nextDouble();
        
        TR = new Point(x+width/2,y-height/2);
        TL = new Point(x-width/2,y-height/2);
        BR = new Point(x+width/2,y+height/2);
        BL = new Point(x-width/2,y+height/2);
    }
    public boolean contains(Point p){//determines if a point is within this rectangle
        return  p.x>=TL.x && p.x<=TR.x &&
                p.y>=TR.y && p.y<=BR.y;
    }
    
    public boolean contains(Rectangle r){//determines if a rectangle intersects this rectangle
        for(Point p: r.getVertecies())
            if(contains(p))
                return true;
        return false;
    }
    
    public Point[] getVertecies(){// returns all 4 vertecies of the rectangle
        return new Point[]{TL,TR,BL,BR};
    }
    
}

class Point{
    double x,y;
    public Point(int x,int y){
        this.x = x;
        this.y = y;
    }
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    
}