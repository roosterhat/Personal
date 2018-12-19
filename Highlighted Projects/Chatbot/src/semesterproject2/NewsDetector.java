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
public class NewsDetector extends GeneralDetector{
    public NewsDetector(ArrayList<String> keywords){
        super(keywords,"News");        
    }

    @Override
    public Output getKeyword(ArrayList array) {
        String keyword = (String)array.get(0);
        if(containsKeywords(array)){
            if(keyword.equalsIgnoreCase("Search")){
                String search = compileArray(new ArrayList(array.subList(1, array.size())));
                Keyword k = new Keyword("Search","News");
                k.object = search;
                return new Output(k,new Range(0,array.size()));
            }
            else
                return new Output(new Keyword(keyword,"News"),new Range(0,1));
        }
        else
            return null;
    }
    
    private String compileArray(ArrayList<String> array){
        String result = "";
        for(String s:array)
            result+=s+(array.indexOf(s)<array.size()-1?" ":"");
        return result;
    }
    
}
