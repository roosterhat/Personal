/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg33445.project.pkg2;

import CommandInterpreter.Command;
import CommandInterpreter.CommandInterpreter;
import java.util.Scanner;

/**
 *
 * @author eriko
 */
public class Project2 {

    static boolean running = true;
    public static void main(String[] args) {
        new Project2().run();
    }
    
    public void run(){  //main function
        Scanner sc = new Scanner(System.in);
        LinkedList<Magazine> list = new LinkedList();
        CommandInterpreter ci = new CommandInterpreter();
        setCommands(ci,list,sc);
        System.out.println("LinkedList Interaction Interface\nType 'help' to see commands");
        while(running){
            System.out.print("Enter Command: ");
            String command = sc.nextLine();
            String res = ci.interpretCommand(command);
            System.out.println(res);
        }
    }
    
    public void setCommands(CommandInterpreter c, LinkedList<Magazine> l, Scanner sc){  //creates all commands        
        c.addCommand(1,"Empty",x->{
            l.makeEmpty();
            return "List Cleared";
        },"0","Clears the List\nParameters (None)");
        
        c.addCommand(2,"FIND",x->{    //calls 'findID', allows for parameters (<int> id)
            if(x.isEmpty()){
                System.out.print("Enter ID to find: ");
                int id = sc.nextInt();
                return l.findID(id).toString();
            }
            else{
                try{
                    int id = Integer.valueOf(x.get(0));
                    return l.findID(id).toString();
                }catch(Exception e){
                    return "Invalid ID specified, expected Integer";
                }
            }
        },"0,1","Returns the value contained by the element with the specified ID\nParameters (<int> ID)");
        
        c.addCommand(3,"PREPEND",x->{ //calls 'insertAtFront', allows for parameters (<int> id, <String> magazineName, <String> publisher)
            if(x.isEmpty()){
                System.out.print("Enter Magazine ID: ");
                int id = sc.nextInt();sc.nextLine();
                System.out.print("Enter Magazine Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Publisher Name: ");
                String publisher = sc.nextLine();
                if(!l.insertAtFront(new Magazine(id,name,publisher)))
                    return "Failed to Add Magazine: ID already exists";
            }
            else{
                if(x.size()>=3){
                    try{
                        int id = Integer.valueOf(x.get(0));
                        String name = x.get(1);
                        String publ = x.get(2);
                        if(!l.insertAtFront(new Magazine(id,name,publ)))
                            return "Failed to Add Magazine: ID already exists";
                    }catch(Exception e){
                        return "Invalid Arguments, expected: 'id' 'name' 'publisher'";
                    }
                }
                else
                    return "Invalid Argument Count, expected: size = 3";
            }
            return "Magazine Added";
        },"0,3","Creates a new Magazine and prepends it to the list\nParameters (<int> ID, <String> Name, <String> Publisher)");
        
        c.addCommand(4,"DELETE",x->{  //calls 'deleteFromFront'
            return "Node Deleted\n"+l.deleteFromFront(); 
        },"0","Deletes the first element of the list\nParameters (None)");
        
        c.addCommand(5,"DELETEID",x->{    //calls 'delete', allows for parameters (<int> id)
            if(x.isEmpty()){
                System.out.print("Enter ID: ");
                int id = sc.nextInt();
                Magazine m = l.delete(id);
                return m==null?"ID does not exist":m.toString();
            }
            else{
                try{
                    int id = Integer.valueOf(x.get(0));
                    Magazine m = l.delete(id);
                    return m==null?"ID does not exist":m.toString();
                }catch(Exception e){
                    return "Invalid Argument, expected Integer";
                }
            }
        },"0-1","Deletes the element with the specified ID\nParameters (<int> ID)");
        
        c.addCommand(6,"PRINT",x->{   //calls 'toString'
            return l.toString();
        },"0","Prints all elements in list\nParameters (None)");
        
        c.addCommand(7,"DONE",x->{    //Returns "DONE" signaling the loop to stop
            Project2.running = false;
            return "DONE";
        },"0","Exists the program\nParameters (None)");
    }
    
}

