/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CommandInterpreter;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class Command {
    Integer id;
    String name,usage;
    Function function;
    ArrayList<Integer> potentialArguments;
    
    public Command(String name, Function function){
        this.name = name;
        this.function = function;
        this.potentialArguments = new ArrayList();
    }
    
    public String execute(ArrayList<String> args){
        return function.execute(args);
    }
    
    public boolean checkArguments(int size){
        if(potentialArguments.isEmpty())
            return true;
        else
            return potentialArguments.contains(size);
    }
    
    public String displayPosibleArguments(){
        if(potentialArguments.isEmpty())
            return "[Unlimited]";
        int index = 0, cur = potentialArguments.get(index);
        String res = "["+cur;
        for(int i = 1; i < potentialArguments.size(); i++){
            if(cur+1<potentialArguments.get(i) || i == potentialArguments.size()-1){
                if(i-1 == index && cur+1<potentialArguments.get(i))
                    res+=",";
                else
                    res+="-";
                res+=potentialArguments.get(i);
            }
            cur = potentialArguments.get(i);
                
        }
        return res+"]";
    }
    
    public boolean equals(Object val){
        if(val instanceof String)
            return name.equalsIgnoreCase((String)val) || String.valueOf(id).equals(val);
        else
            return super.equals(val);
    }
}
