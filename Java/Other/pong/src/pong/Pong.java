
package pong;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class Pong extends Applet implements KeyListener
{
    private Image dbImage;
    private Graphics dbg;   
    paddle player1;
    paddle player2;
    ball gameball;
    int size = 100;
    Timer timer;
    public void init()
    {
        this.setSize(1000,650);
        this.setBackground(Color.BLACK);
        this.addKeyListener(this);
        player1 = new paddle(1,275,size,0);
        player2 = new paddle(990,275,size,0);
        gameball = new ball(500,325,15,0);
       
    }
    public void paint(Graphics g)
    {
        drawBackground(g);
        player1.drawPaddle(g);
        player2.drawPaddle(g);
        gameball.moveBall();
        gameball.drawBall(g);
        String score = gameball.checkCollisonWall();
        if(score=="player1")
        {
            player1.addPoint();
        }
        else if(score=="player2")
        {
            player2.addPoint();
        }
        gameball.checkCollisonPaddle(player1.getYPos(),player2.getYPos(),size);
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
    public void drawBackground(Graphics g)
    {
        g.setColor(Color.GREEN);
        for(int i=0;i<=650;i+=50)
        {
            g.fillRect(500,i,10,25);
        }
        g.drawString(Integer.toString(player1.getScore()), 400, 10);
        g.drawString(Integer.toString(player2.getScore()), 600, 10);
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
    public void keyTyped(KeyEvent e) 
    {
    }

    @Override
    public void keyPressed(KeyEvent e) //<-- Method Used
    {
        int keyID = e.getKeyCode();
        if(keyID==38)
        {
            player2.movePaddleUp();
        }
        if(keyID==87)
        {
            player1.movePaddleUp();
        }
        if(keyID==40)
        {
            player2.movePaddleDown();
        }
        if(keyID==83)
        {
            player1.movePaddleDown();
        }
        //repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) 
    {
    }
}
class paddle
{
    int xpos,ypos,length,score;
    public paddle(int x,int y,int size,int s)
    {
        xpos = x;
        ypos = y;
        length = size;
        score = s;
    }
    public void movePaddleUp()
    {
        if(ypos>1)
        {
            ypos-=20;
        }
    }
    public void movePaddleDown()
    {
        if(ypos+length<650)
        {
            ypos+=20;
        }
    }
    public void drawPaddle(Graphics g)
    {
        g.setColor(Color.GREEN);
        g.fillRect(xpos,ypos,10,length);
    }
    public int getXPos()
    {
        return xpos;
    }
    public int getYPos()
    {
        return ypos;
    }
    public int getScore()
    {
        return score;
    }
    public void addPoint()
    {
        score++;
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
        g.setColor(Color.GREEN);
        g.fillRect(xpos, ypos, 10, 10);
    }
    public void moveBall()
    {
        xpos += xvel;
        ypos += yvel;
    }
    public String checkCollisonWall()
    {
        if(ypos<=1)
        {
            yvel*=-1;
        }
        if(ypos>=640)
        {
            yvel*=-1;
        }
        if(xpos<=1)
        {
            xvel*=-1;
            yvel = 0;
            xpos = 500;
            ypos = 350;
            return "player2";
        }
        if(xpos>=990)
        {
            xvel*=-1;
            yvel = 0;
            xpos = 500;
            ypos = 350;
            return "player1";
        }
        return "no one";
    }
    public void checkCollisonPaddle(int y,int y2,int s)
    {
       
        if(xpos<=10)
        {
            if(ypos-9>=y && ypos<=y+s+9)
            {
                xvel *= -1;
                yvel +=-.5*(50-(ypos-y));
            }
        }

        if(xpos>=980)
        {
           if(ypos>=y2-9 && ypos<=y2+s+9)
            {
                xvel *= -1;
                yvel +=-.5*(50-(ypos-y2));
            } 
        }
        
    }
}