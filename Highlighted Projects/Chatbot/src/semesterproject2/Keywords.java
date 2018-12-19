/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author eriko
 */
public class Keywords {
    ArrayList<Detector> detectors;
    public Keywords(){
        detectors = new ArrayList();
    }
    
    public Keywords(ArrayList<Detector> detectors){
        this.detectors = detectors;
    }
    
    public ArrayList<Keyword> findKeywords(ArrayList<String> array){
        ArrayList<Keyword> keywords = new ArrayList();
        int start = 0;
        while(start<array.size()){
            boolean found = false;
            ArrayList temp = new ArrayList(array.subList(start, array.size()));
            for(Detector d: detectors){
                if(d.containsKeywords(temp)){
                    found = true;
                    Output output = d.getKeyword(temp);
                    if(output==null)
                        start++;
                    else{
                        start += output.range.end;
                        keywords.add((Keyword)output.value);
                    }
                    break;
                }
            }
            if(!found)
                start++;
        }
        return keywords;
    }
   
    
    public static boolean containsKeyword(ArrayList<Keyword> array, String keyword){
        for(Keyword k: array)
            if(k.keyword.equalsIgnoreCase(keyword))
                return true;
        return false;
    }
    
    public static boolean containsType(ArrayList<Keyword> array, String type){
        for(Keyword k: array)
            if(k.type.equals(type))
                return true;
        return false;
    }
    
    public static Keyword get(ArrayList<Keyword> array, String keyword){
        for(Keyword k: array)
            if(k.keyword.equalsIgnoreCase(keyword))
                return k;
        return null;
    }
    
    public static Keyword getFirst(ArrayList<Keyword> array, String type){
        for(Keyword k: array)
            if(k.type.equals(type))
                return k;
        return null;
    }
    
    public static int indexOf(ArrayList<Keyword> array, Keyword k){
        for(int i=0;i<array.size();i++)
            if(k.equals(array.get(i)))
                return i;
        return -1;
    }
    
    public static int indexOf(ArrayList<Keyword> array, String keyword){
        return indexOf(array,get(array,keyword));
    }
    
    public static ArrayList<Keyword> getRelaventKeywords(ArrayList<Keyword> array, String... args){
        return getRelaventKeywords(array,new ArrayList(Arrays.asList(args)));
    }
    
    public static ArrayList<Keyword> getRelaventKeywords(ArrayList<Keyword> array, ArrayList args){
        ArrayList<Keyword> result = new ArrayList();
        for(Keyword keyword: array)
            if(args.contains(keyword.type))
                result.add(keyword);
        return result;
    }
   
    
}
