/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg3;

import CommandInterpreter.CommandInterpreter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author eriko
 */
public class Project3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Project3().run(args);        
    }
    
    public void run(String[] args){
        CommandInterpreter c = new CommandInterpreter();
        c.delimeter = ":";
        LazyBinarySearchTree bs = new LazyBinarySearchTree();
        createCommands(c,bs);
        if(args.length == 2){
            try{
                BufferedReader br = new BufferedReader(new FileReader(new File(args[0]))); 
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
                String s;
                while ((s = br.readLine()) != null) 
                  writer.write(c.interpretCommand(s)+"\n");
                writer.close();
                br.close();
            }catch(Exception e){System.out.println("Error reading file, Error: "+e.getMessage());}
        }
        else{
            System.out.println("Incorrect Arguments, Specify an input and output file, "+args.length+" arguments found");
        }
        
    }
    
    public void createCommands(CommandInterpreter c, LazyBinarySearchTree bs){
        c.addCommand(1, "insert", x->{
            try{
                return bs.insert(Integer.valueOf(x.get(0)))+"";
            }catch(IllegalArgumentException e){
                return "Error in insert: IllegalArgumentException raised, Error: "+e.getMessage();
            }catch(Exception e){
                return "Error in insert: Invalid value specified, given '"+x.get(0)+"' expected Integer";
            }
        }, "1", "Inserts the given element into the tree.\nReturns true if the value was inserted logically");
        
        c.addCommand(2,"delete", x->{
            try{
                return bs.delete(Integer.valueOf(x.get(0)))+"";
            }catch(IllegalArgumentException e){
                return "Error in delete: IllegalArgumentException raised, Error: "+e.getMessage();
            }catch(Exception e){
                return "Error in delete: Invalid value specified, given '"+x.get(0)+"' expected Integer";
            }
        },"1", "Deletes the given element from the tree.\nReturns true if the value was found");
        
        c.addCommand(3, "contains", x->{
            try{
                return bs.contains(Integer.valueOf(x.get(0)))+"";
            }catch(IllegalArgumentException e){
                return "Error in contains: IllegalArgumentException raised, Error: "+e.getMessage();
            }catch(Exception e){
                return "Error in contains: Invalid value specified, given '"+x.get(0)+"' expected Integer";
            }
        },"1", "Returns true if the value is contained within the tree");
        
        c.addCommand(4, "findmax", x->{
            return bs.findMax()+"";
        },"0", "Returns the largest value in the tree");
        
        c.addCommand(5, "findmin", x->{
            return bs.findMin()+"";
        },"0", "Returns the smallest value in the tree");
        
        c.addCommand(6, "size", x->{
            return bs.size()+"";
        },"0", "Returns the number of elements contained within the tree");
        
        c.addCommand(7, "height", x->{
            return bs.height()+"";
        },"0", "Returns the maximum height of the tree");
        
        c.addCommand(6, "printtree", x->{
            return bs.toString();
        },"0", "Returns the preorder representation of the tree");
    }
}
