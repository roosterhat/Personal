/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import static semesterproject2.Utilities.*;

/**
 *
 * @author eriko
 */
public class WeatherFunction implements CommandFunction{
    WeatherCommand _main;
    String forecastURL = "http://api.openweathermap.org/data/2.5/forecast?%s&units=imperial&appid=42895c8daba263ae387b71ff97c7eeb1";
    String weatherURL = "http://api.openweathermap.org/data/2.5/weather?%s&units=imperial&appid=42895c8daba263ae387b71ff97c7eeb1";
    public WeatherFunction(WeatherCommand m){
        _main = m;
    }
    
    public String execute(ArrayList<Keyword> array)throws Exception{
        Location location;
        if(Keywords.containsType(array, "Location")){
            Keyword locationKeyword = Keywords.getFirst(array, "Location");
            location = (Location)locationKeyword.object;
        }
        else
            location = getCurrentLocation();
        if(location==null)
            throw new Exception("Unable to Determine Location");
        WeatherReportWeekly report = getWeather(location);
        if(report==null)
            throw new Exception("No Weather Data for "+location.mainText);
        ArrayList<Integer> days = getDays(array);
        if(days.isEmpty())
            days.addAll(Arrays.asList(((WeatherDetector)_main.getDetector()).days.get("today")));
        String missingDays = getMissingDays(days,report);
        return report.createReport(convertDays(days,report),getValues(array))+missingDays;
    }
    
    private WeatherReportWeekly getWeather(Location location){
        String locationCode = "";
        if(location.type.equals("locality"))
            locationCode = "q="+location.mainText;
        else if(location.type.equals("postal_code"))
            locationCode = "zip="+location.mainText;
        JsonObject forecastJson = fetchContent(String.format(forecastURL,locationCode));
        if(forecastJson==null)
            return null;
        JsonObject weatherJson = fetchContent(String.format(weatherURL,locationCode));
        if(weatherJson!=null){
            Date d = Calendar.getInstance().getTime();
            SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            weatherJson.getAsJsonObject().addProperty("dt_txt", simpleFormatter.format(d));
            forecastJson.getAsJsonObject().get("list").getAsJsonArray().add(weatherJson);
        }
        GsonBuilder builder = new GsonBuilder(); 
        builder.registerTypeAdapter(WeatherReportWeekly.class, new WeatherReportWeeklyDeserializer());
        Gson gson = builder.create(); 
        return gson.fromJson(forecastJson, WeatherReportWeekly.class);
    }  
    
    private String getMissingDays(ArrayList<Integer> days, WeatherReportWeekly report){
        String result = "";
        for(Integer day: days){
            boolean found = false;
            for(WeatherReport r: report.dailyReports)
                if(r.time.get(Calendar.DAY_OF_WEEK)==day)
                    found = true;
            if(!found)
                result+=((WeatherDetector)_main.getDetector()).weekdays.get(day).toUpperCase()+(days.indexOf(day)==days.size()-1?"":", ");
        }
        return result.isEmpty() ? "" : "[No data for "+result+"]"; 
    }
    
    private ArrayList<Integer> convertDays(ArrayList<Integer> days, WeatherReportWeekly report){
        ArrayList<Integer> d = new ArrayList();
        for(Integer day : days)
            for(WeatherReport r: report.dailyReports)
                if(r.time.get(Calendar.DAY_OF_WEEK)==day)
                    d.add(report.dailyReports.indexOf(r));
        return d;
    }
    
    private ArrayList<Integer> getDays(ArrayList<Keyword> keywords){
        ArrayList<Integer> result = new ArrayList();
        for(Keyword key : keywords)
            if(key.type.equals("Day"))
                result.addAll((ArrayList<Integer>)key.object);
        return result;
    }
    
    private ArrayList<WeatherReport.value> getValues(ArrayList<Keyword> keywords){
        ArrayList<WeatherReport.value> result = new ArrayList();
        for(Keyword key : keywords)
            if(key.type.equals("Weather"))
                result.addAll((ArrayList<WeatherReport.value>)key.object);
        return result;
    }

}
