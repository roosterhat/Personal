/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Calendar {
    public static void main(String[] args) {
        new Calendar();
    }
    
    Scanner scanner = new Scanner(System.in);
    int startDay;
    int year;
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    ArrayList<Entry<String, Integer>> months = new ArrayList();
        
    
    public Calendar(){
        months.add(new Entry("January",31));    //filling arraylist with all months and their length in days
        months.add(new Entry("February",28));   //  
        months.add(new Entry("March",31));      //  |
        months.add(new Entry("April",30));      //  V
        months.add(new Entry("May",31));
        months.add(new Entry("June",30));
        months.add(new Entry("July",31));
        months.add(new Entry("August",31));
        months.add(new Entry("September",30));
        months.add(new Entry("October",31));
        months.add(new Entry("November",30));
        months.add(new Entry("December",31));
        System.out.print("Enter the current year: ");
        year = scanner.nextInt();
        System.out.print("Enter the first day of the year (0-6): ");
        startDay = scanner.nextInt();
        int day = startDay;
        for(int i = 0;i<12;i++)
            day = printMonth(i, day);
    }
    
    public int printMonth(int month, int start){
        System.out.println(center(months.get(month).key+" "+year,35));      //print month and year
        
        System.out.println(String.join("", Collections.nCopies(35, "_")));  //separation line
        
        for(String day: days)                                               //print days of the week
            System.out.print(center(day,5));
        System.out.println();
        
        if(start<7)                                                         //print blank spaces before start day of the month
            for(int i=0;i<start;i++)
                System.out.print(center("",5));
        
        for(int i=1;i<=months.get(month).entry;i++){                        //print date for each day of the month
            System.out.print(center(String.valueOf(i),5));
            if((i+(start))%7==0)
                System.out.println();
        }
        System.out.println("\n");
        return (months.get(month).entry+(start-1))%7+1;                     //return start date of consecutive month
    }
    
    public String center(String text, int len){//centers text for specified length
        int before = (len - text.length())/2;
        int rest = len - before;
        return String.format("%" + before + "s%-" + rest + "s", "", text); 
    }
}

class Entry<K,E>{
    K key;
    E entry;
    public Entry(K k, E e){
        key = k;
        entry = e;
    }
}