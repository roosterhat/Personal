
package BreakOut;
import games.GamesCommon;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class Breakout extends Applet implements MouseListener,MouseMotionListener,GamesCommon
{
    private String name = "Break Out";
    private Image dbImage;
    private Graphics dbg;
    ArrayList<brick> bricks = new ArrayList();
    ball gameball;
    paddle gamepaddle;
    int lives = 3;
    boolean start = false;
    
    public String getName()
    {
        return name;
    }
    
    public void run()
    {
        init();
    }
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
        if(gameball.getDimensions().getX()<=1 || gameball.getDimensions().getX()+10>=1000)
        {
            gameball.setXVelocity(gameball.getXVeloxity()*-1);
        }
        if(gameball.getDimensions().getY()<=1)
        {
            gameball.bounceY();
        }
        if(gameball.getDimensions().getY()>=650)
        {
            start=false;
            gameball.getDimensions().setY(550);
            gameball.bounceY();
            gameball.setXVelocity(0);
        }
    }
    public void checkBrickCollision()
    {
        if(gameball.getDimensions().getY()<=140)
        {
            for(int i=0;i<bricks.size();i++)
            {
                if(gameball.checkCollision(bricks.get(i).getDimensions()) && bricks.get(i).getVisibility())
                {
                    bricks.get(i).setVisibility(false);
                    gameball.bounceY();
                }
//                if(gameball.getY()<=bricks.get(i).getY()+20 && gameball.getY()>=bricks.get(i).getY())
//                {
//                    if(gameball.getX()>=bricks.get(i).getX() && gameball.getX()<=(bricks.get(i).getX()+50))
//                    {
//                        if(bricks.get(i).getVisibility())
//                        {
//                            bricks.get(i).setVisibility(false);
//                            gameball.bounceY();
//                        }
//                    }
//                }
            }
        }
    }
    public void checkPaddleCollision()
    {
        if(gameball.getDimensions().getY()==gamepaddle.getDimensions().getY())
        {
            if(gameball.checkCollision(gamepaddle.getDimensions()))
            {
                gameball.bounceY();
                gameball.setXVelocity((int)(gameball.getXVeloxity()+(-.25*(50-(gameball.getDimensions().getX()-gamepaddle.getDimensions().getX())))));
            }
//            if(gameball.getX()-9>=gamepaddle.getX() && gameball.getX()<=gamepaddle.getX()+gamepaddle.getWidth())
//            {
//                gameball.bounceY();
//                gameball.setXVelocity((int)(gameball.getXVeloxity()+(-.25*(50-(gameball.getX()-gamepaddle.getX())))));
//            }
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
            gameball.getDimensions().setX(e.getX());
        }
    }
}
class paddle
{
    Dimension d;
    int score;
    public paddle(int x,int y,int s)
    {
        d = new Dimension(x,y,s,10);
        score = 0;
    }
    public void drawPaddle(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(d.getX(),d.getY(),d.getWidth(),d.getHeight());
    }
    public Dimension getDimensions()
    {
        return d;
    }
    public void score()
    {
        score+=10;
    }
    public void movePaddle(int x)
    {
        d.setX(x-(d.getWidth()/2));
    }
}

class ball
{
    Dimension d;
    int xvel,yvel,radius;
    public ball(int x,int y,int xv,int yv)
    {
        d = new Dimension(x,y);
        xvel = xv;
        yvel = yv;
        radius = 10;
    }
    public void drawBall(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillOval(d.getX(),d.getY(),radius,radius);
    }
    public void moveXBall()
    {
        d.setX(d.getX()+xvel);
    }
    public void moveYBall()
    {
        d.setY(d.getY()+(yvel/Math.abs(yvel))*1);
    }
    public void bounceY()
    {
        yvel*=-1;
    }
    public void setXVelocity(int xv)
    {
        xvel = xv;
    }
   
    public int getXVeloxity()
    {
        return xvel;
    }
    public Dimension getDimensions()
    {
        return d;
    }
    public boolean checkCollision(Dimension o)
    {
        if(d.getX()+radius>=o.getX() && d.getX()+radius<=o.getX()+o.getWidth())
            if(d.getY()+radius>=o.getY() && d.getY()+radius<=o.getY()+o.getHeight())
                return true;
        return false;
    }
}
class brick
{
    Dimension d;
    boolean visible;
    Color color;
    public brick(int x,int y,boolean vis,Color c)
    {
        d = new Dimension(x,y,50,20);
        visible = vis;
        color = c;
    }
    public void drawBrick(Graphics g)
    {
        if(visible)
        {
            g.setColor(color);
            g.fillRect(d.getX(),d.getY(),d.getWidth(),d.getHeight());
        }
    }
    public Dimension getDimensions()
    {
        return d;
    }
    public void setVisibility(boolean input)
    {
        visible = input;
    }
    public boolean getVisibility()
    {
        return visible;
    }
}

class Dimension
{
    int x,y,width,height;
    public Dimension(int a, int b, int c, int d)
    {
        x = a;
        y = b;
        width  = c;
        height = d;
    }
    public Dimension(int a,int b)
    {
        x = a;
        y = b;
        width = 0;
        height = 0;
    }
    public void setX(int a)
    {
        x = a;
    }
    public void setY(int a)
    {
        y = a;
    }
    public void setWidth(int a)
    {
        width = a;
    }
    public void setheight(int a)
    {
        height = a;
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }
}