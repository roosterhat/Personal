/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mouse;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class Mouse extends Applet implements MouseListener , MouseMotionListener 
{
    private int xcord,ycord;
    ArrayList<smileyFace> faces = new ArrayList();
    
    
    public void init()
    {
        this.setSize(1000,650);
        //this.setBackground(Color.GRAY);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    public void paint(Graphics g)
    {
        for(int i =0;i<faces.size();i++)
        {
            faces.get(i).drawFace(g);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent me) 
    {
        xcord = me.getX();
        ycord = me.getY();
        smileyFace face=new smileyFace(xcord-(100),ycord-(100),200,25);
        faces.add(face);
        repaint();   
    }

    @Override
    public void mousePressed(MouseEvent me) 
    {
    }

    @Override
    public void mouseReleased(MouseEvent me) 
    {
    }

    @Override
    public void mouseEntered(MouseEvent me) 
    {
        
    }

    @Override
    public void mouseExited(MouseEvent me) 
    {
    }

    @Override
    public void mouseDragged(MouseEvent me) 
    {
    }

    @Override
    public void mouseMoved(MouseEvent me) 
    {
        
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
        g.setColor(Color.BLACK);
        g.drawOval(xpos,ypos,radius,radius);
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
