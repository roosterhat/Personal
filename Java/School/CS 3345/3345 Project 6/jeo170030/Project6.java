/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author eriko
 */
public class Project6 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*createDataFile("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\3345\\3345 Project 6\\src\\pkg3345\\project\\pkg6\\DataFile3");
        createQueryFile("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\3345\\3345 Project 6\\src\\pkg3345\\project\\pkg6\\QueryFile3");
        args = new String[]{"C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\3345\\3345 Project 6\\src\\pkg3345\\project\\pkg6\\DataFile3",
                            "C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\3345\\3345 Project 6\\src\\pkg3345\\project\\pkg6\\QueryFile3",
                            "C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\3345\\3345 Project 6\\src\\pkg3345\\project\\pkg6\\output"};*/
        if(args.length>=3){
            HashMap<String, Graph> graphs = new HashMap();
            graphs.put("T", new Graph());
            graphs.put("C", new Graph());
            String s = "";
            try{
                BufferedReader data = new BufferedReader(new FileReader(new File(args[0]))); 
                while ((s = data.readLine()) != null && !s.isEmpty()){
                    String[] flightData = s.split("\\|");
                    graphs.get("C").connect(flightData[0], flightData[1], Integer.valueOf(flightData[2]));
                    graphs.get("T").connect(flightData[0], flightData[1], Integer.valueOf(flightData[3]));
                }
                data.close();
            }catch(FileNotFoundException e){
                System.out.println(e.getMessage());
            }
            catch(Exception e){
                System.out.println("Failed to read line '"+s+"', Error: "+e.toString());
                return;
            }
            try{
                BufferedReader query = new BufferedReader(new FileReader(new File(args[1]))); 
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                while ((s = query.readLine()) != null && !s.isEmpty()){
                    String[] specs = s.split("\\|");
                    System.out.println(s);
                    ArrayList<Edge> route = graphs.get(specs[2]).findShortestRoute(specs[0], specs[1]);
                    if(route==null){
                        writer.write("NO FLIGHT AVAILABLE FOR THE REQUEST\n");
                        continue;
                    }
                    int time = 0, cost = 0;
                    String res = (route.size()+1)+"|";
                    for(Edge e : route){
                        time += graphs.get("T").getEdge(e.source.name, e.destination.name).weight;
                        cost += graphs.get("C").getEdge(e.source.name, e.destination.name).weight;
                        res += e.source.name+"|";
                    }
                    res += route.get(route.size()-1).destination.name+"|"+cost+"|"+time+"\n";
                    writer.write(res);
                }
                query.close();
                writer.close();
            }catch(FileNotFoundException e){
                System.out.println(e.getMessage());
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
        else
            System.out.println("Invalid Syntax, Expected 3 arguments, found "+args.length);
    }
    
    public static void createDataFile(String file){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String[] nodes = new String[26];
            for(char c = 'A'; c<='Z'; c++)
                nodes[c-65] = c+"";
            for(String node : nodes){
                int num = (int)(Math.random()*12);
                ArrayList<String> adjacent = new ArrayList(num);
                String s;
                for(int i = 0; i<num; i++)
                    while(!adjacent.contains(s = nodes[(int)(Math.random()*26)]) && !node.equals(s)){
                        writer.write(node+"|"+s+"|"+((int)(Math.random()*100))+"|"+((int)(Math.random()*100))+"\r\n");
                        adjacent.add(s);
                    }
            }
            writer.close();
        }catch(Exception e){System.out.println(e);}
    }
    
    public static void createQueryFile(String file){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String[] nodes = new String[26];
            for(char c = 'A'; c<='Z'; c++)
                nodes[c-65] = c+"";
            String[] type = {"T","C"};
            for(int i = 0; i<20; i++){
                writer.write(nodes[(int)(Math.random()*26)]+"|"+nodes[(int)(Math.random()*26)]+"|"+type[(int)(Math.random()*100)%2]+"\r\n");
            }
            writer.close();
        }catch(Exception e){System.out.println(e);}
    }
}
