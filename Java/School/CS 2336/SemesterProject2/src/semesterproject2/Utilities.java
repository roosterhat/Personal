/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 *
 * @author eriko
 */
public class Utilities {
    public static JsonObject fetchContent(String uri) {
	final int OK = 200;
        try{
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if(responseCode == OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);
                in.close();
                JsonElement jelement = new JsonParser().parse(response.toString());
                return jelement.getAsJsonObject();
            }
            else
                System.out.println("Error: "+connection.getResponseMessage());
        }catch(IOException e){System.out.println(e);}
	return null;
    }
    
    public static Location getCurrentLocation(){
        try{
            InetAddress ip = InetAddress.getLocalHost();
            JsonObject j = fetchContent("http://freegeoip.net/json/?q="+ip.getHostAddress());
            if(j==null)
                return null;
            return new Location(j.getAsJsonObject().get("zip_code").getAsString(),"postal_code");
        }catch(Exception e){System.out.println(e.getMessage());}
        return null;
    }
}
