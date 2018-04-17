/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author eriko
 */
public class TimeCommand extends Command{
    public TimeCommand(){
        super("Time");
        addKeyword("time");
        addKeyword("date");
        addKeyword("year");
        setFunction(x->{
            String result = "It is ";
            for(Keyword k:x){
                String keyword = k.keyword;
                Calendar date = Calendar.getInstance();
                if(keyword.equalsIgnoreCase("time"))
                    result += String.format("%02d:%02d:%02d",date.get(Calendar.HOUR_OF_DAY)
                                                                        ,date.get(Calendar.MINUTE)
                                                                        ,date.get(Calendar.SECOND));
                else if(keyword.equalsIgnoreCase("date"))
                    result += String.format("%4d/%02d/%02d",date.get(Calendar.YEAR)
                                                                       ,date.get(Calendar.MONTH)+1
                                                                       ,date.get(Calendar.DAY_OF_MONTH));
                else if(keyword.equalsIgnoreCase("year"))
                    result += String.format("%4d",date.get(Calendar.YEAR));
                else
                    throw new Exception("Unknown Keyword '"+keyword+"'");
                result += " ";
            }
            return result;
        });
        usage = "Use any combination of keywords to specify type of response";
    }
}
