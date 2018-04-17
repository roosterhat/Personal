/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import static semesterproject2.Utilities.fetchContent;

/**
 *
 * @author eriko
 */
public class NewsFunction implements CommandFunction{
    String apiKey;
    public NewsFunction(String key){
        apiKey = key;
    }
    
    @Override
    public String execute(ArrayList<Keyword> args) {
        String result = "";
        Keyword k = Keywords.getFirst(args, "News");
        if(k.keyword.equalsIgnoreCase("News") || k.keyword.equalsIgnoreCase("Headlines")){
            if(Keywords.containsType(args, "NewsSource")){
                NewsSource source = (NewsSource)Keywords.getFirst(args, "NewsSource").object;
                result = "News from "+source.name+"\n";
                result += compileNews(fetchContent("https://newsapi.org/v2/top-headlines?sources="+source.id+"&apiKey="+apiKey));
            }
            else{
                result = "News for the US\n";
                result += compileNews(fetchContent("https://newsapi.org/v2/top-headlines?country=us&apiKey="+apiKey));
            }
        }
        else if(k.keyword.equalsIgnoreCase("Search")){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
            String to = simpleFormatter.format(c.getTime());
            c.add(Calendar.DATE, -7);
            String from = simpleFormatter.format(c.getTime());
            String search = ((String)k.object).replaceAll(" ", "%20");
            result = "Articles related to '"+k.object+"'\n";
            result += compileSearch(fetchContent("https://newsapi.org/v2/everything?q=%27"+search+"%27"
                    + "&from="+from+"&to="+to+"&sortBy=popularity&language=en&apiKey="+apiKey));
        }
        else if(k.keyword.equalsIgnoreCase("Sources")){
            JsonObject obj = fetchContent("https://newsapi.org/v2/sources?apiKey="+apiKey);
            result = "Possible News Sources: ";
            for(JsonElement source: obj.getAsJsonArray("sources"))
                result+=source.getAsJsonObject().get("name").getAsString()+", ";
        }
            
        return result;          
    }
    
    public String compileNews(JsonObject news){
        String result = "";
        int count = 0;
        if(news!=null)
            for(JsonElement e: news.getAsJsonArray("articles")){
                JsonObject obj = e.getAsJsonObject();
                if(count<10)
                    result+=obj.get("title").getAsString()+" - "+obj.get("url").getAsString()+"\n";
                count++;
            }
        return result;
    }
    
    public String compileSearch(JsonObject search){
        String result = "";
        int count = 0;
        if(search!=null)
            for(JsonElement e: search.getAsJsonArray("articles")){
                JsonObject obj = e.getAsJsonObject();
                if(count<10)
                    result+=obj.get("source").getAsJsonObject().get("name").getAsString()+": "+obj.get("title").getAsString()+" - "+obj.get("url").getAsString()+"\n";
                count++;
            }
        return result;
    }
    
}
