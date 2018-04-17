/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eriko
 */
public class WeatherDetector extends Detector{
    ArrayList<String> weekdays;
    Map<String,Integer[]> days;
    ArrayList<String> keywords;
    String title;
    public WeatherDetector(String title, ArrayList<String> keywords){
        this.title = title;
        this.keywords = keywords;
        weekdays = new ArrayList(Arrays.asList(new String[]{"","sunday","monday","tuesday","wednesday","thursday","friday","saturday"}));
        days = new HashMap();
        Calendar now = Calendar.getInstance();
        days.put("today",new Integer[]{now.get(Calendar.DAY_OF_WEEK)});
        days.put("tomorrow",new Integer[]{(now.get(Calendar.DAY_OF_WEEK)%7+1)});
        Integer[] t = new Integer[5];
        for(int i=0;i<5;i++)
            t[i] = (now.get(Calendar.DAY_OF_WEEK)+i-1)%7+1;
        days.put("week",t);
        days.put("weekend",new Integer[]{7,1});
    }
    
    public boolean containsKeywords(ArrayList<String> array) {
        return isDay(array) || isWeekday(array) || isKeyword(array);
    }

    public Output getKeyword(ArrayList<String> array) {
        Keyword keyword;
        String word = array.isEmpty() ? "" : array.get(0);
        if(isDay(array)){
            keyword = new Keyword(array.get(0),"Day");
            keyword.object = new ArrayList(Arrays.asList(days.get(word)));
        }
        else if(isWeekday(array)){
            keyword = new Keyword(word,"Day");
            keyword.object = new ArrayList(Arrays.asList(new Integer[]{weekdays.indexOf(word)}));
        }
        else if(isKeyword(array)){
            keyword = new Keyword(word,title);
            keyword.object = getValues(word);
        }
        else
            keyword = null;
        return new Output(keyword,new Range(0,1));
    }
    
    private boolean isDay(ArrayList<String> array){
        if(!array.isEmpty())
            for(String day: days.keySet())
                if(day.equalsIgnoreCase(array.get(0)))
                    return true;
        return false;
    }
    
    private boolean isWeekday(ArrayList<String> array){
        if(!array.isEmpty())
            for(String day: weekdays)
                if(day.equalsIgnoreCase(array.get(0)))
                    return true;
        return false;
    }
    
    private boolean isKeyword(ArrayList<String> array){
        if(!array.isEmpty())
            for(String keyword: keywords)
                if(keyword.equalsIgnoreCase(array.get(0)))
                    return true;
        return false;
    }
    
    private ArrayList<WeatherReport.value> getValues(String word){
        Map<String,WeatherReport.value[]> values = new HashMap();
        values.put("weather", new WeatherReport.value[]{WeatherReport.value.DESCRIPTION,WeatherReport.value.TEMPERATURE,WeatherReport.value.HUMIDITY,WeatherReport.value.WIND});
        values.put("temperature", new WeatherReport.value[]{WeatherReport.value.TEMPERATURE});
        values.put("humidity", new WeatherReport.value[]{WeatherReport.value.HUMIDITY});
        values.put("wind", new WeatherReport.value[]{WeatherReport.value.WIND});
        values.put("forecast", new WeatherReport.value[]{WeatherReport.value.DESCRIPTION});
        return new ArrayList(Arrays.asList(values.get(word)));
    }
    
}
