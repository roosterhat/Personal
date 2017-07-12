
package mathinterpreter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public class StringParser {
    MathInterpreter _main;
    ArrayList<String> tokens;
    
    public StringParser(MathInterpreter m){
        _main = m;
        tokens = new ArrayList();
    }
    
    public ArrayList parseString(String input){
        ArrayList output = new ArrayList();
        Range range = new Range(0,input.length());
        while(range.start<input.length())
        {
            Range part = findPart(input,range);
            output.add(input.substring(part.start,part.end));
            range.start = part.end;
        }
        return output;
    }
    
    private ArrayList getRelevant(ArrayList a, String target){
        ArrayList temp = (ArrayList)a.clone();
        temp.removeIf(x->!target.contains((String)x));
        return temp;
    }
    
    private double probabilityOfMatch(String subject, String target){
        double prob = 0;
        double charval = 1.0/target.length();
        boolean consecutive = true;
        Pattern pattern = Pattern.compile("["+Pattern.quote(subject)+"]");
        for(char c:target.toCharArray()){
            if(pattern.matcher(String.valueOf(c)).matches()){
                if(consecutive)
                    prob+=charval;
                else{
                    prob+=charval/2;
                    consecutive = true;
                }
            }
            else
                consecutive = false;
        }
        return prob;
    } 
       
    private ArrayList<Entry<String,Range>> getMostLikely(ArrayList<Entry<String,Range>> possible, String target, double margin){
        ArrayList<Double> res = new ArrayList();
        double max = 0;
        for(Entry<String,Range> entry: possible){
            double val = probabilityOfMatch(entry.key,target);
            max = Math.max(max, val);
            res.add(val);
        }
        final double most = max;
        possible.removeIf(x-> res.get(possible.indexOf(x)) < most-margin);
        return possible;
    }
    
    private String compileRegex(ArrayList a){
        String exp = "";
        Pattern reserved = Pattern.compile("["+Pattern.quote("\\^$.,|?*+/()[]{}")+"]");
        for(String x: (ArrayList<String>)a){
            if(reserved.matcher(x).matches())
                exp+="|"+Pattern.quote(x);
            else
                exp+="|"+x;
        }
        return exp;
    }
    
    private ArrayList<Entry<String,Range>> findAllMatches(String input,Range range){
        ArrayList<Entry<String,Range>> output = new ArrayList();
        ArrayList<String> relaventTokens = getRelevant(tokens,input);
        for(String token:relaventTokens){
            int pos = range.start;
            int length = token.length();
            while(input.substring(pos, range.end).contains(token)){
                int index = input.indexOf(token,pos);
                output.add(new Entry(token, new Range(index,index + length)));
                pos = index + length;
            }
        }
        return output;
    }
    
    private ArrayList<Entry<String,Range>> getIntersections(ArrayList<Entry<String,Range>> entries, Entry<String,Range> token){
        ArrayList<Entry<String,Range>> output = new ArrayList();
        int index = entries.indexOf(token);
        Range range = token.value;
        Entry<String,Range> entry = token;
        while(range.end > entry.value.start){
            output.add(new Entry(entry.key,entry.value));
            if(entries.size()-1>index)
                entry = entries.get(++index);
            else
                break;
        }
        return output;  
    }
    
    private ArrayList<ArrayList<Entry<String,Range>>> getGroups(ArrayList<Entry<String,Range>> matches){
        ArrayList<ArrayList<Entry<String,Range>>>output = new ArrayList();
        ArrayList<Entry<String,Range>> tokens = (ArrayList<Entry<String,Range>>)matches.clone();
        tokens.sort((x,y)->x.value.start-y.value.start);
        for(Entry<String,Range> entry: tokens)
            output.add(getIntersections(tokens,entry));
        return output;        
    }
    
    private Range getTotalArea(ArrayList<Entry<String,Range>> entries){
        Range area = new Range(0,0);
        for(Entry<String,Range> entry: entries){
            Range r = entry.value;
            area.start = Math.min(area.start,r.start);
            area.end = Math.max(area.end, r.end);
        }
        return area;
    }
    
    private ArrayList<String> getKeyset(ArrayList<Entry<String,Range>> entries){
        ArrayList<String> keyset = new ArrayList();
        for(Entry<String,Range> entry: entries)
            keyset.add(entry.key);
        return keyset;
    }
    
    private ArrayList<Entry<String,Range>> refineMatches(String input, ArrayList<Entry<String,Range>> matches){
        ArrayList<Entry<String, Range>> output = new ArrayList();
        ArrayList<ArrayList<Entry<String,Range>>> groups = getGroups(matches);
        for(ArrayList<Entry<String, Range>> group:groups){
            Range totalArea = getTotalArea(group);
            ArrayList<Entry<String,Range>> mostLikely = getMostLikely(group,input.substring(totalArea.start, totalArea.end),0);
            if(mostLikely.size()>0)
                output.add(mostLikely.get(0));
        }
        return output;
    }
    
    private Range getClosest(ArrayList<Entry<String,Range>> matches, Range range){
        Entry<String,Range> closest = null;
        for(Entry<String,Range> entry: matches){
            if(closest==null)
                closest = entry;
            else if(closest.value.start>entry.value.start)
                closest = entry;
        }
        if(closest==null)
            return range;
        return closest.value;
    }
    
    private Range findPart(String input, Range range){
        ArrayList<Entry<String,Range>> matches = findAllMatches(input,range);
        ArrayList<Entry<String,Range>> refinedMatches = refineMatches(input,matches);
        Range part = getClosest(refinedMatches,range);
        if(part.start!=range.start)
            return new Range(range.start,part.start);
        return part;
    }
}

class Entry<K, V>{
    K key;
    V value;
    public Entry(K key, V value){
        this.key = key;
        this.value = value;
    }
}
