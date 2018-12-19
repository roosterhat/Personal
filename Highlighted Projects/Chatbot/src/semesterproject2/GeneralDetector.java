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
public class GeneralDetector extends Detector{
    String type;
    ArrayList<String> keywords;
    public GeneralDetector(String type){
        this.type = type;
        keywords = new ArrayList();
    }
    
    public GeneralDetector(ArrayList<String> keywords, String type){
        this.keywords = keywords;
        this.type = type;
    }
    
    @Override
    public boolean containsKeywords(ArrayList array) {
        if(!array.isEmpty())
            for(String i : keywords)
                //if(((String)array.get(0)).toUpperCase().contains(i.toUpperCase()))
                if(i.equalsIgnoreCase((String)array.get(0)))
                    return true;  
        return false;
    }

    @Override
    public Output<Keyword> getKeyword(ArrayList array) {
        if(containsKeywords(array))
            return new Output(new Keyword((String)array.get(0),type),new Range(0,1));
        return null;
    }
    
}
