/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ball;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.*;

public class Ball extends Applet 
{
    private Image dbImage;
    private Graphics dbg;   
    ArrayList<objectBall> balls = new ArrayList(); 
    int size = 10;
    public  void init() 
    {
        this.setSize(1000,650);
        this.setBackground(Color.GRAY);
        for(int i=0;i<size;i++)
        {
            int x = (int)(1000*Math.random());
            int y = (int)(300*Math.random());
            int vel = (int)(20*Math.random());
            Color c = new Color((int)(256*Math.random()),(int)(256*Math.random()),(int)(256*Math.random())); 
            objectBall ball1=new objectBall(x,y,20,vel,0,c);
            balls.add(ball1);
        }
       
    }
    public void paint(Graphics g)
    {
        for(int i =0;i<size;i++)
        {
            testCollision(i);
            balls.get(i).moveBall();
            balls.get(i).drawBall(g);
            balls.get(i).checkBounds();
        }
        pause(40);
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
    
    public void testCollision(int index)
    {
        for(int i=0;i<balls.size();i++)
        {
            if(i!=index)
            {
                if(balls.get(index).getX()>=balls.get(i).getX() && balls.get(index).getX()<=balls.get(i).getX()+(balls.get(i).getRadius()*2))
                {
                    if(balls.get(index).getY()>=balls.get(i).getY() && balls.get(index).getY()<=balls.get(i).getY()+(balls.get(i).getRadius()*2))
                    {
                        int b1xv = balls.get(index).getXvel();
                        int b1yv = balls.get(index).getYvel();
                        int b2xv = balls.get(i).getXvel();
                        int b2yv = balls.get(i).getYvel();
                        balls.get(index).setXvel((int)(b2xv*1));
                        balls.get(index).setYvel((int)(b2yv*1));
                        balls.get(i).setXvel((int)(b1xv*1));
                        balls.get(i).setYvel((int)(b1yv*1));
                    }
                }
            }
        }
    }
}

class objectBall
{
    Color color;
    int xpos,ypos,xvel,yvel,xacel,yacel;
    int rad;
    public objectBall(int x,int y,int radius,int xvelocity,int yvelocity,Color ballColor)
    {
        xpos = x;
        ypos = y;
        rad = radius;
        xvel = xvelocity;
        yvel = yvelocity;
        color = ballColor;
    }
    public void moveBall()
    {
        xpos += xvel;
        ypos += yvel;
        xvel *= 1;
        yvel += 2;
    }
    public void drawBall(Graphics g)
    {
        g.setColor(color);
        g.fillOval(xpos,ypos,rad,rad);
    }
    public void printPosition()
    {
        System.out.println("("+xpos+","+ypos+")"); 
        System.out.println();
    }
    public void checkBounds()
    {
        if(xpos<1||xpos+rad>999)
        {
            xvel*=-1;
        }
        if(ypos+rad>610)
        {
            yvel*=-1;
            yvel+=2;
        }
    }
    public int getX()
    {
        return xpos;
    }
    public int getY()
    {
        return ypos;
    }
    public int getXvel()
    {
        return xvel;
    }
    public int getYvel()
    {
        return yvel;
    }
    public int getRadius()
    {
        return rad;
    }
    public void setXvel(int xv)
    {
        xvel = xv;
    }
    public void setYvel(int yv)
    {
        yvel = yv;
    }
}
