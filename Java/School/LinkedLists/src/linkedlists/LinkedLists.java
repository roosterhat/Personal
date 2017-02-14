/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedlists;

/**
 *
 * @author ostlinja
 */
public class LinkedLists {
    
    static SortedLinkedList ll = new SortedLinkedList();
    //static LinkedList ll = new LinkedList();
    public static void main(String[] args) {
        for(int i=0;i<200;i++)
            ll.insert(new Node<Integer>((int)(Math.random()*2000)));
        System.out.println("List");
        ll.print();
        System.out.println("Size");
        System.out.println(ll.getSize());
        Node<Integer> testnode = new Node(1337);
        Node<Integer> testnode2 = new Node(1234);
        System.out.println("Add 1337");
        ll.insert(testnode);
        System.out.println("Add 1234");
        ll.insert(testnode2);
        ll.print();
        System.out.println("Delete 1337");
        ll.delete(testnode);
        ll.print();
        System.out.println("find(1234)");
        System.out.println(ll.find(testnode2));
        System.out.println("findVague(\"1234\")");
        System.out.println(ll.findVague("1234"));
        System.out.println("findVague(\"1234567\")");
        System.out.println(ll.findVague("1234567"));
        System.out.println("getNode(findVague(1234))");
        System.out.println(ll.getNode(ll.findVague("1234")).getValue());
        System.out.println("delete(findVague(1234))");
        ll.delete(ll.findVague("1234"));
        ll.print();
        System.out.println("getNode(findVague(1234))");
        System.out.println(ll.getNode(ll.findVague("1234")));
        Node six = new Node(600);
        System.out.println("insert(600)");
        ll.nodeInsert(six);
        ll.print();
        new LLUI().setVisible(true);
        System.out.println("findS(600)");
        System.out.println(ll.findS(six));
        System.out.println("find(600)");
        System.out.println(ll.find(six));
    }
    
}
