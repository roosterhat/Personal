/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CommandInterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author eriko
 */
public class CommandInterpreter {
    private ArrayList<Command> commands;
    
    public CommandInterpreter(){
        commands = new ArrayList();
        Command help = new Command("HELP",x->helpCommand(x));
        help.potentialArguments = createArgumentList("0-1");
        help.usage = "Displays information about commands\nParameters (<String> Command ID | <String> Command Name)";
        commands.add(help);
    }
    
    private String helpCommand(ArrayList<String> args){
        String res = "=====Command Help=====\n";
            if(args.isEmpty()){
                int max = 0;
                for(Command c: commands)
                    max = Math.max(max, String.valueOf(c.id==null?0:c.id).length());
                for(Command command: commands)
                    res += String.format("%"+max+"s: %s\n",command.id==null?"":String.valueOf(command.id),command.name);
            }
            else{
                for(String s: args)
                    for(Command command: commands)
                        if(command.equals(s)){
                            res += (command.id==null?"":"("+command.id+")")+command.name+":\n"
                                    + command.usage+"\n"
                                    + "Takes "+command.displayPosibleArguments()+" arguments\n";
                            break;
                        }
            }
            return res;
    }
    
    public void addCommand(Command c){
        commands.add(c);
    }
    
    public void addCommand(String name, Function function){
        commands.add(new Command(name,function));
    }
    
    public void addCommand(int id, String name, Function function, String numArguments, String usage){
        Command c = new Command(name,function);
        c.id = id;
        c.usage = usage;
        c.potentialArguments = createArgumentList(numArguments);
        commands.add(c);
    }
    
    private ArrayList<Integer> createArgumentList(String args){// specify command arguments through a list of numbers sepereated by ',' or ranges of numbers seperated by '-'
        Set<Integer> arguments = new HashSet();
        for(String arg: args.split(",")){
            if(arg.matches("\\d+-\\d+")){
                String[] numRange = arg.split("-");
                try{
                    int start = Integer.valueOf(numRange[0]);
                    int end = Integer.valueOf(numRange[1]);
                    for(int i = start; i<=end; i++)
                        arguments.add(i);
                }catch(Exception e){System.out.println("Failed to add value range '"+arg+"', Error: "+e.getMessage());}
            }
            else{
                try{
                    arguments.add(Integer.valueOf(arg));
                }catch(Exception e){System.out.println("Failed to add value '"+arg+"', Error: "+e.getMessage());}
            }
        }
        ArrayList<Integer> temp = new ArrayList(arguments);
        temp.sort((x,y)->x-y);
        return temp;
    }
    
    public String interpretCommand(String arg){
        ArrayList<String> args = new ArrayList(Arrays.asList(arg.split(" ")));
        if(args.isEmpty())
            return "Invalid Syntax: empty input";
        for(Command c : commands)
            if(c.equals(args.get(0))){
                if(c.checkArguments(args.size()-1))
                    return removeTrailingNewline(c.execute(new ArrayList(args.subList(1, args.size()))));
                else
                    return "Invalid Syntax: expected "+c.displayPosibleArguments()+" arguments, found "+(args.size()-1)+" for command '"+c.name+"'";
            }                
        return "Invalid Syntax: unknown command '"+args.get(0)+"' try 'help'";
    }
    
    private String removeTrailingNewline(String s){
        s = s.trim();
        if(s.lastIndexOf("\n")==s.length()-1)
            return s.substring(0, s.length()-1);
        return s;
    }
}
