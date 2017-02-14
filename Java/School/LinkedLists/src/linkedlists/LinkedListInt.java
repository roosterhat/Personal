/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedlists;

import java.io.File;

/**
 *
 * @author ostlinja
 */
public interface LinkedListInt {
    public boolean isEmpty();
    public int getSize();
    public Node getFirst();
    public Node getNode(int i);
    public int find(Node n);
    public int findVague(String s);
    public void insert(Node n);
    public void insert(Node n, int i);
    public void push(Node n);
    public void append(Node n);
    public boolean delete(Node n);
    public boolean delete(int i);
    public void importData(String p);
    public void importData(File f);
    public void print();
    public String toString();
}
