/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3d;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

public class Main extends Applet
{

    square shape;
    public void init()
    {
        this.setSize(1000,650);
        shape=new square(100,100,200,100,200,200,100,200);
    }
    public void paint(Graphics g)
    {  
        shape.rotateRight();
        shape.drawShape(g);
        pause(100);
        repaint();
        
    }  
    public void pause(int time)
    {
        try 
        {
        Thread.sleep(time);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }
}

class square
{
    int x1,y1,x2,y2,x3,y3,x4,y4; 
    public square(int a,int b,int c,int d, int e,int f,int g,int h)
    {
        x1=a;
        y1=b;
        x2=c;
        y2=d;
        x3=e;
        y3=f;
        x4=g;
        y4=h;
    }
    public void rotateRight()
    {
        x1+=2;
        x4+=2;
        y1+=1;
        y4-=1;
    }
    public void drawShape(Graphics g)
    {
        g.setColor(Color.RED);
        int[] side1x = {x1,x2,x3,x4};
        int[] side1y = {y1,y2,y3,y4};
        g.fillPolygon(side1x, side1y, side1x.length);
        g.setColor(Color.GREEN);
        int[] side2x = {x4,x2-100,x3-100,x1};
        int[] side2y = {y1,y2,y3,y4};
        g.fillPolygon(side2x,side2y,side2x.length);
        g.setColor(Color.BLUE);
        int[] side3x = {x4,x2+100,x3+100,x1};
        int[] side3y = {y1,y2,y3,y4};
        g.fillPolygon(side3x, side3y, side3x.length);
        g.setColor(Color.YELLOW);
        int[] side4x = {x1,x2,x3,x4};
        int[] side4y = {y1,y2,y3,y4};
        g.fillPolygon(side4x, side4y, side4x.length);
        g.setColor(Color.ORANGE);
        int[] side5x = {x1,x2,x3,x4};
        int[] side5y = {y1,y2,y3,y4};
        g.fillPolygon(side5x, side5y, side5x.length);
        g.setColor(Color.PINK);
        int[] side6x = {x1,x2,x3,x4};
        int[] side6y = {y1,y2,y3,y4};
        g.fillPolygon(side6x, side6y, side6x.length);
        
    }
}