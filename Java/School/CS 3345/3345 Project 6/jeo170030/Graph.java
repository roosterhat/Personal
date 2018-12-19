/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3345.project.pkg6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import javafx.util.Pair;

/**
 *
 * @author eriko
 */
public class Graph {
    ArrayList<Vertex> vertices;
    ArrayList<Edge> edges;
    
    public Graph(){
        vertices = new ArrayList();
        edges = new ArrayList();
    }
    
    public boolean addVertex(String v){
        return addVertex(new Vertex(v));
    }
    
    public boolean addVertex(Vertex v){
        if(!vertices.contains(v)){
            vertices.add(v);
            for(Edge e: v.out)
                if(!edges.contains(e))
                    edges.add(e);
            return true;
        }
        return false;
    }
    
    public void connect(Vertex source, Vertex destination, int weight){
        if(!vertices.contains(source))
            addVertex(source);
        if(!vertices.contains(destination))
            addVertex(destination);
        source.connect(destination, weight);
    }
    
    public void connect(String s, String d, int weight){
        Vertex source = getVertex(s);
        Vertex destination = getVertex(d);
        if(source==null){
            source = new Vertex(s);
            addVertex(source);
        }
        if(destination==null){
            destination = new Vertex(d);
            addVertex(destination);
        }
        source.connect(destination, weight);
    }
    
    public Vertex getVertex(String name){
        for(Vertex v : vertices)
            if(v.name.equals(name))
                return v;
        return null;
    }
    
    public Edge getEdge(String source, String destination){
        Vertex s = getVertex(source);
        Vertex d = getVertex(destination);
        if(s==null | d==null)
            return null;
        for(Edge e : s.out)
            if(e.destination.equals(d))
                return e;
        return null;
    }
    
    public ArrayList<Edge> findShortestRoute(Vertex source, Vertex destination){
        if(source==null || destination==null)
            return null;
        PriorityQueue<Pair<Vertex,Integer>> queue = new PriorityQueue(vertices.size(),(x,y)->((Pair<Vertex,Integer>)x).getValue()-((Pair<Vertex,Integer>)y).getValue());
        HashMap<Vertex,Pair<Integer, Edge>> dist = new HashMap();
        for(Vertex v : vertices)
            dist.put(v, new Pair(Integer.MAX_VALUE,null));
        dist.replace(source, new Pair(0,null));  
        queue.add(new Pair(source,0));
        while(!queue.isEmpty()){
            Vertex v = queue.poll().getKey();
            for(Edge e : v.out)
                if(dist.get(v).getKey()+e.weight < dist.get(e.destination).getKey()){
                    dist.replace(e.destination,new Pair(dist.get(v).getKey()+e.weight,e));
                    queue.add(new Pair(e.destination,dist.get(e.destination).getKey()));
                }
        }
        ArrayList<Edge> path = new ArrayList();
        Edge edge = dist.get(destination).getValue();
        while(edge!=null){
            path.add(edge);
            edge = dist.get(edge.source).getValue();
        }
        Collections.reverse(path);
        if(path.isEmpty() || path.get(0).source!=source)
            return null;
        return path;
    }
    
    public ArrayList<Edge> findShortestRoute(String source, String destination){
        return findShortestRoute(getVertex(source), getVertex(destination));
    }
    
    public String toString(){
        return vertices.toString();
    }
}

class Vertex{
    String name;
    ArrayList<Edge> out;
    
    public Vertex(String name){
        this.name = name;
        out = new ArrayList();
    }
    
    public void connect(Vertex destination, int weight){
        out.add(new Edge(this,destination,weight));
    }
    
    public boolean isAdjacent(Vertex v){
        for(Edge e: out)
            if(e.destination.equals(v))
                return true;
        return false;
    }
    
    public boolean isAdjacent(String v){
        for(Edge e: out)
            if(e.destination.name.equals(v))
                return true;
        return false;
    }
    
    public boolean equals(Vertex v){
        return name.equals(v.name);
    }
    
    public String toString(){
        return name+": "+out.toString();
    }
}

class Edge{
    Vertex source, destination;
    int weight;
    
    public Edge(Vertex source, Vertex destination, int weight){
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
    
    public boolean equals(Edge e){
        return source.name.equals(e.source.name) &&
                destination.name.equals(e.destination.name) &&
                weight==e.weight;
    }
    
    public String toString(){
        return source.name+" -["+weight+"]- "+destination.name;
    }
}


