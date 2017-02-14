/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule;

import java.awt.Color;
import java.util.Calendar;
import javax.swing.ImageIcon;

/**
 *
 * @author erik
 */
public class Assignment
{
    boolean isFlagged;
    String name,type,disc;
    Calendar date;
    ImageIcon flagged,close;
    Color color;
    public Assignment(String n, String t, String a, Calendar d, boolean f, Color c)
    {
        name = n;
        type = t;
        disc = a;
        date = d;
        isFlagged = f;
        color = c;
        flagged = new ImageIcon("C:/Users/erik/Pictures/blankFlagged.png");
        if(isFlagged)
        {
             flagged = new ImageIcon("C:/Users/erik/Pictures/flagged.png");
        }
        close = new ImageIcon("C:/Users/erik/Pictures/close.png");
        
    }
    public void toggleFlagged()
    {
        isFlagged = !isFlagged;
    }
    public String getName()
    {
        return name;
    }
    public String getDescription()
    {
        return disc;
    }
    public String getAssignmentType()
    {
        return type;
    }
    public Calendar getDate()
    {
        return date;
    }
    public boolean getFlagged()
    {
        return isFlagged;
    }
    public ImageIcon getFlaggedIcon()
    {
        return flagged;
    }
    public ImageIcon getCloseIcon()
    {
        return close;
    }
    public Color getColor()
    {
        return color;
    }
}
