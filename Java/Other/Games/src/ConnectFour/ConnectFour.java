/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ConnectFour;

import games.GamesCommon;
import java.util.Scanner;

/**
 *
 * @author Erik Ostlind
 */
public class ConnectFour implements GamesCommon
{
    Scanner scanner = new Scanner(System.in);
    String name = "ConnectFour";
    boolean player = true;
    boolean running = true;
    String[][] board = new String[7][6];
    public String getName()
    {
        return name;
    }
    public void run()
    {
        fillBoard();
        while (running)
        {
            String piece;
            drawBoard();
            
            while (true)
            {
                if(player)
                {
                    System.out.print("P1 move: ");
                    piece = "@";
                }
                else
                {
                    System.out.print("P2 move: ");
                    piece = "o";
                }
                int temp = scanner.nextInt();
                if(temp>-1 && temp<7)
                {
                    if(checkColumnVolume(temp)<6)
                    {
                        System.out.println(checkColumnVolume(temp));
                        board[temp][(6-checkColumnVolume(temp))-1] = piece;
                        player=!player;
                        break;
                    }
                    else
                        System.out.println("Column full");
                }
                else if(temp==1337)
                {
                    System.out.println("Game Quit");
                    running = false;
                    break;
                }
                else
                    System.out.println("Invalid column");
            }
        }
    }
    
    public int checkColumnVolume(int c)
    {
        int temp = 0;
        for(int i=0;i<6;i++)
        {
            if(!board[c][i].equals(" "))
                temp++;
        }
        return temp;
    }
    
    public boolean checkWin(String p)
    {
        //find first piece
        //find adjacent pieces
        //follow adjacent pieces    
        return false;
    }
    
    public void fillBoard()
    {
        for(int y=0;y<6;y++)
            for(int x=0;x<7;x++)
                board[x][y] = " ";
    }
    
    public void drawBoard()
    {
        System.out.println("  0  1  2  3  4  5  6   P1: @   P2: o");
        for(int y=0;y<6;y++)
        {
            System.out.print("|");
            for(int x=0;x<7;x++)
                System.out.print("("+board[x][y]+")");
            
            System.out.println("|");
        }
    }
}
