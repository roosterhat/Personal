/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Race {
    static String defaultTrack = "----------U-C-S-------C---S---------C--U----";
    static String raceTrack;
    static int raceLength;
    static int raceNumber;
    static int seasonRaces;
    static ArrayList<Car> racers;
    
    public static void main(String[] args) {
        seasonRaces = 25;
        raceNumber = 1;
        racers = new ArrayList();
        racers.add(new Car("Bob Barker Car",0.30,0.65,0.1,0.95,0.13,'B'));
        racers.add(new Car("Alex Trebek Car",0.25,0.55,0.1,0.95,0.19,'A'));
        racers.add(new Car("Pat Sajak Car",0.45,0.85,0.3,0.78,0.08,'P'));
        racers.add(new Car("Drew Carrie Car",0.15,0.25,0.05,0.9999,0.21,'D'));
        racers.add(new Car("Ricky Bobby Car",0.2,0.8,0.075,0.97,0.2,'R'));
        for(int i=0;i<seasonRaces;i++){
            raceTrack = generateTrack();
            raceLength = raceTrack.length();
            System.out.println(raceNumber);
            System.out.println("["+raceTrack+"]\n");
            race(false);
            raceNumber++;
        }
        displayStandings();
    }
    
    private static String generateTrack(){
        String[] segments = {"S","C","U","-"};      //track segements
        int[] weights = {6,12,18,100};              //track weights
        String track = "-----";                     //track starts with straight away 
        int length = (int)(Math.random()*30)+30;    //random length between 30-60
        for(int i=0;i<length;i++){
            int rand = (int)(Math.random()*100);    //random weight value 1-100
            for(int x=0;x<4;x++)
                if(rand<weights[x]){
                    track+=segments[x];
                    break;
                }
        }
        return track;
    }
    
    public static void race(boolean display){
        int time = 0;
        int longestName = getLongestName();
        racers.forEach(x->x.reset());   //resets all racers to prep for beginning of race
        while(!finished()){
            time++;
            for(Car c: racers){
                if(!c.finished){
                    c.driveSection(raceTrack.charAt((int)c.raceProgress)); //tell car to drive the section of track it is currently at
                    if(c.raceProgress >= raceLength-1){ //if car has finished
                        c.finished = true;
                        c.finishTime = time;
                        c.position = getRank(c);
                    }
                }
            }
            if(display)
                displayRace(time,longestName);
        }
        for(Car c: racers)  //display winners
            if(c.position==1){
                c.wins++;
                System.out.println("Winner: "+c.name+"\n");
            }
        
    }
    
    private static void displayRace(int time, int longest){     
        System.out.println("Race: "+raceNumber+" Clock: "+time);
        System.out.println(raceTrack+"#");
        for(Car c: racers){
            System.out.print(String.format("%"+(((int)c.raceProgress)+1)+"s%"+
                    (c.finished ? "s" : (raceLength-((int)c.raceProgress))+"s"), c.symbol, "#"));   //displays current racer's position
            if(c.finished)
                System.out.print(String.format("%-"+longest+"s #%2d Time: %3d",c.name, c.position, c.finishTime));  //display name, rank, and time
            else
                System.out.print(c.name);   
            System.out.println();
        }
        System.out.println();
    }
    
    
    private static int getLongestName(){//used for formatting
        int longest = 0;
        for(Car c: racers)
            if(longest<c.name.length())
                longest = c.name.length();
        return longest;
    }
    
    private static int getRank(Car c){
        int pos=1;
        for(Car x : racers)
            if(x!=c)
                if(x.finished && x.finishTime<c.finishTime)
                    pos++;
        return pos;
    }
    
    private static boolean finished(){
        for(Car c: racers)
            if(!c.finished)
                return false;
        return true;
    }
    
    public static void displayStandings(){
        int longestName = getLongestName();
        ArrayList<Car> temp = (ArrayList<Car>)racers.clone();
        temp.sort((x,y)->{return y.wins-x.wins;});
        System.out.println("Standings");
        System.out.println(String.format("%-2s %-"+longestName+"s %-4s %-7s", "#", "Name","Wins","Ratio")); //display heading
        for(Car c: temp){   
            System.out.println(String.format("%-2d %-"+longestName+"s %-4d %.3f%%", 
                    temp.indexOf(c)+1, c.name, c.wins, (double)c.wins/raceNumber*100));//display data for each racer
        }
    }
}


class Car{
    String name;
    private Map<Character, Double> handling = new HashMap();
    double raceProgress;
    double speed;
    double topSpeed;
    double acceleration;
    int wins;
    int finishTime;
    int position;
    boolean finished;
    char symbol;
    
    public Car(String name, double s, double c, double u, double topspeed, double acceleration, char symbol){
        this.name = name;
        handling.put('-', 1.0); //map each handling value to corresponding track section
        handling.put('S', s);   //
        handling.put('C', c);   //
        handling.put('U', u);   //
        this.topSpeed = topspeed;
        this.acceleration = acceleration;
        this.symbol = symbol;
        wins = 0;
        reset();
    }
    
    public void driveSection(char trackSection){    //changes speed based on given track segment
        if(speed < topSpeed * handling.get(trackSection))
            speed += acceleration;
        else if(speed > topSpeed * handling.get(trackSection))
            speed = topSpeed*handling.get(trackSection);
        raceProgress += speed;
    }
    
    public void reset(){    //resets car values to default values
        raceProgress = 0;
        speed = 0;
        finished = false;
        finishTime = 0;
        position = 0;
    }
}