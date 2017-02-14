/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package face;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

public class Face extends Applet
{
    ArrayList<smileyFace> faces = new ArrayList();
    public void init() 
    {
        this.setSize(1000,650);
        for(int i=0;i<100;i++)
        {
            int cx = (int)(1000*Math.random());
            int cy = (int)(650*Math.random());
            smileyFace smile=new smileyFace(cx,cy,200,25);
            faces.add(smile);
        }
    }
    public void paint(Graphics g)
    {  
        for(int i =0;i<100;i++)
        {
            faces.get(i).drawFace(g);
        }
    }  
}
class smileyFace
{
    private int xpos, ypos, radius, radius2;
    
    public smileyFace(int cx,int cy,int rad1, int rad2)
    {
        xpos = cx;
        ypos = cy;
        radius = rad1;
        radius2= rad2;
    }
    public void drawFace(Graphics g)
    {
        g.setColor(Color.YELLOW);
        g.fillOval(xpos,ypos,radius,radius);
        g.setColor(Color.WHITE);
        g.fillOval(xpos+(radius/4),ypos+(radius/3),radius2,radius2);
        g.fillOval(xpos+(3*radius/4)-20,ypos+(radius/3),radius2,radius2);
        g.setColor(Color.BLACK);
        g.drawArc(xpos+(radius/4), ypos+(2*radius/3), radius/2, radius2, 180, 180);
        //g.drawLine(xpos+(radius/2),ypos+(radius/2) , xpos+(radius/2)+20, xpos+(radius/2)+10);
        //g.drawLine(xpos+(radius/2),ypos+(radius/2)+20 , xpos+(radius/2)+20, xpos+(radius/2)+10);
        g.fillOval(xpos+(radius/4)+3, ypos+(radius/3)+2, 10, 10);
        g.fillOval(xpos+(3*radius/4)-20+10, ypos+(radius/3)+10, 10, 10);
    }
}
