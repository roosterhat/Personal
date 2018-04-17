/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author eriko
 */
public class WeatherReport {
    enum value{DESCRIPTION, TEMPERATURE, HUMIDITY, WIND}; 
    ArrayList forecasts;
    String description,location;
    double temperature,high,low,humidity,windspeed,winddir;
    Calendar time;
    public WeatherReport(String location, String day, String description, double high, double low, double humidity, double windspeed, double winddir){
        this.location = location;
        this.time = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try{this.time.setTime(sdf.parse(day));}catch(Exception e){System.out.println(e);}
        this.description = description;
        this.temperature = (high+low)/2;
        this.high = high;
        this.low = low;
        this.humidity = humidity;
        this.windspeed = windspeed;
        this.winddir = winddir;
        forecasts = new ArrayList();
        forecasts.add(description);
    }
    
    private String degreeToCardinalDirection(double degree){
        String[] directions = {"North", "North East", "East", "South East", "South", "South West", "West", "North West"};
        return directions[((int)(degree/45+.5))%8];
    }
    
    public String createReport(value... weatherValues){
        ArrayList<value> values = new ArrayList(Arrays.asList(weatherValues));
        if(weatherValues.length==0)
            values.addAll(Arrays.asList(value.values()));
        return createReport(values,true);
    }
    
    public String getTime(boolean specific){
        Date d = time.getTime();
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("E, MMM dd"+(specific ? " 'at' hha":""));
        return simpleFormatter.format(d);
    }
    
    public String getDescription(){
        Map<String, Integer> occurrences = (Map<String,Integer>)forecasts.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        ArrayList<Map.Entry<String, Long>> occur = new ArrayList(occurrences.entrySet());
        occur.sort((x,y)->(int)(x.getValue()-y.getValue()));
        return occur.get(0).getKey();
    }
    
    public String createReport(ArrayList<value> weatherValues,boolean specific){
        String report = getTime(specific)+(!weatherValues.isEmpty() && weatherValues.get(0).equals(value.DESCRIPTION) ? " is " : " has ");
        for(value v: weatherValues){
            if(v.equals(value.DESCRIPTION))
                report+=getDescription();
            if(v.equals(value.TEMPERATURE))
                report+=String.format("a high of %.2fF and a low of %.2fF",high,low);
            if(v.equals(value.HUMIDITY))
                report+=String.format("a humidity of %.2f%%",humidity);
            if(v.equals(value.WIND))
                report+=String.format("winds with speeds of %.2f m/s out of the %s",windspeed,degreeToCardinalDirection(winddir));
            report += weatherValues.indexOf(v)==weatherValues.size()-1 ? "" : ", ";
        }
        return report.substring(0, report.length());
    }
    
    public String toString(){
        return "Location: "+location+
                "\nDescription: "+description+
                "\nTemperature: "+temperature+
                "\nHumidity: "+humidity+
                "\nWind Speed: "+windspeed+
                "\nWind Direction: "+winddir;
    }
}

class WeatherReportHourlyDeserializer implements JsonDeserializer<WeatherReport> {
    public WeatherReport deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)throws JsonParseException {
        String location = "";
        String description = json.getAsJsonObject().get("weather").getAsJsonArray().size()>0 && 
                json.getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().has("description")?
                json.getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString():"No Forecast Data";
        double max = json.getAsJsonObject().get("main").getAsJsonObject().has("temp_max")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("temp_max").getAsDouble():Double.NaN;
        double min = json.getAsJsonObject().get("main").getAsJsonObject().has("temp_min")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("temp_max").getAsDouble():Double.NaN;
        double humidity = json.getAsJsonObject().get("main").getAsJsonObject().has("humidity")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("humidity").getAsDouble():Double.NaN;
        double speed = json.getAsJsonObject().get("wind").getAsJsonObject().has("speed")?
                json.getAsJsonObject().get("wind").getAsJsonObject().get("speed").getAsDouble():Double.NaN;
        double dir = json.getAsJsonObject().get("wind").getAsJsonObject().has("deg") ? 
                json.getAsJsonObject().get("wind").getAsJsonObject().get("deg").getAsDouble() : Double.NaN;
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = json.getAsJsonObject().has("dt_txt")?
                json.getAsJsonObject().get("dt_txt").getAsString():simpleFormatter.format(Calendar.getInstance().getTime());
        return new WeatherReport(location,time,description,max,min,humidity,speed,dir);
    }
}

class WeatherReportDeserializer implements JsonDeserializer<WeatherReport> {
    public WeatherReport deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)throws JsonParseException {
        String location = json.getAsJsonObject().has("name")?
                json.getAsJsonObject().get("name").getAsString():"No Location Data";
        String description = json.getAsJsonObject().get("weather").getAsJsonArray().size()>0 && 
                json.getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().has("description")?
                json.getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString():"No Forecast Data";
        double max = json.getAsJsonObject().get("main").getAsJsonObject().has("temp_max")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("temp_max").getAsDouble():Double.NaN;
        double min = json.getAsJsonObject().get("main").getAsJsonObject().has("temp_min")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("temp_max").getAsDouble():Double.NaN;
        double humidity = json.getAsJsonObject().get("main").getAsJsonObject().has("humidity")?
                json.getAsJsonObject().get("main").getAsJsonObject().get("humidity").getAsDouble():Double.NaN;
        double speed = json.getAsJsonObject().get("wind").getAsJsonObject().has("speed")?
                json.getAsJsonObject().get("wind").getAsJsonObject().get("speed").getAsDouble():Double.NaN;
        double dir = json.getAsJsonObject().get("wind").getAsJsonObject().has("deg") ? 
                json.getAsJsonObject().get("wind").getAsJsonObject().get("deg").getAsDouble() : Double.NaN;
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = json.getAsJsonObject().has("dt_txt")?
                json.getAsJsonObject().get("dt_txt").getAsString():simpleFormatter.format(Calendar.getInstance().getTime());
        return new WeatherReport(location,time,description,max,min,humidity,speed,dir);
    }
}
