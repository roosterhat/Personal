/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

/**
 *
 * @author ostlinja
 */
public class Range {
    int start;
    int end;
    public Range(){
        this(0,0);
    }
    
    public Range(int s,int e){
        start = s;
        end = e;
    }
    
    public boolean contains(int point){
        return contains((double)point);
    }
    
    public boolean contains(double point){
        return point>=start && point<=end;
    }
    
    public int length(){
        return end-start;
    }
    
    public boolean compare(Range r){
        return r.start==start && r.end==r.end;
    }
    
    public String toString(){
        return start+","+end;
    }
}
