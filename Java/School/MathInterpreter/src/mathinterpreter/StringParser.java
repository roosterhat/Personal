
package mathinterpreter;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author ostlinja
 */
public class StringParser {
    ArrayList<String> tokens;
    
    public StringParser(){
        tokens = new ArrayList();
    }
    
    public ArrayList parseString(String input){
        ArrayList output = new ArrayList();
        ArrayList<String> relaventTokens = getRelevant(tokens, input);
        int rangeLength = tokens.isEmpty() ? input.length() : getLongestPart(input,relaventTokens)+getLongestToken(relaventTokens);
        Range range = new Range(0,Math.min(rangeLength,input.length()));
        while(range.start<input.length())
        {
            Range part = findPart(input,range);
            output.add(input.substring(part.start,part.end));
            range.start = part.end;
            range.end = Math.min(range.start+rangeLength,input.length());
        }
        return output;
    }
        
    private String compileRegex(ArrayList<String> tokns){
        String exp = "";
        Pattern reserved = Pattern.compile("["+Pattern.quote("\\^$.,|?*+/()[]{}")+"]");
        for(String x: tokns){
            if(reserved.matcher(x).matches())
                exp+=Pattern.quote(x)+"|";
            else
                exp+=x+"|";
        }
        if(exp.isEmpty())
            return "|";
        else
            return exp.substring(0, exp.length()-1);
    }
    
    private int getLongestPart(String input,ArrayList<String> tokns){
        Pattern tokenExpression = Pattern.compile("("+compileRegex(tokns)+")");
        String[] parts = tokenExpression.split(input);
        int longest = 0;
        for(String part:parts)
            longest = Math.max(longest,part.length());
        return longest;
    }
    
    private int getLongestToken(ArrayList<String> tokns){
        int longest = 0;
        for(String token:tokns)
            longest = Math.max(longest,token.length());
        return longest;
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
    
    private ArrayList<ArrayList<Entry<String ,Range>>> getGroups(ArrayList<Entry<String,Range>> matches){
        ArrayList<ArrayList<Entry<String,Range>>> output = new ArrayList();
        ArrayList<Entry<String,Range>> tokenGroups = (ArrayList<Entry<String,Range>>)matches.clone();
        tokenGroups.sort((x,y)->x.value.start-y.value.start);
        tokenGroups.forEach(x->output.add(getIntersections(tokenGroups,x)));
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
