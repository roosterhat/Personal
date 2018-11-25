/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg33445.project.pkg2;

import java.util.ArrayList;
import java.util.Arrays;
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
        c.commands.add(new Command(0,"HELP",x->{    //displays information about commands
            String res = "=====Command Help=====\n";
            if(x.isEmpty()){
                for(Command command: c.commands)
                    res += command.id+": "+command.name+"\n";
            }
            else{
                for(String s: x)
                    for(Command command: c.commands)
                        if(command.equals(s)){
                            res += "("+command.id+")"+command.name+":\n"+command.usage+"\n";
                            break;
                        }
            }
            return res;
        },"Displays information about commands\nParameters (<String> Command ID | <String> Command Name)"));
        
        c.commands.add(new Command(1,"EMPTY",x->{   //calls 'makeEmpty'
            l.makeEmpty();
            return "List Cleared";
        },"Clears the List\nParameters (None)"));
        
        c.commands.add(new Command(2,"FIND",x->{    //calls 'findID', allows for parameters (<int> id)
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
        },"Returns the value contained by the element with the specified ID\nParameters (<int> ID)"));
        
        c.commands.add(new Command(3,"PREPEND",x->{ //calls 'insertAtFront', allows for parameters (<int> id, <String> magazineName, <String> publisher)
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
        },"Creates a new Magazine and prepends it to the list\nParameters (<int> ID, <String> Name, <String> Publisher)"));
        
        c.commands.add(new Command(4,"DELETE",x->{  //calls 'deleteFromFront'
            return "Node Deleted\n"+l.deleteFromFront(); 
        },"Deletes the first element of the list\nParameters (None)"));
        
        c.commands.add(new Command(5,"DELETEID",x->{    //calls 'delete', allows for parameters (<int> id)
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
        },"Deletes the element with the specified ID\nParameters (<int> ID)"));
        
        c.commands.add(new Command(6,"PRINT",x->{   //calls 'toString'
            return l.toString();
        },"Prints all elements in list\nParameters (None)"));
        
        c.commands.add(new Command(7,"DONE",x->{    //Returns "DONE" signaling the loop to stop
            Project2.running = false;
            return "DONE";
        },"Exists the program\nParameters (None)"));
    }
    
}

class CommandInterpreter{   //contains all commands and interprets inputs and executes commands
    ArrayList<Command> commands;
    
    public CommandInterpreter(){
        commands = new ArrayList();
    }
    
    public String interpretCommand(String arg){
        ArrayList<String> args = new ArrayList(Arrays.asList(arg.split(" ")));
        for(Command c : commands)
            if(c.name.equalsIgnoreCase(args.get(0)) || String.valueOf(c.id).equals(args.get(0)))
                return removeTrailingNewline(c.execute(new ArrayList(args.subList(1, args.size()))));
        return "Invalid Command";
    }
    
    private String removeTrailingNewline(String s){
        s = s.trim();
        if(s.lastIndexOf("\n")==s.length()-1)
            return s.substring(0, s.length()-1);
        return s;
    }
}

class Command{ 
    int id;
    String name,usage;
    Function function;
    public Command(int id, String name, Function function){
        this(id, name, function, "");
    }
    
    public Command(int id, String name, Function function, String usage){
        this.id = id;
        this.name = name;
        this.usage = usage;
        this.function = function;
    }
    
    public String execute(ArrayList<String> array){
        return function.execute(array);
    }
    
    public boolean equals(String val){
        return name.equalsIgnoreCase(val) || String.valueOf(id).equals(val);
    }
}

interface Function{
    public String execute(ArrayList<String> array);
}
