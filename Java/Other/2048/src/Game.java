
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;


public class Game extends Applet implements KeyListener
{
    int score = 0;
    Cell[][] cells = new Cell[4][4];
    private Image dbImage;
    private Graphics dbg; 
    Image backGround;
    public void init()
    {
        this.setSize(820,700);
        backGround = getImage(getCodeBase(), "2048board.jpg");
        ImagePanel bgp = new ImagePanel(0,0,(int)getBounds().getWidth(), (int)getBounds().getHeight());
        bgp.setLayout(new FlowLayout());
        bgp.setBackGroundImage(backGround);
        setLayout(new BorderLayout());
        add(bgp);
        for(int r=0;r<4;r++)
        {
            for(int c=0;c<4;c++)
            {
                cells[r][c] = new Cell(r,c);
            }
        }
        placeNewTiles();
        for(int r=0;r<4;r++)
        {
            for(int c=0;c<4;c++)
            {
                System.out.print(cells[r][c].getValue()+" ");
            }
            System.out.println();
        }
    }
    public void paint(Graphics g)
    {    
        
        drawTiles(g);
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
    public void drawTiles(Graphics g)
    {
       for(int r=0;r<4;r++)
        {
            for(int c=0;c<4;c++)
            {
//                ImagePanel bgp = new ImagePanel(20+(c-1)*185,20+(c-1)*185,185,185);
//                bgp.setLayout(new FlowLayout());
//                bgp.setBackGroundImage(cells[r][c].getImage());
//                add(bgp);
                g.drawImage(cells[r][c].getImage(), 20+(c-1)*185,20+(r-1)*185,185,185, this);
                if(cells[r][c].getImage()!=null)
                {
                    //System.out.println("test");
                    g.setColor(Color.RED);
                    g.fillOval(110+(c-1)*185,110+(r-1)*185,10,10);
                }
            }
        } 
    }
    public void placeNewTiles()
    {
        for(int x=0;x<Math.random()*1+1;x++)
        {
            while (true)
            {
                int r =(int)(Math.random()*4);
                int c =(int)(Math.random()*4);
                if(cells[r][c].getValue()==0)
                {
                    if(new Random().nextInt(2)==1)
                    {
                        cells[r][c].setValue(2);
                        cells[r][c].setImage(getImage(getCodeBase(), "tile2.jpg"));
                    }
                    else
                    {
                        cells[r][c].setValue(4);
                        cells[r][c].setImage(getImage(getCodeBase(), "tile4.jpg"));
                    }
                    break;
                }
            }
        }
    }
    public void keyPressed(KeyEvent e) 
    {
    }
    public void keyReleased(KeyEvent e) 
    {
    }
    public void keyTyped(KeyEvent e) 
    {
    }
}

class Cell
{
    int column,row,value;
    Image cellImage;
    public Cell(int a, int b)
    {
        column=a;
        row=b;
        value=0;
        cellImage = null;
    }
    public void setImage(Image i)
    {
        cellImage=i;
    }
    public void setValue(int i)
    {
        value=i;
    }
    public Image getImage()
    {
        return cellImage;
    }
    public int getValue()
    {
        return value;
    }
}

class ImagePanel extends Panel 
{
     Image panelImage;
     int x,y,width,height;

     ImagePanel(int a, int b, int c, int d) 
     {
          super();
          x = a;
          y = b;
          width = c;
          height = d;
     }

     public void paint(Graphics g) 
     {
          g.drawImage(getBackGroundImage(), x, y, width, height, this);
     }

     public void setBackGroundImage(Image backGround) 
     {
          this.panelImage = backGround;    
     }

     private Image getBackGroundImage() 
     {
          return panelImage;    
     }
}

