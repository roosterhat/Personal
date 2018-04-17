/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;

/**
 *
 * @author eriko
 */
public class Command {
    String title,usage;
    ArrayList<String> keywords;
    ArrayList<String> relavent;
    private CommandFunction function;
    private Detector detector;
    public Command(String title){
        this.title = title;
        relavent = new ArrayList();
        keywords = new ArrayList();
        function = x->"";
        detector = new GeneralDetector(title);
        relavent.add(title);
    }
    
    public Command(String title, ArrayList<String> keywords, CommandFunction function){
        this.title = title;
        this.keywords = keywords;
        this.function = function;
        detector = new GeneralDetector(title);
        ((GeneralDetector)detector).keywords.addAll(keywords);
        relavent = new ArrayList();
        relavent.add(title);
    }
    
    public void setDetector(Detector d){
        detector = d;
    }
    
    public Detector getDetector(){
        return detector;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public void addKeyword(String keyword){
        keywords.add(keyword);
        if(detector instanceof GeneralDetector)
            ((GeneralDetector)detector).keywords.add(keyword);
    }
    
    public void setFunction(CommandFunction function){
        this.function = function;
    }
    
    public String execute(ArrayList<Keyword> args)throws Exception{
        return function.execute(args);
    }
    
    public String toString(){
        return title+" keywords:"+keywords+"\n"+usage;
    }
}
