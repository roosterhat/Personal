/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class DaysOfMonth {
    public static void main(String[] args) {
        new DaysOfMonth();
    }
    
    Scanner scanner = new Scanner(System.in);
    public DaysOfMonth(){
        Map<String, Integer> months = new HashMap();//Map of all the months and their length in days 
        months.put("January",31);
        months.put("February",28);
        months.put("March",31);
        months.put("April",30);
        months.put("May",31);
        months.put("June",30);
        months.put("July",31);
        months.put("August",31);
        months.put("September",30);
        months.put("October",31);
        months.put("November",30);
        months.put("December",31);
        
        System.out.print("Enter year: ");
        int year = scanner.nextInt();
        String month;
        do{
            System.out.println("Enter month(at least 3 letters)");  //Makes sure that the entered month is at least 3 characters long
            month = scanner.next();
        }while(month.length()<3);
        
        for(String m: months.keySet()){
            if(Pattern.matches(month.toUpperCase()+".*", m.toUpperCase())){//attempts to match the input month to a month from the Map of months
                month = m;
                break;
            }                
        }
        
        if(months.containsKey(month))
            System.out.println(month+" "+year+" has "+months.get(month)+" days");
        else
            System.out.println("'"+month+"' is not a known month");
    }
}
