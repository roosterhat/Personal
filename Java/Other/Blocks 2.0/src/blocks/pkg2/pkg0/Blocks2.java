
package blocks.pkg2.pkg0;

import java.util.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class Blocks2 extends Applet implements MouseListener, MouseMotionListener, KeyListener
{
    private Image dbImage;
    private Graphics dbg;
    ArrayList<Block> objects = new ArrayList();
    int xbound,ybound;
    public void init()
    {
        this.addKeyListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setSize(1000, 650);
        xbound = this.getSize().width;
        ybound = this.getSize().height;
        this.setBackground(Color.BLACK);
        for (int i = 0; i < (Math.random() * 10); i++)
        {
            Color c = new Color((int) (256 * Math.random()), (int) (256 * Math.random()), (int) (256 * Math.random()));
            Block obj = new Block((int) ((xbound-100) * Math.random()), (int) ((ybound-100) * Math.random()), 100, 100, c);
            objects.add(obj);
        }

    }
    
    public void paint(Graphics g)
    {
        xbound = this.getSize().width;
        ybound = this.getSize().height;
        moveBlocks(g);
        pause(40);
        repaint();
    }
    
    public void update(Graphics g)
    {
        // initialize buffer
        if (dbImage == null)
        {

            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics();

        }

        // clear screen in background
        dbg.setColor(getBackground());
        dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

        // draw elements in background
        dbg.setColor(getForeground());
        paint(dbg);

        // draw image on the screen
        g.drawImage(dbImage, 0, 0, this);
    }
    
    public void pause(int time)
    {
        try
        {
            Thread.sleep(time);
        } catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
    
    public void displayBlocks(Graphics g)
    {
        for(Block b:objects)
        {
            b.display(g);
        }
    }
    
    public void moveBlocks(Graphics g)
    {
        
        for(Block b:objects)
        {
            boolean canMove = true;
            for(Block x:objects)
            {
                if(x!=b)
                {
                    if(b.isColliding(x.getXPos(), x.getYPos(), x.getWidth()+x.getXPos(), x.getHeight()+x.getYPos()))
                    {
                        //canMove = false;
                    }
                }
            }
            if(canMove)
            {
                b.setYVel(b.getYVel()+2);
                b.setXVel(b.getXVel()/1.1);
                b.move();
            }
            b.display(g);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
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
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
    
}

class Block
{
    private int x,y;
    final private int width,height;
    private double xVel, yVel;
    final private Color color;
    public Block(int a, int b, int c, int d, Color e)
    {
        x = a;
        y = b;
        width = c;
        height = d;
        color = e;
        xVel = 0;
        yVel = 0;
    }
    public int getXPos()
    {
        return x;
    }
    public int getYPos()
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
    public double getXVel()
    {
        return xVel;
    }
    public double getYVel()
    {
        return yVel;
    }
    public void setXVel(double x)
    {
        xVel = x;
    }
    public void setYVel(double x)
    {
        if(y<550)
        {
            yVel = x;
        }
        else
        {
            //System.out.println("stop: "+yVel);
            yVel = 0;
        }
    }
    public void move()
    {
        System.out.println("test: "+y);
        //x += xVel;
        y += yVel;
    }
    public void display(Graphics g)
    {
        g.setColor(color);
        g.fillRect(x - 1, y - 1, width + 1, height + 1);
    }
    public boolean isColliding(double x1, int y1, int x2, int y2)
    {
        boolean collide = true;
        if(!(y>y1&&y<y2)&&!(y+height>y1&&y+height<y2))
        {
            if(!(x>x1&&x<x2)&&!(x+width>x1&&x+width<x2))
            {
                collide = false;
            }
        }
        return collide;
    }
}
