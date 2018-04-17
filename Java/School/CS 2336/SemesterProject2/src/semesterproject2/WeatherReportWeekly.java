/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author eriko
 */
public class WeatherReportWeekly {
    String location;
    Calendar start,end;
    ArrayList<WeatherReport> hourlyReports;
    ArrayList<WeatherReport> dailyReports;
    
    public WeatherReportWeekly(String location, String start, String end, ArrayList<WeatherReport> reports){
        this.location = location;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        this.start = Calendar.getInstance();
        try{this.start.setTime(sdf.parse(start));}catch(Exception e){System.out.println(e);}
        this.end = Calendar.getInstance();
        try{this.end.setTime(sdf.parse(start));}catch(Exception e){System.out.println(e);}
        this.hourlyReports = reports;
        dailyReports = new ArrayList();
        createDailyReports();
    }
    
    private void createDailyReports(){
        for(WeatherReport report: hourlyReports)
            addReportToDay(report);
    }
    
    public void addReportToDay(WeatherReport report){
        WeatherReport day = getDay(report.time.get(Calendar.DATE));
        if(day==null){
            Date d = report.time.getTime();
            SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd '00:00:00'");
            String date = simpleFormatter.format(d);
            dailyReports.add(new WeatherReport(this.location,
                                            date,
                                            report.description,
                                            report.temperature,
                                            report.temperature,
                                            report.humidity,
                                            report.windspeed,
                                            report.winddir));
            dailyReports.sort((x,y)->x.time.get(Calendar.DATE)-y.time.get(Calendar.DATE));
        }
        else{
            day.forecasts.add(report.description);
            day.high = Math.max(day.high, report.temperature);
            day.low = Math.min(day.low, report.temperature);
            day.humidity = (day.humidity+report.humidity)/2f;
            day.windspeed = (day.windspeed+report.windspeed)/2f;
            day.winddir = (day.winddir+report.winddir)/2f;
        }
    }
    
    private WeatherReport getDay(int date){
        for(WeatherReport day: dailyReports)
            if(day.time.get(Calendar.DATE)==date)
                return day;
        return null;
    }
    
    public String getTime(Calendar day){
        Date d = day.getTime();
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("E MMMM dd");
        return simpleFormatter.format(d);
    }
    
    public String createReport(ArrayList<Integer> days,ArrayList<WeatherReport.value> weatherValues){
        String report = "The weather for "+location+"\n";
        for(Integer i:days)
            report+=" "+dailyReports.get(i).createReport(weatherValues,false)+".\n";
        return report;
    }
    
    public String toString(){
        return "Location: "+location+
                "Start: "+getTime(start)+
                "End: "+getTime(end);
                
    }
}

class WeatherReportWeeklyDeserializer implements JsonDeserializer<WeatherReportWeekly> {
    public WeatherReportWeekly deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)throws JsonParseException {
        ArrayList<WeatherReport> reports = new ArrayList();
        if(!json.getAsJsonObject().has("list"))
            return null;
        JsonArray list = json.getAsJsonObject().get("list").getAsJsonArray();
        GsonBuilder builder = new GsonBuilder(); 
        builder.registerTypeAdapter(WeatherReport.class, new WeatherReportHourlyDeserializer());
        Gson gson = builder.create(); 
        for(JsonElement j: list)
            reports.add(gson.fromJson(j, WeatherReport.class));
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = list.size()>0 && list.get(0).getAsJsonObject().has("dt_txt")?
                list.get(0).getAsJsonObject().get("dt_txt").getAsString():simpleFormatter.format(Calendar.getInstance().getTime());
        String end = list.size()>0 && list.get(list.size()-1).getAsJsonObject().has("dt_txt")?
                list.get(list.size()-1).getAsJsonObject().get("dt_txt").getAsString():simpleFormatter.format(Calendar.getInstance().getTime());
        String location = json.getAsJsonObject().get("city").getAsJsonObject().has("name")?
                json.getAsJsonObject().get("city").getAsJsonObject().get("name").getAsString():"No Location Data";
        return new WeatherReportWeekly(location,start,end,reports);
    }
}