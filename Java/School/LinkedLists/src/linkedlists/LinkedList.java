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
public class LinkedList implements LinkedListInt {
    Node first;
    public LinkedList()
    {
        first = null;
    }
    public LinkedList(Node n)
    {
        first = n;
    }
    
    public boolean isEmpty(){return first==null;}
    
    public Node getFirst(){return first;}
    
    public int getSize()
    {
        if(isEmpty())
            return 0;
        else
            return first.getSize();
    }
    
    public int find(Node n)//returns the index of the given Node or -1 if not found
    {
        int pos = 0;
        if(isEmpty())
            return -1;
        else
        {
            Node curr = first;
            if(curr==n)
                return pos;
            //System.out.print(curr);
            while(curr.hasNext())
            {
                pos++;
                //System.out.print(">"+curr);
                if(curr.getNext()==n)
                    return pos;
                curr = curr.getNext();
            }
            return -1;
        }
    }
    
    public int findVague(String s)//similar to 'find' but the argument is a string representing the value of a specific node to find
    {
        int pos = 0;
        if(isEmpty())
            return -1;
        else
        {
            Node curr = first;
            if(s.equals(first.getValue().toString()))
                return pos;
            while(curr.hasNext())
            {
                pos++;
                if(s.equals(curr.getNext().getValue().toString()))
                    return pos;
                curr = curr.getNext();
            }                
            return -1;
        }
    }
    
    public Node getNode(int i)//returns a node at the given index
    {
        int pos = 0;
        Node curr = first;
        if(isEmpty())
            return null;
        else if(i==pos)
        {
            return first;
        }
        else if(pos>=getSize())
            return null;
        while(curr.hasNext() && pos<=i)
        {
            pos++;
            if(pos==i)
            {
                return curr.getNext();
            }
            curr = curr.getNext();
        }
        return null;
    }
    
    public void push(Node n)//inserts the given node at  the beginning of the list
    {
        if(isEmpty())
            first = n;
        else
        {
            n.setNext(first);
            first = n;
        }
    }
    
    public void insert(Node n)
    {
        push(n);
    }
    
    public void insert(Node n, int index)
    {
        int pos = 0;
        if(isEmpty())
            first = n;
        else if(index==0)
        {
            n.setNext(first);
            first = n;
        }
        else if(index>=getSize())
            append(n);
        else if(index>0)
            first.insert(n,--index);
    }
    

    
    public void append(Node n)//adds the given node to the end of the list
    {
        if(isEmpty())
            first = n;
        else
            first.append(n);
    }
    
    public boolean delete(Node n)//attempts to delete the given node, returns true if it was succesful false otherwise
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
        while(curr.hasNext())
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
    
    public boolean delete(int i)//attempts to delete the given index, if succesful it returns true, false otherwise
    {
        int pos = 0;
        Node curr = first;
        if(isEmpty())
            return false;
        else if(i==pos)
        {
            if(first.hasNext())
                first = first.getNext();
            else
                first = null;
            return true;
        }
        else if(i>getSize())
            return false;
        while(curr.hasNext()&&pos<=i)
        {
            pos++;
            if(pos==i)
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
    
    public void importData(String s)//converts the given file path to a file and calls importData
    {
        try
        {
            File f = new File(s);
            if(!f.canRead())
                throw new Exception("File is unreadable");
            importData(f);
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    public void importData(File f)//loads and reads a given file, then takes that contents and appends it to the list
    { 
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader reader = Files.newBufferedReader(f.toPath(), charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Pattern p = Pattern.compile("[^\\s,]+");
                Matcher m = p.matcher(line);  
                while(m.find())
                {
                    String s = line.substring(m.start(), m.end());
                    if(isNumeric(s))
                        append(new Node(Integer.parseInt(s)));
                    else
                        append(new Node(s));
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }
    
    public boolean isNumeric(String s)//returns true if the given string is a number
    {  
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }  
    
    public void print()//prints the contents of the list
    {
        if(!isEmpty())
            first.print();
        System.out.println();
    }
    
    public String toString()//returns the contents of the list as a string
    {
        if(isEmpty())
            return "Empty";
        else
            return first.toString();
    }
}
