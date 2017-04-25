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
public class PriorityQueue {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PQueue pq = new PQueue();
        for(int i=0;i<10;i++)
            pq.add((int)(Math.random()*10));
        System.out.println(pq);
        System.out.println(pq.getProcesses().size()+"/"+pq.getQueue().size());
        System.out.println(pq.pop());
        System.out.println(pq);
        System.out.println(pq.getProcesses().size()+"/"+pq.getQueue().size());
    }
    
}
