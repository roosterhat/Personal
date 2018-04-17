/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Lottery {
    public static void main(String[] args) {
        new Lottery();
    }
    
    Scanner scanner = new Scanner(System.in);
    double winnings;
    public Lottery(){
        winnings = 0;
        while(true){
            String winningNumber = String.valueOf((int)(Math.random()*1000));           //gemerate a random number (1-999)
            winningNumber = String.format("%3s", winningNumber).replaceAll(" ", "0");   //pad the number with zeros (ex. 98 -> 098)
            System.out.println("Total Earnings: "+winnings);
            System.out.print("Enter number: ");
            String attemptNumber = String.valueOf(scanner.nextInt());
            if(attemptNumber.equals(winningNumber)){                                           
                System.out.println("You win!");
                winnings+=10000;
            }
            else{
                for(char n: attemptNumber.toCharArray()){                                       //checks each digit in the user number
                    for(char c: winningNumber.toCharArray()){                                   //against each digit of the winning number
                        if(n==c){                                                               //if they match then remove the digit from the winning number
                            winnings+=1000;
                            winningNumber = winningNumber.replaceFirst(String.valueOf(c), "");
                            break;
                        }
                    }
                }
            }
        }
    }
}
