/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package priorityqueue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author ostlinja
 */
public class PQueue {
    private ArrayList<Entry> queue;
    private ArrayList processes;
    int MAXPRIORITY = 9999;
    int MINPRIORITY = 0;
    Random IDGenerator;
    public PQueue(){
        queue = new ArrayList();
        processes = new ArrayList();
        IDGenerator = new Random();
    }
    
    public PQueue(int seed){
        this();
        IDGenerator = new Random(seed);
    }
    
    public boolean isEmpty(){return queue.isEmpty();}
    public int length(){return queue.size();}
    
    public ArrayList<Entry> getQueue(){
        return (ArrayList<Entry>)queue.clone();
    }
    
    public ArrayList getProcesses(){
        return (ArrayList)processes.clone();
    }
    
    public void push(){
        add(MAXPRIORITY);
    }
    
    public void add(int p){
        if(p>=MINPRIORITY && p<=MAXPRIORITY)
        {
            String id = generateID();
            processes.add(id);
            queue.add(new Entry(id,p));
            queue.sort((x,y)->((Entry)y).getPriority()-((Entry)x).getPriority());
        }
    }
    
    public int maxPriority(){
        int max = 0;
        for(Entry e: queue)
            max = Math.max(max, e.getPriority());
        return max;
    }
    
    public String pop(){
        String res = queue.get(0).getID();
        delete(0);
        return res;
    }
    
    public Entry peek(){
        return get(0);
    }
    
    public Entry get(int index){
        return queue.get(index);
    }
    
    public void changePriority(int index, int p){
        if(p>=MINPRIORITY && p<=MAXPRIORITY)
            queue.get(index).setPriority(p);
    }
    
    public void incrementPriority(int index){
        changePriority(index,queue.get(index).getPriority()+1);
    }
    
    public void decrementPriority(int index){
        changePriority(index,queue.get(index).getPriority()-1);
    }
    
    public void delete(int index){
        processes.remove(queue.get(index).getID());
        queue.remove(index);
    }
            
    public boolean containsID(String id){
        for(Entry e:queue)
            if(e.getID().equals(id))
                return true;
        return false;
    }
    
    private String generateID(){
        IDChars c = new IDChars();
        while(true)
        {
            String id = "";
            for(int i=0;i<3;i++)
                id += c.getChar();
            for(int i=0;i<6;i++)
                id += c.getNumber();
            if(!containsID(id))
                return id;
        }
    }
    
    public String toString()
    {
        String res = "{";
        for(Entry e:queue)
        {
            res += e.toString();
            if(queue.indexOf(e)<queue.size()-1)
                res+=",";
        }
        return res+"}";
    }
}



class IDChars{
    private String Schars;
    private String Ichars;
    private int NCR;
    public IDChars(){
        this(4);
    }
    
    public IDChars(int r){
        Schars = "";
        Ichars = "";
        generateChars();
        NCR = r;
    }
    
    private void generateChars(){
        for(char c='a';c<'z';c++)
            Schars+=c;
        for(char c='A';c<'Z';c++)
            Schars+=c;
        for(int c=0;c<10;c++)
            Ichars+=c;
    }
    
    public String getRandChar(){
        if((int)(Math.random()*NCR)==1){
            return getChar();
        }
        else{
            return getNumber();
        }
    }
    public String getNumber(){
        int index = (int)(Math.random()*Ichars.length());
        return Ichars.substring(index, index+1);
    }
    
    public String getChar(){
        int index = (int)(Math.random()*Schars.length());
        return Schars.substring(index, index+1);
    }

}

