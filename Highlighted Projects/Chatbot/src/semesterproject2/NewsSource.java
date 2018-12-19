/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 *
 * @author eriko
 */
public class NewsSource {
    String id,name,description,category,language,country;
    public NewsSource(String id, String name, String description, String category, String language, String country){
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.language = language;
        this.country = country;
    }
}

class NewsSourceDeserializer implements JsonDeserializer<NewsSource>{
    public NewsSource deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = je.getAsJsonObject();
        String id = obj.get("id").getAsString();
        String name = obj.get("name").getAsString();
        String desc = obj.get("description").getAsString();
        String cat = obj.get("category").getAsString();
        String lang = obj.get("language").getAsString();
        String country = obj.get("country").getAsString();
        return new NewsSource(id,name,desc,cat,lang,country);
    }
    
}
