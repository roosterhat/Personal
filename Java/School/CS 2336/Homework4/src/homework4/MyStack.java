/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework4;

/**
 *
 * @author eriko
 */
import java.util.ArrayList;


public class MyStack extends ArrayList{
    public Object peek(){
        return get(size()-1);
    }
    
    public Object pop(){
        Object o = peek();
        remove(o);
        return o;
    }
    
    public void push(Object o){
        add(o);
    }
    
    public String toString(){
        return "MyStack: "+super.toString();
    }
}


class MyStackRef {
  private ArrayList<Object> list = new ArrayList<>();

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public int getSize() {
    return list.size();
  }

  public Object peek() {
    return list.get(getSize() - 1);
  }

  public Object pop() {
    Object o = list.get(getSize() - 1);
    list.remove(getSize() - 1);
    return o;
  }

  public void push(Object o) {
    list.add(o);
  }

  @Override /** Override the toString in the Object class */
  public String toString() {
    return "stack: " + list.toString();
  }
}
