/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piggybank;
import java.util.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Piggybank extends Applet implements MouseListener
{
    bank bobby;
    bank suzzy;
    button button1;
    button button2;
    button button3;
    button button4;
    button button5;
    button button6;
    button button7;
    button button8;
    button button9;
    button button10;
    button button11;
    boolean edit = false;
    Scanner scan = new Scanner(System.in);
    private int x,y;
    int choice = -1;
    ArrayList<bank> accounts = new ArrayList();
    public void init()
    {
        //String name=JOptionPane.showInputDialog(this, "Enter your name");
        this.setBackground(Color.GRAY);
        this.setSize(1000,650);
        this.addMouseListener(this);
        button1 = new button(450,100,550,150,"Withdraw");
        button2 = new button(450,175,550,225,"Deposite");
        button3 = new button(450,250,550,300,"Edit Account");
        button4 = new button(450,325,550,375,"Log Out");
        button5 = new button(450,175,550,225,"Log In");
        button6 = new button(450,250,550,300,"New Account");
        button7 = new button(400,100,600,150,"Wonder Space Bank");
        button8 = new button(450,175,550,225,"Username");
        button9 = new button(450,250,550,300,"Password");
        button10 = new button(450,325,550,375,"Return");
        button11 = new button(450,325,550,375,"List Accounts");
    }
    
    
    public void paint(Graphics g)
    {
        if(choice!=-1 && !edit)
        {
            button1.drawButton(g);
            button2.drawButton(g);
            button3.drawButton(g);
            button4.drawButton(g);
            
        }
        else if(edit)
        {
            button8.drawButton(g);
            button9.drawButton(g);
            button10.drawButton(g);
        }
        else
        {
            button5.drawButton(g);
            button6.drawButton(g);
            button7.drawButton(g);
            button11.drawButton(g);
        }
        
        if(accounts.size()!=0&&choice!=-1 && !edit)
        {
            g.drawString("Account: "+accounts.get(choice).getName(),500-(((accounts.get(choice).getName().length()+9)*6)/2),20);
            String money = ""+accounts.get(choice).viewBalance();
            g.drawString("Balance: $"+accounts.get(choice).viewBalance(),500-((money.length()+10)*6)/2,40);
            if(button1.checkClick(x,y,"withdraw"))
            {
                button1.flash(g);
                accounts.get(choice).withdraw();
                repaint();
            }
            if(button2.checkClick(x,y,"deposit"))
            {
                button2.flash(g);
                accounts.get(choice).deposite();
                repaint();
            }
            if(button3.checkClick(x, y, "editAccount"))
            {
                button3.flash(g);
                edit = true;
                repaint();
            }
            if(button4.checkClick(x,y,"logout"))
            {
                button4.flash(g);
                choice = -1;
                repaint();
            }
            
        }
        else if(edit)
        {
            g.drawString("Account: "+accounts.get(choice).getName(),500-(((accounts.get(choice).getName().length()+9)*6)/2),20);
            String money = ""+accounts.get(choice).viewBalance();
            g.drawString("Balance: $"+accounts.get(choice).viewBalance(),500-((money.length()+10)*6)/2,40);
            if(button8.checkClick(x, y, "username"))
            {
                button8.flash(g);
                accounts.get(choice).changeName();
                repaint();
            }
            if(button9.checkClick(x, y, "password"))
            {
                button9.flash(g);
                accounts.get(choice).changePassword();
                repaint();
            }
            if(button10.checkClick(x, y, "return"))
            {
                button10.flash(g);
                edit = false;
                repaint();
            }
        }
        else
        {
            if(button5.checkClick(x,y,"login"))
            {
                button5.flash(g);
                changeAccount();
                repaint();
            }
            if(button6.checkClick(x,y,"addAccount"))
            {
                button6.flash(g);
                addAccount();
                repaint();
            }
            if(button11.checkClick(x, y, "listaccounts"))
            {
                button11.flash(g);
                listAccounts();
                repaint();
            }
        }
        x=1;
        y=1;
    }
    
    public void listAccounts()
    {
        String names = "";
        //System.out.println("Accounts");
        for(int i=0;i<accounts.size();i++)
        {
            names+=i+" "+accounts.get(i).getName()+"\n";
            //System.out.println(i+" "+accounts.get(i).getName());
        }
        JOptionPane.showMessageDialog(null, names, "Accounts", 1);
    }
    
    @Override
    public void mouseClicked(MouseEvent me)
    {
        x = me.getX();
        y = me.getY();
        repaint();
    }
    public void changeAccount()
    {
        boolean found = false;
        String name = JOptionPane.showInputDialog(this, "Enter your username");
        String pass = JOptionPane.showInputDialog(this, "Enter your password");
        for(int i=0;i<accounts.size();i++)
        {
            if(accounts.get(i).getName().equals(name))
            {
                found = true;
                if(accounts.get(i).getPass().equals(pass))
                {
                    choice = i;
                    //System.out.println("Logging into account "+name);
                    JOptionPane.showMessageDialog(null,"Logging into account "+name);
                    
                }
                else
                {
                    //System.out.println("Password is incorrect");
                    JOptionPane.showMessageDialog(null,"Password is incorrect");
                }
            }
        }
        if(!found)
        {
            //System.out.println("Account doesnt exist");
            JOptionPane.showMessageDialog(null,"Account doesnt exist");
        }
       
            
        x=1;
        y=1;
        repaint();
    }
    public void addAccount()
    {
        boolean found = false;
        String name = JOptionPane.showInputDialog(this, "Enter new username");
        for(int i=0;i<accounts.size();i++)
        {
            if(accounts.get(i).getName().equals(name))
            {
                found = true;
            }
        }
        if(!found)
        {
            String pass = JOptionPane.showInputDialog(this, "Enter your new password");
            bank bankaccount = new bank(0,name,pass);
            accounts.add(bankaccount);
            //System.out.println("Account added");
            JOptionPane.showMessageDialog(null,"Account added");
        }
        else
        {
            //System.out.println("Account already exists");
            JOptionPane.showMessageDialog(null,"Account already exists");
        }
        
    }

    @Override
    public void mousePressed(MouseEvent me) 
    {
    }

    @Override
    public void mouseReleased(MouseEvent me)
    {
    }

    @Override
    public void mouseEntered(MouseEvent me) 
    {
    }

    @Override
    public void mouseExited(MouseEvent me) 
    {
    }
}
class bank
{
    Scanner scan = new Scanner(System.in);
    double balance;
    String accountname,accountpassword;
    public bank(double money,String name,String password)
    {
        balance = money;
        accountname = name;
        accountpassword = password;
    }
    public void withdraw()
    {
        String temp = JOptionPane.showInputDialog(null, "Enter Amount You Wish to Withdraw");
        double amount = Double.parseDouble(temp);
        if(amount<=balance)
        {
            balance -= amount; 
            //System.out.println("$"+amount+" succcesfully withdrawn");
            JOptionPane.showMessageDialog(null,"$"+amount+" succcesfully withdrawn");
        }
        else
        {
            //System.out.println("You dont have enough money");
            JOptionPane.showMessageDialog(null,"! You dont have enough money !");
        }
    }
    public void deposite()
    {
        String temp = JOptionPane.showInputDialog(null, "Enter Amount You Wish to Deposite");
        double amount = Double.parseDouble(temp);
        balance += amount;
        //System.out.println("$"+amount+" deposited");
        JOptionPane.showMessageDialog(null,"$"+amount+" deposited");
    }
    public void changeName()
    {
        while (true)
        {
            accountname = JOptionPane.showInputDialog(null, "Enter new name");
            if(accountname!="" && accountname!=" " && accountname!=null)
            {
                String pass = JOptionPane.showInputDialog(null, "Enter Password");
                if(pass.equals(accountpassword))
                {
                    //System.out.println("Name Changed to "+accountname);
                    JOptionPane.showMessageDialog(null,"Name Changed to "+accountname);
                    break;
                }
                else
                {
                    //System.out.println("Password is incorrect");
                    JOptionPane.showMessageDialog(null,"Password is incorrect");
                }
            }
            else
            {
                //System.out.println("Name cannot be Null");
                JOptionPane.showMessageDialog(null,"Name cannot be Null");
            }
        }
    }
    public void changePassword()
    {
        while (true)
        {
            accountpassword = JOptionPane.showInputDialog(null, "Enter new password");
            if(accountpassword!="" && accountpassword!=" " && accountpassword!=null)
            {
                //System.out.println("Password Changed to "+accountpassword);
                JOptionPane.showMessageDialog(null,"Password Changed to "+accountpassword);
                break;
            }
            else
            {
                //System.out.println("Password cannot be Null");
                JOptionPane.showMessageDialog(null,"Password cannot be Null");
            }
        }    
        
    }
    public String getName()
    {
        return accountname;
    }
    public String getPass()
    {
        return accountpassword;
    }
    public double viewBalance()
    {
        return balance;
    }
}
class button
{
    int x,y,x2,y2;
    String name;
    public button(int xpos,int ypos,int x2pos,int y2pos,String newname)
    {
        x = xpos;
        y = ypos;
        x2 = x2pos;
        y2 = y2pos;
        name = newname;
    }
    public void drawButton(Graphics g)
    {
        g.setColor(Color.BLUE);
        g.fillRect(x,y,x2-x,y2-y);
        g.setColor(Color.BLACK);
        g.drawString(name,x+(((x2-x)-(name.length()*6))/2),y+(y2-y)/2);
    }
    public boolean checkClick(int xclick,int yclick,String choice)
    {
        if(xclick>=x && xclick<=x2)
        {
            if(yclick>=y && yclick<=y2)
            {
                return true;
            }
        }
        return false;
    }
    public void flash(Graphics g)
    {
        g.setColor(Color.RED);
        g.fillRect(x,y,x2-x,y2-y);
        g.setColor(Color.BLACK);
        g.drawString(name,x+((100-(name.length()*6))/2),y+(y2-y)/2);
        try 
        {
            Thread.sleep(100);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        g.setColor(Color.BLUE);
        g.fillRect(x,y,x2-x,y2-y);
        g.setColor(Color.BLACK);
        g.drawString(name,x+((100-(name.length()*6))/2),y+(y2-y)/2);
    }
}
