/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package screentyping;
import java.util.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;

public class Screentyping extends Applet implements MouseListener,KeyListener 
{
    private int xpos,ypos;
    String word = "";
    String displayword = "";
    int x=100;
    int y=100;
    boolean typing = false;
    int  blink  = 0;
    String mask = "";
    public void init()
    {
        this.setSize(1000,650);
        this.setBackground(Color.BLUE);
        this.addMouseListener(this);
        this.addKeyListener(this);
    }
    
    public void paint(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 200, 25);
        g.setColor(Color.BLACK);
        g.drawString(displayword, x+5, y+((y+35)-y)/2);
        if(xpos>=x && xpos<=x+200)
        {
            if(ypos>=y && ypos<=y+25)
            {
                typing = true;
                xpos=1;
                ypos=1;
            }
            else
            {
                typing = false;
            }
        }
        blinker(g);
        pause(50);
        
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
    
    public void blinker(Graphics g)
    {
        if(typing)
        {
            if(blink>=500)
            {
                g.drawString(displayword+"|",x+5, y+((y+35)-y)/2);
                blink = 0;
            }
            else
            {
                blink += 50;
            }
            //System.out.println(blink);
        }
        repaint();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) 
    {
        xpos = e.getX();
        ypos = e.getY();
        repaint();
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
    public void keyTyped(KeyEvent e) 
    {
        
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        if(typing)
        {
            if(e.getKeyCode()==10)
            {
               typing = false; 
               System.out.println("Word: "+word);
               word = "";
               displayword = "";
            }
            else if(e.getKeyCode()==8)
            {
                if(word.length()>0)
                {
                    word = word.substring(0,word.length()-1);
                    displayword = displayword.substring(0,displayword.length()-1);
                }
            }
            else if(e.getKeyCode()!=16)
            {
                word+=e.getKeyChar();
                if(mask==null||mask=="")
                {
                    displayword+=e.getKeyChar();
                }
                else
                {
                    displayword+=mask;
                }
            }
            //System.out.println(e.getKeyCode());
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) 
    {
    }
}
