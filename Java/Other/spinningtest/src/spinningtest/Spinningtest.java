package spinningtest;

import java.applet.Applet;
import java.awt.*;
import java.util.*;

public class Spinningtest extends Applet
{

    private Image dbImage;
    private Graphics dbg;
    int x, y, x2, y2, x3, y3, x4, y4;
    int centerx = 500;
    int centery = 300;
    int count = 0;
    int w = 100;
    int l = 100;
    double r = (Math.sqrt(Math.pow(w / 2, 2) + Math.pow(l / 2, 2))) / 90;

    public void init()
    {
        this.setSize(1000, 650);
        //this.setBackground(Color.BLACK);
        System.out.println(r);
    }

    public void paint(Graphics g)
    {
        //g.drawLine(x,y,x2,y2);
        //g.drawLine(x3,y3,x4,y4);
        int[] xpos =
        {
            x, x2, x3, x4
        };
        int[] ypos =
        {
            y, y2, y3, y4
        };
        g.drawString("1", x, y);
        g.drawString("2", x2, y2);
        g.drawString("3", x3, y3);
        g.drawString("4", x4, y4);
        g.drawPolygon(xpos, ypos, 4);
        pause(40);
        test2();
        //System.out.println(10*(Math.sin(Math.toDegrees(10*count))));
        count++;
        repaint();
    }

    public void test1()
    {
        x = (int) (r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count))))) + centerx;
        y = (int) (-r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count))))) + centery;

        x2 = (int) (r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count + (w)))))) + centerx;
        y2 = (int) (-r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count + (w)))))) + centery;

        x3 = (int) (-r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count))))) + centerx;
        y3 = (int) (r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count))))) + centery;

        x4 = (int) (-r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count + w))))) + centerx;
        y4 = (int) (r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count + w))))) + centery;
    }

    public void test2()
    {
        x = (int) (r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count))))) + centerx;
        y = (int) (-r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count))))) + centery;

        x2 = (int) (r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count + (w)))))) + centerx;
        y2 = (int) (-r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count + (w)))))) + centery;

        x3 = (int) (-r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count + (w + l/2)))))) + centerx;
        y3 = (int) (r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count + (w + l/2)))))) + centery;

        x4 = (int) (-r * Math.toDegrees(Math.sin(Math.toDegrees(.001 * (count + (w + l + w)))))) + centerx;
        y4 = (int) (r * Math.toDegrees(Math.cos(Math.toDegrees(.001 * (count + (w + l + w)))))) + centery;
    }

    public void test3()
    {
        x = (int) (Math.toDegrees(Math.cos(Math.toDegrees(count))) * (x - centerx) - Math.toDegrees(Math.sin(Math.toDegrees(count) * (y - centery))) + centerx);
        y = (int) (Math.toDegrees(Math.sin(Math.toDegrees(count))) * (x - centerx) + Math.toDegrees(Math.cos(Math.toDegrees(count) * (y - centery))) + centery);

        x2 = (int) (Math.toDegrees(Math.cos(Math.toDegrees(count))) * (x - centerx) - Math.toDegrees(Math.sin(Math.toDegrees(count) * (y - centery))) + centerx);
        y2 = (int) (Math.toDegrees(Math.sin(Math.toDegrees(count))) * (x - centerx) + Math.toDegrees(Math.cos(Math.toDegrees(count) * (y - centery))) + centery);

        x3 = (int) (Math.toDegrees(Math.cos(Math.toDegrees(count))) * (x - centerx) - Math.toDegrees(Math.sin(Math.toDegrees(count) * (y - centery))) + centerx);
        y3 = (int) (Math.toDegrees(Math.sin(Math.toDegrees(count))) * (x - centerx) + Math.toDegrees(Math.cos(Math.toDegrees(count) * (y - centery))) + centery);

        x4 = (int) (Math.toDegrees(Math.cos(Math.toDegrees(count))) * (x - centerx) - Math.toDegrees(Math.sin(Math.toDegrees(count) * (y - centery))) + centerx);
        y4 = (int) (Math.toDegrees(Math.sin(Math.toDegrees(count))) * (x - centerx) + Math.toDegrees(Math.cos(Math.toDegrees(count) * (y - centery))) + centery);
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
}
