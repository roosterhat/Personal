/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sortingalgorithms;

import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author ostlinja
 */
public class HeapSelect extends SortingMethod{
    ArrayList<Integer> heap;

    public HeapSelect(ArrayList numbers)
    {
        unsorted = (ArrayList)numbers.clone();
        heap = (ArrayList)numbers.clone();
    }
    public String getName(){return "Heap Select";}
    
    public void sort()
    {
        buildHeap();
        for(int i=heap.size()-1;i>=0;i--){
            heap = swap(0,i,heap);
            compareSwap(0,i);
        }
        sorted = reverse(heap);
    }
    
    public int getLeft(int i){
        return i*2+1;
    }
    
    public int getRight(int i){
        return i*2+2;
    }
    
    public boolean hasLeft(int i){
        return i>=0 && getLeft(i)<heap.size();
    }
    
    public boolean hasRight(int i){
        return i>=0 && getRight(i)<heap.size();
    }
    
    public void buildHeap(){
        int start = ((heap.size()-1)-1)/2;
        for(int pos = start;pos>=0;pos--){
            compareSwap(pos);
        }
    }
    
    public void compareSwap(int index){
        compareSwap(index,heap.size());
    }
    
    public void compareSwap(int index,int max){
        if(hasLeft(index) || hasRight(index)){
            comparisons++;
            int child = getMinChildIndex(index);
            if(child<max){
                heap = swap(index,child,heap);
                if(child!=index)
                    compareSwap(child,max);
            }
        }
    }
    
    public int getMinChildIndex(int index){
        int min;
        if(hasLeft(index) && hasRight(index)){
            if(heap.get(getLeft(index))<heap.get(getRight(index)))
                min = getLeft(index);
            else
                min = getRight(index);
        }
        else if(hasLeft(index))
            min = getLeft(index);
        else if(hasRight(index))
            min = getRight(index);
        else
            min = index;
        
        if(heap.get(min)<heap.get(index))
            return min;
        else
            return index;
    }

    public ArrayList swap(int i1, int i2, ArrayList h){
        int temp = (int)h.get(i1);
        h.set(i1, (int)h.get(i2));
        h.set(i2,temp);
        return h;
    }
    
    public ArrayList reverse(ArrayList a){
        ArrayList temp = new ArrayList();
        for(Object o:a)
            temp.add(0, o);
        return temp;
    }
    
}


