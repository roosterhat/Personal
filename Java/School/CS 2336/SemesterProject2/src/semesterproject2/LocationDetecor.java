/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import static semesterproject2.Utilities.*;

/**
 *
 * @author eriko
 */
public class LocationDetecor extends Detector{
    String apiKey = "AIzaSyAjdCJ3GVrNNWOJNsZu3Ru4XtCeF_LxVYM";
    String locationURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input='%s'&key="+apiKey;

    public boolean containsKeywords(ArrayList array) {
        return !compileLocation(array).isEmpty();
    }

    public Output getKeyword(ArrayList array) {
        String location = compileLocation(array);
        Location loc = getLocation(location);
        if(loc==null)
            return null;
        Keyword k = new Keyword(loc.mainText,"Location");
        k.object = loc;
        return new Output(k,getRange(array,location));
    }
    
    private String compileLocation(ArrayList<String> words){
        String curr = "";
        double highScore = 0.0;
        String location = "";
        for(String s:words.subList(0, Math.min(3, words.size()))){
            if(s.equalsIgnoreCase("here"))
                return "";          
            curr += s;
            Location l = getLocation(curr);
            if(l!=null)
                if(l.type.equals("locality") || l.type.equals("postal_code")){
                    double score = ((double)curr.length())/l.mainText.length();
                    score = score>1 ? 0 : score;
                    if(score>highScore){
                        highScore = score;
                        location = curr;
                    }
                }
        }
        return location;
    }
    
    private Location getLocation(String proposedLocation){
        JsonObject obj = fetchContent(String.format(locationURL, proposedLocation));
        if(obj==null)
            return null;
        GsonBuilder builder = new GsonBuilder(); 
        builder.registerTypeAdapter(Location.class, new LocationDeserializer());
        Gson gson = builder.create(); 
        return gson.fromJson(obj.toString(),Location.class);
    }
    
    private Range getRange(ArrayList<String> array, String word){
        for(int i=0;i<array.size();i++)
            if(!word.contains(array.get(i)))
                return new Range(0,i);
        return new Range(0,array.size());
    }
    
}
