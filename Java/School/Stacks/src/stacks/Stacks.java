/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stacks;

public class Stacks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IStack<Integer> s = new Stack(1337,new EmptyStack());
        IStack<Integer> rev = new EmptyStack();
        int[] l = new int[100];
        for(int i=0;i<100;i++)
        {
            s = s.push((int)(Math.random()*1000));
            l[i] = (int)(Math.random()*1000);
        }
        System.out.println(s.toString());
        for(int i:l)
        {
            System.out.print(i+",");
            rev = rev.push(i);
        }
        System.out.println();
        System.out.println(rev.toString());
        System.out.println(s.length());
        //System.out.println(s.toArray().length);
        for(int i:s.toArrayList())
        {
            System.out.print(i+",");
        }
        System.out.println();
    }
    
}
