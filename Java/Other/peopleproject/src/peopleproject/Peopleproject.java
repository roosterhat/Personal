/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peopleproject;
import java.util.*;

public class Peopleproject 
{
   private static Scanner scan = new Scanner(System.in);
   private static ArrayList name = new ArrayList();
   private static ArrayList age = new ArrayList();
   private static ArrayList SSN = new ArrayList();
   private static ArrayList POR = new ArrayList();
   private static ArrayList YOB = new ArrayList();
    public static void main(String[] args) 
    {
        for(int x=1;x<=5;x++)
        {
           addperson();
        }
        showUserData();
    }
    public static void addperson()
    {
        System.out.println("Enter Your Name");
        scan.nextLine();
        String person = scan.nextLine();
        String temp = person.substring(0,1);
        person = person.substring(1, person.length());
        person = temp.toUpperCase()+person;
        name.add(person);
        System.out.println("Enter Your Age");
        age.add(scan.nextInt());
        System.out.println("Enter Your Social Security Number");
        SSN.add(scan.nextLong());
        System.out.println("Enter Your Place of Residence");
        scan.nextLine();
        String place = scan.nextLine();
        temp = place.substring(0,1);
        place = place.substring(1, place.length());
        place = temp.toUpperCase()+place;       
        POR.add(place);
        System.out.println("Enter Your Year of Birth");
        YOB.add(scan.nextInt());
        System.out.println();
    }
    public static void showUserData()
    {
        for(int x=0;x<name.size();x++)
        {
            System.out.println("Name: "+name.get(x));
            System.out.println("Age: "+age.get(x));
            System.out.println("SSN: "+SSN.get(x));
            System.out.println("Place of Residence: "+POR.get(x));
            System.out.println("Year of Birth: "+YOB.get(x));
            System.out.println();
        }
    }
}
