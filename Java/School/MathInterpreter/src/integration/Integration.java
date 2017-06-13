/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;
import mathinterpreter.*;
public class Integration {

    
    
    public static void main(String[] args) {
        Equation e = new Equation("e^x/x^2");
        //Equation e = new Equation("25/sqrt(4-(x^4))");
        int lower = 1;
        int upper = 4;
        int n = 10;
        new Integration().integrate(e,lower,upper,n);
    }
    
    public void integrate(Equation eq,double lower,double upper,int steps){
        double trap = trapezoid(eq,lower,upper,steps);
        double trap2 = trapezoid(eq,lower,upper,2*steps);
        double mid = midpoint(eq,lower,upper,steps);
        double mid2 = midpoint(eq,lower,upper,2*steps);
        double simps = simpsons(eq,lower,upper,steps);
        double simps2 = simpsons(eq,lower,upper,2*steps);
        double r1 = richardson(steps,trap,trap2);
        double r2 = richardson(steps,mid,mid2);
        double r3 = richardson(steps,simps,simps2);
        System.out.println("Integrate: "+eq+" on ["+lower+","+upper+"] with "+steps+" steps");
        System.out.println(String.format("%-12s %-14s %-14s %-14s","Method","Trapezoid","Midpoint","Simpsons"));
        System.out.println(String.format("%-12s %-14f %-14f %-14f",steps+" Steps",trap,mid,simps));
        System.out.println(String.format("%-12s %-14f %-14f %-14f",2*steps+" Steps",trap2,mid2,simps2));
        System.out.println(String.format("%-12s %-14f %-14f %-14f","Richardson",r1,r2,r3));
        System.out.println("True Integral: "+deepIntegrate(eq,lower,upper));
    }
    
    
    public double deepIntegrate(Equation eq,double lower,double upper){
        double integral = 0;
        double width = 0.001;
        eq.setDecimalDepth(10);
        for(double x=lower;x<=upper;x+=width)
            integral += eq.fD(x);
        return integral*width;
    }
    
    public double trapezoid(Equation eq,double lower,double upper,int steps){
        double width = (upper-lower)/steps;
        double integral = 0;
        double f1 = eq.fD(lower);
        for(double x=lower+width;x<=upper;x+=width){
            
            double f2 = eq.fD(x);
            double average = (f2+f1)/2;
            integral += average;
            f1 = f2;
        }
        return integral*width;
    }
    
    public double midpoint(Equation eq,double lower,double upper,int steps){
        double width = (upper-lower)/steps;
        double integral = 0;
        double x1 = lower;
        for(double x=lower+width;x<=upper;x+=width){
            double x2 = x;
            double m = (x2+x1)/2;
            integral += eq.fD(m);
            x1 = x2;
        }
        return integral*width;
    }
    
    public double simpsons(Equation eq,double lower,double upper,int steps){
        double width = (upper-lower)/steps;
        double integral = 0;
        double x = lower;
        for(int s=0;s<=2*steps;s++){
            double f = eq.fD(x);
            int w;
            if(s==0 || s==(2*steps))
                w=1;
            else if(s%2==0)
                w=2;
            else
                w=4;
            integral += f*w;
            x += width/2;
        }
        return integral*width/6;
    }
    
    public double richardson(int steps, double lower,double upper){
        double c = new Equation("2x^2*x^2/(2x^2-x^2)").fD(steps);
        c *= (upper-lower);
        double diff = c/Math.pow(steps*2,2);
        double integral = upper+diff;
        return integral;
    }
}