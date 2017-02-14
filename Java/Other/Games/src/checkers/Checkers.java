/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkers;

import games.GamesCommon;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Erik Ostlind
 */
public class Checkers implements GamesCommon
{
    String[] alphabet = {"a","b","c","d","e","f","g","h"};
    Scanner scanner = new Scanner(System.in);
    boolean running = true;
    String name = "Checkers";
    String[][] board = new String[8][8];
    boolean player = true;
    int[] lost = {0,0};
    public String getName()
    {
        return name;
    }
    
    public void run()
    {
        fillBoard();
        while(running)
        {
            String start,move;
            drawBoard();
            String piece;
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
            
            while (true)
            {
                String temp = scanner.nextLine();
                start = temp.substring(0, 2);
                move = temp.substring(2, 4);
                if(temp.equals("quit"))
                {
                    System.out.println("Game Quit");
                    running = false;
                    return;
                }
                if(Arrays.asList(alphabet).indexOf(""+start.charAt(0))>-1 && Integer.parseInt(""+start.charAt(1))>-1 && Integer.parseInt(""+start.charAt(1))<8)
                    if(board[Arrays.asList(alphabet).indexOf(""+start.charAt(0))][Integer.parseInt(""+start.charAt(1))].equals(piece))
                    {}    //break;
                    else
                        System.out.println("Invalid piece");
                else
                    System.out.println("Invalid start");
              
                if(board[Arrays.asList(alphabet).indexOf(""+move.charAt(0))][Integer.parseInt(""+move.charAt(1))].equals(" "))
                {
                    if(checkMove(start,move))
                    {
                        board[Arrays.asList(alphabet).indexOf(""+start.charAt(0))][Integer.parseInt(""+start.charAt(1))] = " ";
                        board[Arrays.asList(alphabet).indexOf(""+move.charAt(0))][Integer.parseInt(""+move.charAt(1))] = piece;            
                        player=!player;
                        break;
                    }
                    else if(checkJump(start,move))
                    {
                        board[Arrays.asList(alphabet).indexOf(""+start.charAt(0))][Integer.parseInt(""+start.charAt(1))] = " ";
                        board[Arrays.asList(alphabet).indexOf(""+move.charAt(0))][Integer.parseInt(""+move.charAt(1))] = piece;            
                        String jumped = getJumpedPiece(start,move);
                        board[Arrays.asList(alphabet).indexOf(""+jumped.charAt(0))][Integer.parseInt(""+jumped.charAt(1))] = " ";
                        System.out.println(jumped);
                        if(player)
                            lost[1]++;
                        else
                            lost[0]++;
                        //player=!player;
                        break;
                    }
                    else
                        System.out.println("Invalid move");  
                }
                else
                    System.out.println("Invalid space");
            }
        }
    }
    
    public boolean checkMove(String s,String e)
    {
        if(Math.abs(Arrays.asList(alphabet).indexOf(""+e.charAt(0))-Arrays.asList(alphabet).indexOf(""+s.charAt(0)))==1)
            if(Math.abs(Integer.parseInt(""+e.charAt(1))-Integer.parseInt(""+s.charAt(1)))==1)
                return true;
        return false;
    }
    
    public boolean checkJump(String s,String e)
    {
        if(Math.abs(Arrays.asList(alphabet).indexOf(""+e.charAt(0))-Arrays.asList(alphabet).indexOf(""+s.charAt(0)))==2)
            if(Math.abs(Integer.parseInt(""+e.charAt(1))-Integer.parseInt(""+s.charAt(1)))==2)
                return true;
        return false;
    }
    
    public String getJumpedPiece(String s,String e)
    {
        int x = Arrays.asList(alphabet).indexOf(""+e.charAt(0))-Arrays.asList(alphabet).indexOf(""+s.charAt(0));
        int y = Integer.parseInt(""+e.charAt(1))-Integer.parseInt(""+s.charAt(1));
        String temp = alphabet[Arrays.asList(alphabet).indexOf(""+s.charAt(0))+x/2]+(""+(Integer.parseInt(""+s.charAt(1))+y/2));
        return temp;
    }
    
    public void fillBoard()
    {
        for(int y=0;y<8;y++)
            for(int x=0;x<8;x++)
                board[x][y] = " ";
        int count = 0;
        for(int y=0;y<8;y++)
            for(int x=0;x<8;x++)
                if((x+y)%2==0 && count<12)
                {
                    board[x][y] = "@";
                    count++;
                }
        count = 0;
        for(int y=7;y>=0;y--)
            for(int x=7;x>=0;x--)
                if((x+y)%2==0 && count<12)
                {
                    board[x][y] = "o";
                    count++;
                }  
    }
    
    public void drawBoard()
    {
        System.out.println("  A B C D E F G H   P1:@ ("+(12-lost[0])+")    P2:o ("+(12-lost[1])+")");
        for(int y=0;y<8;y++)
        {
            System.out.print(y+"|");
            for(int x=0;x<8;x++)
            {
                if(board[x][y].equals(" "))
                    if((x+y)%2==0)
                        System.out.print("#|");
                    else
                        System.out.print(" |");
                else
                    System.out.print(board[x][y]+"|");
            }
            System.out.println();
        }

    }
}
