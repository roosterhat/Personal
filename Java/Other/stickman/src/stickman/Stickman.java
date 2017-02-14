
package stickman;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;

public class Stickman extends Applet {

    public void init()
    {
        this.setSize(1000,650);
    }
    public void paint(Graphics g)
    {
        int x=200;
        int y=200;
        int r=25;
        int l=75;
        //drawCircle(g,200,200,150,Color.GREEN);
        //drawCircle(g,300,200,150,Color.BLUE);
        //drawCircle(g,250,125,150,Color.RED);
        drawCircle(g,x,y,r,Color.BLACK);
        g.drawLine(x+r,y+r,x+r,y+r+l);
        g.drawLine(x,y+r+25,x+2*r,y+r+25);
        g.drawLine(x+r,y+l+r,x,y+r+l+25);
        g.drawLine(x+r,y+l+r,x+2*r,y+r+l+25);
                
    }
    public void drawCircle(Graphics g,int xpos,int ypos, int rad, Color color)
    {
        g.setColor(color);
        g.fillOval(xpos+(rad/2),ypos+(rad/2),rad,rad);
    }
    
}
