/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

/**
 *
 * @author eriko
 */
public class WeatherCommand extends Command{
    public WeatherCommand(){
        super("Weather");
        addKeyword("weather");
        addKeyword("temperature");
        addKeyword("humidity");
        addKeyword("wind");
        addKeyword("forecast");
        setDetector(new WeatherDetector(title, keywords));
        setFunction(new WeatherFunction(this));
        relavent.add("Location");
        relavent.add("Day");
        usage = "Use any combination of keywords to specify type of response, use city or zip code to specify location, use any number of days of the week i.e. 'monday' or an identifier such as 'today', 'tomorrow', 'week', or 'weekend' to specify forecast period";
    }
}
