
package breakout;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class Breakout extends Applet implements MouseListener,MouseMotionListener
{
    private Image dbImage;
    private Graphics dbg;
    ArrayList<brick> bricks = new ArrayList();
    ball gameball;
    paddle gamepaddle;
    int lives = 3;
    boolean start = false;
    public void init()
    {
        this.setSize(1000,650);
        this.setBackground(Color.BLACK);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        for(int y=0;y<6;y++)
        {
            if(y%2==0)
            {
                for(int i=0;i<1000;i+=50)
                {
                    Color c = new Color((int)(256*Math.random()),(int)(256*Math.random()),(int)(256*Math.random()));
                    brick b = new brick(i,(y+1)*20,true,c);
                    bricks.add(b);
                }
             }
            else
            {
                for(int i=25;i<950;i+=50)
                {
                    Color c = new Color((int)(256*Math.random()),(int)(256*Math.random()),(int)(256*Math.random()));
                    brick b = new brick(i,(y+1)*20,true,c);
                    bricks.add(b);
                }
            }
        }
        gameball = new ball(500,550,0,-15);
        gamepaddle = new paddle(450,600,100);
    }
    
    public void paint(Graphics g)
    {
        for(int i=0;i<bricks.size();i++)
        {
            bricks.get(i).drawBrick(g);
        }
        gameball.drawBall(g);
        if(start)
        {
            moveBall();
        }
        gamepaddle.drawPaddle(g);
        pause(40);
        if(gamepaddle.score!=bricks.size()-1 && lives>0)
        {
            repaint();
        }
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
    
    public void moveBall()
    {
        gameball.moveXBall();
        for(int i=1;i<=15;i++)
        {
            gameball.moveYBall();
            checkWallCollision();
            checkBrickCollision();
            checkPaddleCollision();
        }
    }
    public void checkWallCollision()
    {
        if(gameball.getX()<=1 || gameball.getX()+10>=1000)
        {
            gameball.setXVelocity(gameball.getXVeloxity()*-1);
        }
        if(gameball.getY()<=1)
        {
            gameball.bounceY();
        }
        if(gameball.getY()>=650)
        {
            start=false;
            gameball.setY(550);
            gameball.bounceY();
            gameball.setXVelocity(0);
        }
    }
    public void checkBrickCollision()
    {
        if(gameball.getY()<=140)
        {
            for(int i=0;i<bricks.size();i++)
            {
                if(gameball.getY()<=bricks.get(i).getY()+20 && gameball.getY()>=bricks.get(i).getY())
                {
                    if(gameball.getX()>=bricks.get(i).getX() && gameball.getX()<=(bricks.get(i).getX()+50))
                    {
                        if(bricks.get(i).getVisibility())
                        {
                            bricks.get(i).setVisibility(false);
                            gameball.bounceY();
                        }
                    }
                }
            }
        }
    }
    public void checkPaddleCollision()
    {
        if(gameball.getY()==gamepaddle.getY())
        {
            if(gameball.getX()-9>=gamepaddle.getX() && gameball.getX()<=gamepaddle.getX()+gamepaddle.getSize())
            {
                gameball.bounceY();
                gameball.setXVelocity((int)(gameball.getXVeloxity()+(-.25*(50-(gameball.getX()-gamepaddle.getX())))));
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) 
    {
        start = true;
    }

    @Override
    public void mousePressed(MouseEvent e) 
    {
    }

    @Override
    public void mouseReleased(MouseEvent e) 
    {
    }

    @Override
    public void mouseEntered(MouseEvent e) 
    {
    }

    @Override
    public void mouseExited(MouseEvent e) 
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    @Override
    public void mouseMoved(MouseEvent e) 
    {
        gamepaddle.movePaddle(e.getX());
        if(!start)
        {
            gameball.setX(e.getX());
        }
    }
}
class paddle
{
    int xpos,ypos,size,score;
    public paddle(int x,int y,int s)
    {
        xpos=x;
        ypos=y;
        size=s;
        score = 0;
    }
    public void drawPaddle(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(xpos,ypos,size,10);
    }
    public int getX()
    {
        return xpos;
    }
    public int getY()
    {
        return ypos;
    }
    public int getSize()
    {
        return size;
    }
    public void score()
    {
        score++;
    }
    public void movePaddle(int x)
    {
        xpos = x-(size/2);
    }
}

class ball
{
    int xpos,ypos,xvel,yvel;
    public ball(int x,int y,int xv,int yv)
    {
        xpos = x;
        ypos = y;
        xvel = xv;
        yvel = yv;
    }
    public void drawBall(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillOval(xpos,ypos,10,10);
    }
    public void moveXBall()
    {
        xpos+=xvel;
    }
    public void moveYBall()
    {
        ypos+=(yvel/Math.abs(yvel))*1;
    }
    public void bounceY()
    {
        yvel*=-1;
    }
    public void setXVelocity(int xv)
    {
        xvel = xv;
    }
    public void setX(int x)
    {
        xpos = x;
    }
    public void setY(int y)
    {
        ypos = y;
    }
    public int getXVeloxity()
    {
        return xvel;
    }
    public int getX()
    {
        return xpos;
    }
    public int getY()
    {
        return ypos;
    }
}
class brick
{
    int xpos,ypos;
    boolean visible;
    Color color;
    public brick(int x,int y,boolean vis,Color c)
    {
        xpos = x;
        ypos = y;
        visible = vis;
        color = c;
    }
    public void drawBrick(Graphics g)
    {
        if(visible)
        {
            g.setColor(color);
            g.fillRect(xpos,ypos,50,20);
        }
    }
    public void setVisibility(boolean input)
    {
        visible = input;
    }
    public int getX()
    {
        return xpos;
    }
    public int getY()
    {
        return ypos;
    }
    public boolean getVisibility()
    {
        return visible;
    }
}