
package circles;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Circles extends Applet implements MouseMotionListener
{
    int mousex,mousey;
    private Image dbImage;
    private Graphics dbg;
    ArrayList<circle> cyl = new ArrayList();
    public void init() 
    {
        this.setSize(1000,650);
        this.setBackground(Color.BLACK);
        this.addMouseMotionListener(this);
        for(int i=30;i<=200;i+=10)
        {
            circle c = new circle(500,350,210-i,i/15);
            cyl.add(c);
        }
        mousex = 500;
        mousey = 350;
    }
    public void paint(Graphics g)
    {
        for(int i=0;i<cyl.size();i++)
        {
            cyl.get(i).moveCircle(mousex, mousey);
        }
        for(int i=0;i<cyl.size();i++)
        {
            cyl.get(i).drawCircle(g);
            pause(1);
        }
        
        repaint();
    }
    public void update(Graphics g)
    {
                // initialize buffer
        if (dbImage == null)
        {

            dbImage = createImage (this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics ();

        }

        // clear screen in background
        dbg.setColor (getBackground ());
        dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);

        // draw elements in background
        dbg.setColor (getForeground());
        paint (dbg);

        // draw image on the screen
        g.drawImage (dbImage, 0, 0, this); 
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
    @Override
    public void mouseDragged(MouseEvent e) 
    {
    }

    @Override
    public void mouseMoved(MouseEvent e) 
    {
        mousex = e.getX();
        mousey = e.getY();
    }
}
class circle
{
    int xpos,ypos,radius,speed;
    public circle(int x,int y,int r,int s)
    {
        xpos=x;
        ypos=y;
        radius=r;
        speed=s;
    }
    public void drawCircle(Graphics g)
    {
       g.setColor(Color.WHITE);
       g.drawOval(xpos-(radius/2), ypos-(radius/2), radius, radius);
    }
    public void moveCircle(int x,int y)
    {
        int temp=0;
        int temp2=0;
        if(x>xpos)
        {
           temp=1; 
        }
        else if(x<xpos)
        {
            temp=-1;
        }
        if(y>ypos)
        {
            temp2=1;
        }
        else if(y<ypos)
        {
            temp2=-1;
        }
        if(xpos+(speed*temp)>x)
        {
            //xpos+=x-xpos;
        }
        else
        {
            xpos+=(speed*temp);
        }
        if(ypos+(speed*temp2)>y)
        {
            //ypos+=y-ypos;
        }
        else
        {
            ypos+=(speed*temp2);
        }
        
        
    }
}
