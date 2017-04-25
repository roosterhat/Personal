/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package priorityqueue;

/**
 *
 * @author ostlinja
 */
public class Entry {
    private String processID;
    private int priority;
    public Entry(String id, int p){
        processID = id;
        priority = p;
    }
    public String getID(){return processID;}
    public int getPriority(){return priority;}
    public void setPriority(int p){
        priority = p;
    }
    
    public String toString(){
        return "["+processID+","+priority+"]";
    }
}
