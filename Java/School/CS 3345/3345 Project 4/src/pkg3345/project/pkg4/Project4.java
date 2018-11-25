/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg4;

import CommandInterpreter.CommandInterpreter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eriko
 */
public class Project4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Project4().run(args);
    }
   public void run(String[] args){
        if(args.length == 2){
            try{
                CommandInterpreter c = new CommandInterpreter();
                BufferedReader br = new BufferedReader(new FileReader(new File(args[0]))); 
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
                String s;
                Map<String,InputType> types = new HashMap();
                types.put("String", new InputType<String>(new RedBlackTree<String>(), x->{
                    try{return String.valueOf(x);}
                    catch(Exception e){throw new ConversionException("Expected 'String', got '"+x.getClass().getName()+"'");}
                }));
                types.put("Integer", new InputType<Integer>(new RedBlackTree<Integer>(), x->{
                    try{return Integer.valueOf(x);}
                    catch(Exception e){throw new ConversionException("Expected 'Integer', got '"+x.getClass().getName()+"'");}
                }));
                if((s = br.readLine()) != null){
                    if(types.containsKey(s))
                        createCommands(c,types.get(s));
                    else{
                        writer.write("Only works for objects Integers and Strings");
                        throw new Exception("Only works for objects Integers and Strings");
                    }
                }
                
                c.delimeter = ":";
                
                while ((s = br.readLine()) != null)
                    if(!s.isEmpty())
                        writer.write(c.interpretCommand(s)+"\n");
                writer.close();
                br.close();
            }catch(Exception e){System.out.println("Error reading file, Error: "+e.getMessage());}
        }
        else{
            System.out.println("Incorrect Arguments, Specify an input and output file, "+args.length+" arguments found");
        }
        
    }
    
    public void createCommands(CommandInterpreter c, InputType type){
        c.addCommand(1, "insert", x->{
            try{
                return type.tree.insert(type.converter.convert(x.get(0)))+"";
            }catch(IllegalArgumentException e){
                return "Error in insert: IllegalArgumentException raised, Error: "+e.getMessage();
            }catch(ConversionException e){
                return "Error in insert: Unable to convert given value '"+x.get(0)+"', "+e.getMessage();
            }catch(Exception e){
                return "Error in insert: Error: "+e;
            }
        }, "1", "Inserts the given element into the tree.\nReturns true if the value was inserted logically");
        
        c.addCommand(3, "contains", x->{
            try{
                return type.tree.contains(type.converter.convert(x.get(0)))+"";
            }catch(IllegalArgumentException e){
                return "Error in contains: IllegalArgumentException raised, Error: "+e.getMessage();
            }catch(ConversionException e){
                return "Error in contains: Unable to convert given value '"+x.get(0)+"', "+e.getMessage();
            }catch(Exception e){
                return "Error in contains: Error: "+e;
            }
        },"1", "Returns true if the value is contained within the tree");
        
        c.addCommand(6, "printtree", x->{
            return type.tree.toString();
        },"0", "Returns the preorder representation of the tree");
    }
}

class InputType<T extends Comparable>{
    RedBlackTree<T> tree;
    Converter<T> converter;
    public InputType(RedBlackTree<T> tree, Converter<T> converter){
        this.tree = tree;
        this.converter = converter;
    }
}

interface Converter<T extends Comparable>{
    public T convert(String s)throws ConversionException;
}

class ConversionException extends Exception{
    public ConversionException(String error){
        super(error);
    }
}
