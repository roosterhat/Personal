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
public class Location {
    String mainText,type;
    public Location(String main, String type){
        mainText = main;
        this.type = type;
    }
    public String toString(){
        return "Location [main: "+mainText+", type: "+type+"]";
    }
}

class LocationDeserializer implements JsonDeserializer<Location> {
    public Location deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)throws JsonParseException {
        if(json.getAsJsonObject().get("predictions").getAsJsonArray().size()==0)
            return null;
        JsonObject j = json.getAsJsonObject().get("predictions").getAsJsonArray().get(0).getAsJsonObject();
        String main = j.get("structured_formatting").getAsJsonObject().get("main_text").getAsString();
        String type = j.get("types").getAsJsonArray().get(0).getAsString();
        return new Location(main,type);
    }
}
