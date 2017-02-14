package light;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;


public class Light extends Applet implements MouseListener
{

    private Image dbImage;
    private Graphics dbg;
    //int x = 500;
    //int y = 300;
    ArrayList<lightSource> lights = new ArrayList();

    public void init()
    {
        this.setSize(1000, 1000);
        this.setBackground(Color.BLACK);
        this.addMouseListener(this);
        //lightSource l1 = new lightSource(500, 300);
        //lights.add(l1);
        //lightSource l2 = new lightSource(900, 600);
        //lights.add(l2);
    }

    public void paint(Graphics g)
    {
        for (int i = 0; i < lights.size(); i++)
        {
            lights.get(i).drawLight(g);
        }
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

    @Override
    public void mouseClicked(MouseEvent e)
    {
        lightSource l1 = new lightSource(e.getX(),e.getY());
        lights.add(l1);
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
}
class lightSource
{

    int xpos, ypos;

    public lightSource(int x, int y)
    {
        xpos = x;
        ypos = y;
    }

    public void drawLight(Graphics g)
    {
        for (int i = 0; i < 250; i++)
        {
            Color c = new Color(i, i, i);
            g.setColor(c);
            g.fillOval(xpos - (500 - (i * 2)) / 2, ypos - (500 - (i * 2)) / 2, 500 - (i * 2), 500 - (i * 2));
            g.setColor(Color.YELLOW);
            g.fillOval(xpos - 5, ypos - 5, 10, 10);
        }
    }
}