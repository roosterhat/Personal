package blocks;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Blocks extends Applet implements MouseListener, MouseMotionListener, KeyListener
{

    private Image dbImage;
    private Graphics dbg;
    int mousex, mousey, diffX, diffY, startx, starty, xbound, ybound, objCount;
    int selected = -1;
    int a, b, c, d, oldx, oldy;
    boolean newobject = false;
    boolean gravity = true;
    boolean collisions = true;
    boolean physics = true;
    boolean menu = false;
    ArrayList<object> objects = new ArrayList();

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
            object obj = new object((int) (1000 * Math.random()), (int) (650 * Math.random()), 100, 100, i, c);
            objects.add(obj);
            objCount++;
        }

    }

    public void paint(Graphics g)
    {
        xbound = this.getSize().width;
        ybound = this.getSize().height;
        drawOptions(g);
        for (int i = 0; i < objects.size(); i++)
        {
            objects.get(i).drawObject(g);
        }

        if (newobject)
        {
            drawSelectionBox(g);
        }
        moveObjects();
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

    public void drawOptions(Graphics g)
    {
        g.setColor(Color.RED);
        if (gravity)
        {
            g.setColor(Color.GREEN);
        }
        g.drawString("Gravity", 1, 10);
        g.setColor(Color.RED);
        if (collisions)
        {
            g.setColor(Color.GREEN);
        }
        g.drawString("Collisions", 45, 10);
        g.setColor(Color.RED);
        if (physics)
        {
            g.setColor(Color.GREEN);
        }
        g.drawString("Physics", 110, 10);

    }

    public void drawMenu(Graphics g, int x, int y, int index)
    {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 100, 300);
        g.setColor(Color.GRAY);
        g.fillRect(x, y, 100, 20);
        g.setColor(Color.WHITE);
        g.drawString("Block " + Integer.toString(index), x, y);
    }

    public void drawSelectionBox(Graphics g)
    {
        if (startx > mousex)
        {
            a = mousex;
            c = startx;
        } else
        {
            a = startx;
            c = mousex;
        }
        if (starty > mousey)
        {
            b = mousey;
            d = starty;
        } else
        {
            b = starty;
            d = mousey;
        }
        g.setColor(Color.lightGray);
        g.drawRect(a, b, c - a, d - b);
    }

    public void moveObjects()
    {
        int count = 0;
        for (int i = 0; i < objects.size(); i++)
        {
            if (!physics)
            {
                objects.get(i).setXvel(0);
            }
            if (gravity)
            {
                checkBelow(i, true);
                for (int x = 1; x <= Math.abs(objects.get(i).getYvel()); x++)
                {
                    if (!checkBelow(i, false) && i != selected)
                    {
                        if (objects.get(i).getY() + objects.get(i).getYlen() < ybound)
                        {
                            objects.get(i).moveObject(objects.get(i).getYvel() / Math.abs(objects.get(i).getYvel()));
                        }
                    }
                    if (i != selected && count <= Math.abs(objects.get(i).getXvel()))
                    {
                        if (objects.get(i).getX() >= 1 && objects.get(i).getX() <= xbound - objects.get(i).getXlen() && !checkSides(i))
                        {
                            //System.out.println("move "+objects.get(i).getXvel());
                            objects.get(i).setX(objects.get(i).getX() + (int) (objects.get(i).getXvel() / Math.abs(objects.get(i).getXvel())));
                            count++;
                        }
                    }
                }
                if (count <= Math.abs(objects.get(i).getXvel()))
                {
                    for (int x = count; x <= Math.abs(objects.get(i).getXvel()); x++)
                    {
                        if (objects.get(i).getX() >= 1 && objects.get(i).getX() <= xbound - objects.get(i).getXlen() && i != selected && !checkSides(i))
                        {
                            objects.get(i).setX(objects.get(i).getX() + (int) (objects.get(i).getXvel() / Math.abs(objects.get(i).getXvel())));
                            count++;
                        }
                    }
                }
                objects.get(i).setXvel(objects.get(i).getXvel() * .9);
                //System.out.println(objects.get(0).getXvel()+"-"+count);
            }
        }
    }

    public void rotateObject(int index)
    {
        int temp = objects.get(index).getXlen();
        objects.get(index).setXlen(objects.get(index).getYlen());
        objects.get(index).setYlen(temp);
    }

    public boolean checkBelow(int index, boolean first)
    {
        boolean below = false;
        int x = objects.get(index).getX();
        int y = objects.get(index).getY();
        int x2 = x + objects.get(index).getXlen();
        int y2 = y + objects.get(index).getYlen();
        Graphics g;
        for (int i = 0; i < objects.size(); i++)
        {
            if (i != index)
            {
                if (objects.get(i).getY() - 1 <= y2 && (objects.get(i).getYlen() + objects.get(i).getY()) >= y)
                {
                    if (x >= objects.get(i).getX() && x <= (objects.get(i).getXlen() + objects.get(i).getX()))
                    {
                        below = true;
                        break;
                    } else if (x2 >= objects.get(i).getX() && x2 <= (objects.get(i).getXlen() + objects.get(i).getX()))
                    {
                        below = true;
                        break;
                    } else if (x <= objects.get(i).getX() && x2 >= objects.get(i).getX())
                    {
                        below = true;
                        break;
                    }
                } else
                {
                    below = false;
                }
            }
        }
        if (!collisions)
        {
            below = false;
        }
        if (below)
        {
            objects.get(index).setYvel(0);
            //objects.get(index).setXvel(objects.get(index).getXvel()*.1);
        } else if (first && selected != index)
        {
            objects.get(index).setYvel(objects.get(index).getYvel() + 4);
        }
        return below;
    }

    public boolean checkSides(int index)
    {
        int x = objects.get(index).getX() - 1;
        int y = objects.get(index).getY();
        int x2 = x + objects.get(index).getXlen() + 2;
        int y2 = y + objects.get(index).getYlen();
        boolean collision = false;
        for (int i = 0; i < objects.size(); i++)
        {
            if (i != index && collisions)
            {

                if (objects.get(i).getY() <= y2 && (objects.get(i).getYlen() + objects.get(i).getY()) >= y)
                {
                    if (x >= objects.get(i).getX() && x <= (objects.get(i).getXlen() + objects.get(i).getX()))
                    {
                        collision = true;
                        //System.out.println(objects.get(i).getXvel());
                        if (objects.get(i).getXvel() == 0)
                        {
                            objects.get(i).setXvel(objects.get(index).getXvel() * .5);
                        }
                        //break;
                    } else if (x2 >= objects.get(i).getX() && x2 <= (objects.get(i).getXlen() + objects.get(i).getX()))
                    {
                        collision = true;
                        //System.out.println(objects.get(i).getXvel());
                        if (objects.get(i).getXvel() == 0)
                        {
                            objects.get(i).setXvel(objects.get(index).getXvel() * .5);
                        }
                        //break;
                    } else if (x <= objects.get(i).getX() && x2 >= objects.get(i).getX())
                    {
                        collision = true;
                        //System.out.println(objects.get(i).getXvel());
                        if (objects.get(i).getXvel() == 0)
                        {
                            objects.get(i).setXvel(objects.get(index).getXvel() * .5);
                        }

                        //break;
                    }
                } else
                {
                    collision = false;
                }
            }
        }
        if (collision && collisions)
        {
            objects.get(index).setXvel(objects.get(index).getXvel() * -.3);
        }
        if (!collisions)
        {
            collision = false;
        }
        return collision;
    }

    public int mouseInObject()
    {
        for (int i = 0; i < objects.size(); i++)
        {
            if (mousex >= objects.get(i).getX() && mousex <= (objects.get(i).getX() + objects.get(i).getXlen()))
            {
                if (mousey >= objects.get(i).getY() && mousey <= (objects.get(i).getY() + objects.get(i).getYlen()))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) //Used
    {
        if (e.getY() <= 10)
        {
            if (e.getX() <= 40)
            {
                gravity = !gravity;
            }
            if (e.getX() >= 45 && e.getX() <= 100)
            {
                collisions = !collisions;
            }
            if (e.getX() >= 110 && e.getX() <= 150)
            {
                physics = !physics;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e)  //Used
    {
        if (e.getButton() == MouseEvent.BUTTON3)
        {
            if (mouseInObject() != -1)
            {
            } else
            {
                newobject = true;
                startx = e.getX();
                starty = e.getY();
            }
        }
        System.out.println("pressed");
        for (int i = 0; i < objects.size(); i++)
        {
            if (e.getX() >= objects.get(i).getX() && e.getX() <= (objects.get(i).getX() + objects.get(i).getXlen()))
            {
                if (e.getY() >= objects.get(i).getY() && e.getY() <= (objects.get(i).getY() + objects.get(i).getYlen()))
                {
                    selected = i;
                    diffX = e.getX() - objects.get(selected).getX();
                    diffY = e.getY() - objects.get(selected).getY();

                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) //Used
    {
        if (newobject)
        {
            Color f = new Color((int) (256 * Math.random()), (int) (256 * Math.random()), (int) (256 * Math.random()));
            object obj = new object(a, b, c - a, d - b, objCount, f);
            objects.add(obj);
            objCount++;
        }
        System.out.println("released");
        selected = -1;
        newobject = false;
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
    public void mouseDragged(MouseEvent e) //Used
    {

        if (newobject)
        {
            mousex = e.getX();
            mousey = e.getY();
        } else if (selected > -1)
        {
            objects.get(selected).setXvel(1.5 * (e.getX() - oldx));
            objects.get(selected).setYvel(e.getY() - oldy);
            objects.get(selected).setX(e.getX() - diffX);
            objects.get(selected).setY(e.getY() - diffY);
            oldx = e.getX();
            oldy = e.getY();
            //System.out.println(objects.get(selected).getXvel());
            //System.out.println(objects.get(selected).getYvel());

        }
    }

    @Override
    public void mouseMoved(MouseEvent e)   //Used
    {
        if (!newobject)
        {
            mousex = e.getX();
            mousey = e.getY();
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e) //Used
    {

        System.out.println(e.getKeyCode());
        if (e.getKeyCode() == 8 || e.getKeyCode() == 127)
        {
            int result = mouseInObject();
            if (result != -1)
            {
                objects.remove(result);
            }
        } else if (e.getKeyCode() == 17)
        {
            int result = mouseInObject();
            if (result != -1)
            {
                rotateObject(result);
            }
        } else if (e.getKeyCode() == 67)
        {
            int result = mouseInObject();
            if (result != -1)
            {
                Color c = new Color((int) (256 * Math.random()), (int) (256 * Math.random()), (int) (256 * Math.random()));
                object obj = new object(objects.get(result).getX() + 10, objects.get(result).getY() - 10, objects.get(result).getXlen(), objects.get(result).getYlen(), objCount, c);
                objects.add(obj);
                objCount++;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
}

class object
{

    int xpos, ypos, xlen, ylen, yvel, num;
    double xvel;
    Color color;

    public object(int x, int y, int xl, int yl, int n, Color c)
    {
        xpos = x;
        ypos = y;
        xlen = xl;
        ylen = yl;
        xvel = 0;
        yvel = 0;
        num = n;
        color = c;
    }

    public void drawObject(Graphics g)
    {
        g.setColor(color);
        g.fillRect(xpos - 1, ypos - 1, xlen + 1, ylen + 1);
        g.setColor(color.BLACK);
        g.drawString(Integer.toString(num), xpos, ypos + 10);
    }

    public int getX()
    {
        return xpos;
    }

    public int getY()
    {
        return ypos;
    }

    public int getXlen()
    {
        return xlen;
    }

    public int getYlen()
    {
        return ylen;
    }

    public double getXvel()
    {
        if (Math.abs(xvel) < 1)
        {
            xvel = 0;
        }
        return xvel;
    }

    public int getYvel()
    {
        return yvel;
    }

    public void setX(int x)
    {
        xpos = x;
    }

    public void setY(int y)
    {
        ypos = y;
    }

    public void setXlen(int x)
    {
        xlen = x;
    }

    public void setYlen(int y)
    {
        ylen = y;
    }

    public void setXvel(double xv)
    {
        xvel = xv;
    }

    public void setYvel(int yv)
    {
        yvel = yv;
    }

    public void moveObject(int move)
    {
        ypos += move;
    }
}