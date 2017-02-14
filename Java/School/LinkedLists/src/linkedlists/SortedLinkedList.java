/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linkedlists;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public class SortedLinkedList extends LinkedList{
    public SortedLinkedList()
    {
        super();
    }
    public SortedLinkedList(Node<Integer> n)
    {
        super(n);
    }
    
    public void insert(Node n)//inserts the given node in a place in an ordered list
    {
        if(isEmpty())
            first = n;
        else if(!(n.getValue() instanceof Integer))
            append(n);
        else if((first.getValue() instanceof Integer) && 
                (int)first.getValue()>(int)n.getValue())
        {
            n.setNext(first);
            first = n;
        }
        else
        {
            Node curr = first;
            while(curr.hasNext()) 
            {
                if(curr.getNext().getValue() instanceof Integer)
                {
                    if((int)curr.getNext().getValue()>(int)n.getValue())
                    {
                        n.setNext(curr.getNext());
                        curr.setNext(n);
                        break;
                    }
                }
                else
                {
                    n.setNext(curr.getNext());
                    curr.setNext(n);
                    break;
                }
                curr = curr.getNext();
            }
            if(!curr.hasNext())
               curr.setNext(n);
        }
    }
    
    public void nodeInsert(Node n)//calls insert for the first node if it exists which recursivly 
                                 //calls insert until the proper place for the new node has been found
    {
        if(isEmpty())
            first = n;
        else
            first.insert(n);
    }
    
    public boolean delete(Node n)//similar to 'delete' (in super class) but quits if it passes the value of the given node
    {
        if(isEmpty())
            return false;
        if(first==n)
        {
            if(first.hasNext())
                first = first.getNext();
            else
                first = null;
            return true;               
        }
        Node curr = first;
        while(curr.hasNext() && (int)curr.getValue()<=(int)n.getValue())
        {
            if(curr.getNext()==n)
            {
                if(curr.getNext().hasNext())
                    curr.setNext(curr.getNext().getNext());
                else
                    curr.setNext(null);
                return true;
            }
            curr = curr.getNext();
        }
        return false;
    }
        
    public void importData(File f)//same as super class but instead of appending the new values to the list it inserts them
    { 
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader reader = Files.newBufferedReader(f.toPath(), charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher m = Pattern.compile("[^\\s,]+").matcher(line);   
                while(m.find())
                {
                    String s = line.substring(m.start(), m.end());
                    if(isNumeric(s))
                        insert(new Node(Integer.parseInt(s)));
                    else
                        insert(new Node(s));
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }
    
    public int find(Node n)//similar to 'find' (in super class) but quits if passes the value of the given node
    {
        int pos = 0;
        if(isEmpty())
            return -1;
        else
        {
            Node curr = first;
            if(curr==n)
                return pos;
            while(curr.hasNext() && (int)curr.getValue()<=(int)n.getValue())
            {
                pos++;
                if(curr.getNext()==n)
                    return pos;
                curr = curr.getNext();
            }
            return -1;
        }
    }
    
    public int findS(Node n)
    {
        int low = 0;
        int high = getSize()-1;
        int mid = high/2;
        while (mid>low || mid<high)
        {
            if(getNode(mid)==n)
            {
                return mid;
            }
            else if((int)getNode(mid).getValue()<(int)n.getValue())
            {
                low = mid;
                mid = (high-low)/2+low;
            }
            else if((int)getNode(mid).getValue()>(int)n.getValue())
            {
                high = mid;
                mid = (high-low)/2+low;
            }            
        }
        if(getNode(mid)==n)
                    return mid;
                else
                    return -1;
    }
}
