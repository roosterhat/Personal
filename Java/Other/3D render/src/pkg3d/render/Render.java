/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3d.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author ostlinja
 */
public class Render {

    /**
     * @param args the command line arguments
     */
    DPoint p1 = new DPoint(200,200,0);
    DPoint p2 = new DPoint(250,250,0);
    public static void main(String[] args) {
        new Render().run();
    }
    
    public void run()
    {
        JPanel p = new DrawingPanel(this);
        JFrame application = new JFrame();                            // the program itself
        
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // set frame to exit
                                                                      // when it is closed
        application.add(p);           


        application.setSize(500, 500);         // window is 500 pixels wide, 400 high
        application.setVisible(true);
        for(int i=0;i<100;i++)
        {
            application.repaint();
            p1.rotate(0,0,5,p2);
            //p2.rotate(10,0,0,null);
            waitFor(0.1);
        }
    }
    
    public void waitFor(double time)
    {
        long init =  System.currentTimeMillis();
        while(System.currentTimeMillis()<init+time*1000){}
    }
    
    public DPoint[] getPoints()
    {
        DPoint[] a = {p1,p2};
        return a;
    }
    
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        g2.draw(new Line2D.Double(p1,p2));
    }
    
}

class DrawingPanel extends JPanel
{
    Render main;
    public DrawingPanel()
    {
        super();
    }
    
    public DrawingPanel(Render r)
    {
        super();
        main = r;
    }
    public void paintComponent(Graphics g)  // draw graphics in the panel
    {
        int width = getWidth();             // width of window in pixels
        int height = getHeight();           // height of window in pixels

        super.paintComponent(g);            // call superclass to make panel display correctly

        Graphics2D g2 = (Graphics2D)g;
        if(main!=null)
        {
            DPoint a = main.getPoints()[0];
            DPoint b = main.getPoints()[1];
            g2.draw(new Line2D.Double(new Point2D.Double(a.getX(),a.getY()),new Point2D.Double(b.getX(),b.getY())));
        }
        // Drawing code goes here
    }

    
}

class DObject
{
    ArrayList<DPoint> vertecies = new ArrayList();
    DPoint center;
    
    public DObject()
    {    }
    public DObject(ArrayList p)
    {
        vertecies = (ArrayList)p.clone();
        if(!vertecies.isEmpty())
            center = vertecies.get(0);
    }
    
    public DObject(ArrayList p, DPoint c)
    {
        vertecies = (ArrayList)p.clone();
        center  = c;
    }
    
    public DPoint getCenter()
    {
        return center;
    }
    
    public ArrayList<DPoint> getVertecies()
    {
        return vertecies;
    }
    
    public void drawObject()
    {
        
    }
    
    public void rotate(double h, double v, double l)
    {
        DPoint z = new DPoint(0,0,0);
        rotate(h,v,l,z);
    }
    
    public void rotate(double h, double v, double l, DPoint c)
    {
        center.rotate(v, h, l, c);
        for(DPoint p: vertecies)
        {
            p.rotate(v, h, l, c);
        }
    }
    
    public void translate(int dx,int dy,int dz)
    {
        center.translate(dx, dy, dz);
        for(DPoint p: vertecies)
        {
            p.translate(dx, dy, dz);
        }
    }
}

class DPoint extends Point
{ 
    int z;
    
    public DPoint()
    {super();}
    
    public DPoint(int a,int b,int c)
    {
        super(a,b);
        z=c;
    }
    
    public DPoint getLocation()
    {return this;}
    
    public double getZ()
    {return z;}
    
    public double distance(DPoint p)
    {
        return Math.sqrt(Math.pow((x-p.getX()),2)+Math.pow((y-p.getY()), 2)+Math.pow((z-p.getZ()), 2));
    }
    
    public void moveTo(int a, int b, int c)
    {x=a;y=b;z=c;}
    
    public void moveTo(DPoint p)
    {
        x = (int)p.getX();
        y = (int)p.getY();
        z = (int)p.getZ();
    }
    
    public void translate(int dx,int dy,int dz)
    {
        x += dx;
        y += dy;
        z += dz;
    }
    
    private void rotateZ(double t, DPoint c)
    {
        x = (int)(x*Math.cos(t)-y*Math.sin(t)+c.getX());
        y = (int)(y*Math.cos(t)-x*Math.sin(t)+c.getY());
    }
    
    private void rotateX(double t, DPoint c)
    {
        x = (int)(x*Math.cos(t)-z*Math.sin(t)+c.getX());
        z = (int)(z*Math.cos(t)-x*Math.sin(t)+c.getZ());
    }
    
    private void rotateY(double t, DPoint c)
    {
        y = (int)(y*Math.cos(t)-z*Math.sin(t)+c.getY());
        z = (int)(z*Math.cos(t)-y*Math.sin(t)+c.getZ());
    }
    
    public void rotate(double v, double h, double l, DPoint c)
    {
        x -= c.getX();
        y -= c.getY();
        z -= c.getX();
//        x = (int)(x*Math.cos(l)-y*Math.sin(l));
//        y = (int)(y*Math.cos(l)-x*Math.sin(l));
//        x = (int)(x*Math.cos(h)-z*Math.sin(h));
//        z = (int)(z*Math.cos(h)-x*Math.sin(h));
//        y = (int)(y*Math.cos(v)-z*Math.sin(v));
//        z = (int)(z*Math.cos(v)-y*Math.sin(v));
        rotateX(h,c);
        rotateY(v,c);
        rotateZ(l,c);

    }
    
    public String toString()
    {
        return x+","+y+","+z;
    }
            
}

