/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jibble.pircbot.PircBot;

/**
 *
 * @author eriko
 */
public class ChatBot extends PircBot{
    String name = "WeatherBot_Storm";
    String nickName = "wbot";
    ArrayList<Command> commands;
    Keywords keywords;
    
    public ChatBot(){
        setName(name);
        commands = new ArrayList();
        createCommands();
        keywords = new Keywords(getDetectors());
        //onMessage("","","","","wbot date and time, weather in houston, and news abc");
    }
    
    private void createCommands(){            
        Command help = new Command("Help");
            help.addKeyword("help");
            help.setFunction(x->{
                String res = "::Commands::\n";
                for(Command c: commands)
                    res += c + "\n";
                return res;
            });
        commands.add(new WeatherCommand());
        commands.add(new TimeCommand());
        commands.add(new NewsCommand());
        commands.add(help);
    }
    
    private Command getCommand(String name){
        for(Command c: commands)
            if(c.title.equalsIgnoreCase(name))
                return c;
        return null;
    }
    
    private void removeUnnecessary(ArrayList<String> array){
        String unnecessary = "what is in for and the"+name+nickName;
        array.removeIf(x->unnecessary.contains(x));
    }
    
    private ArrayList<Detector> getDetectors(){
        ArrayList<Detector> results = new ArrayList();
        commands.forEach(x->results.add(x.getDetector()));
        results.add(new LocationDetecor());
        results.add(new NewsSourceDetector(((NewsCommand)getCommand("News")).APIKey));
        return results;
    }
    
    private Set<Command> getCommandOrder(ArrayList<Keyword> keywords){
        Set<Command> results = new HashSet();
        for(Keyword k : keywords)
            for(Command c: commands)
                if(c.relavent.contains(k.type))
                    results.add(c);
        return results;
    }
    
    public void onMessage(String channel, String sender, String login, String hostName, String message){
        String result = "";
        if(message.contains(name) || message.contains(nickName)){
            message = message.replaceAll("(?=[\\S])[\\W]", "");
            ArrayList<String> parsedMessage = new ArrayList(Arrays.asList(message.split(" ")));
            removeUnnecessary(parsedMessage);
            ArrayList<Keyword> parsedKeywords = keywords.findKeywords(parsedMessage);
            for(Command c: getCommandOrder(parsedKeywords)){
                ArrayList<Keyword> relaventKeywords = Keywords.getRelaventKeywords(parsedKeywords, c.relavent);
                if(!relaventKeywords.isEmpty()){
                    try{
                        result += c.execute(relaventKeywords)+"\n";
                        parsedKeywords.removeAll(relaventKeywords);
                    }
                    catch(Exception e){result = e.getMessage();}
                }
            }
            if(result.isEmpty())
                result = "Unknown Command, Try asking 'help'";
            System.out.println(result);
            for(String s: result.split("\n"))
                if(s!=null && !s.isEmpty())
                    sendMessage(channel,s);
        }
    }
}