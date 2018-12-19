/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import static semesterproject2.Utilities.fetchContent;

/**
 *
 * @author eriko
 */
public class NewsSourceDetector extends Detector{
    String apiKey;
    ArrayList<NewsSource> sources;
    public NewsSourceDetector(String key){
        apiKey = key;
        sources = getSources();
    }    
    
    public boolean containsKeywords(ArrayList<String> array) {
        return !compileSource(array).isEmpty();
    }

    public Output getKeyword(ArrayList<String> array) {
        String s = compileSource(array);
        NewsSource source = getSource(s);
        Keyword k = new Keyword(source.name,"NewsSource");
        k.object = source;
        return new Output(k,getRange(array,s));
    }
    
    private ArrayList<NewsSource> getSources(){
        ArrayList<NewsSource> result = new ArrayList();
        JsonObject obj = fetchContent("https://newsapi.org/v2/sources?apiKey="+apiKey);
        GsonBuilder builder = new GsonBuilder(); 
        builder.registerTypeAdapter(NewsSource.class, new NewsSourceDeserializer());
        Gson gson = builder.create(); 
        for(JsonElement source: obj.getAsJsonArray("sources"))
            result.add(gson.fromJson(source.getAsJsonObject(),NewsSource.class));
        return result;
    }
    
    private String compileSource(ArrayList<String> array){
        String curr = "";
        double highScore = 0.0;
        String source = "";
        for(String word:array.subList(0, Math.min(3, array.size()))){        
            curr += (curr.isEmpty()?"":" ")+word;
            NewsSource s = getSource(curr);
            if(s!=null){
                double score = ((double)curr.length())/s.name.length();
                score = score>1 ? 0 : score;
                if(score>highScore){
                    highScore = score;
                    source = curr;
                }
            }
        }
        return source;
    }
    
    private NewsSource getSource(String target){
        for(NewsSource s: sources)
            if(s.name.toUpperCase().contains(target.toUpperCase()))
                return s;
        return null;
    }
            
    private Range getRange(ArrayList<String> array, String word){
        for(int i=0;i<array.size();i++)
            if(!word.contains(array.get(i)))
                return new Range(0,i);
        return new Range(0,array.size());
    }

    
    
}
