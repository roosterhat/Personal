
package polygons;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Polygons extends Applet implements MouseListener,MouseMotionListener
{
    private Image dbImage;
    private Graphics dbg;    
    ArrayList<object> objects = new ArrayList();
    public void init() 
    {
        this.setSize(1000,650);
        //this.setBackground(Color.BLACK);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        int[] temp = {200,300,100};
        int[] temp2 = {10,100,100};
        object poly = new object(temp,temp2,Color.RED);
        objects.add(poly);
    }
    public void paint(Graphics g)
    {        
        for(int i=0;i<objects.size();i++)
        {
            objects.get(i).drawObject(g);
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
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
class object
{
    int[] xpoints;
    int[] ypoints;
    Color color;
    public object(int[] x,int[] y,Color c)
    {
        xpoints = x;
        ypoints = y;
        color = c;
    }
    public void drawObject(Graphics g)
    {
        g.fillPolygon(xpoints, ypoints, xpoints.length);
        System.out.println(xpoints[2]);
    }
}