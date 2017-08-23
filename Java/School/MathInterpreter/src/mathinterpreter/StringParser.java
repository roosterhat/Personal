
package mathinterpreter;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class StringParser {
    ArrayList<String> tokens;
    
    public StringParser(){
        tokens = new ArrayList();
    }
    
    public StringParser(ArrayList<String> tokens){
        this.tokens = tokens;
    }
    
    public ArrayList parseString(String input){
        return findComponents(input);
    }
           
    private ArrayList getRelevant(ArrayList a, String target){
        ArrayList temp = (ArrayList)a.clone();
        temp.removeIf(x->!target.contains((String)x));
        return temp;
    }
    
    private double probabilityOfMatch(String subject, String target){
        return subject.length()/target.length();
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
        while(range.end > token.value.start){
            output.add(new Entry(token.key,token.value));
            if(entries.size()-1>index)
                token = entries.get(++index);
            else
                break;
        }
        return output;  
    }
    
    private boolean containsIntersection(ArrayList<ArrayList<Entry<String,Range>>> intersections, Entry<String,Range> token){
        boolean contains = false;
        for(ArrayList<Entry<String,Range>> i: intersections)
            for(Entry<String,Range> entry: i)
                if(token.value.compare(entry.value))
                    contains = true;
        return contains;
    }
    
    private ArrayList<ArrayList<Entry<String ,Range>>> getGroups(ArrayList<Entry<String,Range>> matches){
        ArrayList<ArrayList<Entry<String,Range>>> output = new ArrayList();
        ArrayList<Entry<String,Range>> tokenGroups = (ArrayList<Entry<String,Range>>)matches.clone();
        tokenGroups.sort((x,y)->x.value.start-y.value.start);
        tokenGroups.forEach(x->
        {
            if(!containsIntersection(output, x))
                output.add(getIntersections(tokenGroups,x));
        });
        return output;        
    }
    
    private Range getTotalArea(ArrayList<Entry<String,Range>> entries){
        Range area;
        if(!entries.isEmpty()){
            area = entries.get(0).value;
            for(Entry<String,Range> entry: entries){
                Range r = entry.value;
                area.start = Math.min(area.start,r.start);
                area.end = Math.max(area.end, r.end);
            }
        }
        else
            area = new Range(0,0);
        return area;
    }
    
    private ArrayList<Entry<String,Range>> refineMatches(String input, ArrayList<Entry<String,Range>> matches){
        ArrayList<Entry<String, Range>> output = new ArrayList();
        for(ArrayList<Entry<String, Range>> group: getGroups(matches)){
            Range totalArea = getTotalArea(group);
            ArrayList<Entry<String,Range>> mostLikely = getMostLikely(group,input.substring(totalArea.start, totalArea.end),0);
            if(mostLikely.size()>0)
                output.add(mostLikely.get(0));
        }
        return output;
    }
    
    private ArrayList<Entry<String,Range>> findTokens(String input, Range range){
        ArrayList<Entry<String,Range>> matches = findAllMatches(input,range);
        return refineMatches(input,matches);
    }
    
    private ArrayList<String> findComponents(String input){
        ArrayList<String> components = new ArrayList();
        ArrayList<Entry<String,Range>> matches = findTokens(input, new Range(0,input.length()));
        Range current = new Range(0,0);
        for(Entry<String,Range> entry: matches){
            if(entry.value.start>current.end)
                components.add(input.substring(current.end, entry.value.start));
            current = entry.value;
            components.add(input.substring(current.start, current.end));
        }
        components.add(input.substring(current.end));
        components.removeIf(x->x.isEmpty());
        return components;
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
