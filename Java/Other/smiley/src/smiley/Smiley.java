/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smiley;

/**
 *
 * @author Wonder-Space 3
 */
public class Smiley 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        bicycle bike = new bicycle(1,20,0);
    }
}
class bicycle
{
    int gear,tireSize,speed;
    public bicycle(int newgear,int newtiresize,int newspeed)
    {
        gear = newgear;
        tireSize = newtiresize;
        speed = newspeed;
    }
    public void increaseGear()
    {
        gear += 1;
    }
    public void decreaseGear()
    {
        gear -= 1;
    }
    public void increaseSpeed()
    {
        speed += 1;
    }
    public void decreaseSpeed()
    {
        speed -= 1;
    }
}
