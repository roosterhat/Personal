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
public class Zeller {
    public static void main(String[] args) {
        new Zeller();
    }
    
    Scanner scanner = new Scanner(System.in);
    public Zeller(){
        System.out.print("Enter Year(YYYY): ");
        int year = scanner.nextInt();
        System.out.print("Enter Month(MM): ");
        int month = scanner.nextInt();
        if(month<3){    //if the month is january or februrary then change the month to 13 or 14 respectivly
            month += 12;
            year--;
        }
        System.out.print("Enter Day(DD): ");
        int day = scanner.nextInt();
        int k = year%100;
        double j = year/100;
        int dayOfWeek = (int)((day + (26*(month+1))/10 + k + k/4 + j/4 + 5*j)%7);
        String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        System.out.println("Day of the week is "+days[dayOfWeek]);
    }
}
