/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchalgorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public abstract class SearchMethod {
    int success = 0;
    int failures = 0;
    int probes = 0;
    ArrayList<Integer> succProbs = new ArrayList();
    ArrayList<Integer> failProbs = new ArrayList();
    final int size = 150;
    final int top = 9999;
    final int search = 3;
    ArrayList<Integer> values = new ArrayList();
    ArrayList<Integer> searchValues = new ArrayList();
    public int search(int value){return -1;};
    public int getSuccesses(){return success;}
    public int getFailures(){return failures;}
    public void resetStats(){success = 0;failures = 0;}
    public String getName(){return "";}
    
    public boolean isOrdered(ArrayList<Integer> l)
    {
        if(!l.isEmpty())
        {
            int last = l.get(0);
            for(int i:l)
            {
                if(i<last)
                    return false;
                last = i;
            }
        }
        return true;
    }
    
    public ArrayList<Integer> readFile(String path,int lim)
    {
        ArrayList<Integer> res = new ArrayList();
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader reader = Files.newBufferedReader(new File(path).toPath(), charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Pattern p = Pattern.compile("[^\\s,]+");
                Matcher m = p.matcher(line);  
                int count = 0;
                while(m.find() && count<lim)
                {
                    String s = line.substring(m.start(), m.end());  
                    int i = Integer.parseInt(s);
                    if(i<top)
                        res.add(i);
                    count++;
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return res;
    }
    
    public void runSearch()
    {
        for(int i=0;i<search;i++)
            if(i<searchValues.size())
            {
                search(searchValues.get(i));
                System.out.println(getName()+": "+probes);
            }
    }
    
    public static ArrayList<Integer> sort(ArrayList<Integer> l)
    {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for(Integer i:l)
            temp.add(findPos(i,temp), i);
        return temp;
    }
    
    public static int findPos(int v,ArrayList<Integer> l)
    {
        if(!l.isEmpty())
        {
            int low = 0;
            int high = l.size();
            while(high>low+1)
            {
                int mid = low + (high-low)/2;
                if(l.get(mid)==v)
                    return mid;
                else
                {
                    if(l.get(mid)>v)
                        high = mid;
                    else if(l.get(mid)<v)
                        low = mid;                    
                }
            }
            if(l.get(high-1)>v)
                return high-1;
            else
                return high;
        }
        return 0;
    }
    
}
