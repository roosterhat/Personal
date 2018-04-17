/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework5;
import java.util.Date;

/**
 *
 * @author eriko
 */
public abstract class GeometricObject implements Comparable<GeometricObject>{
  protected String color = "white";
  protected boolean filled;
  protected Date dateCreated;

  /** Construct a default geometric object */
  protected GeometricObject() {
    dateCreated = new Date();
  }

  /** Construct a geometric object with color and filled value */
  protected GeometricObject(String color, boolean filled) {
    dateCreated = new Date();
    this.color = color;
    this.filled = filled;
  }
  
  //returns the larger of the two objects based on their area
  public static GeometricObject max(GeometricObject o1, GeometricObject o2){
      return o1.compareTo(o2)>0 ? o1 : o2;
  }

  /** Return color */
  public String getColor() {
    return color;
  }

  /** Set a new color */
  public void setColor(String color) {
    this.color = color;
  }

  /** Return filled. Since filled is boolean,
   *  the get method is named isFilled */
  public boolean isFilled() {
    return filled;
  }

  /** Set a new filled */
  public void setFilled(boolean filled) {
    this.filled = filled;
  }

  /** Get dateCreated */
  public Date getDateCreated() {
    return dateCreated;
  }
  
  public int compareTo(GeometricObject o){
      return (int)(getArea() - o.getArea());
  }

  public String toString() {
    return "created on " + dateCreated + " color: " + color +" filled: " + filled+" Area: "+getArea()+" Perimeter: "+getPerimeter();
  }
  
  /** Abstract method getArea */
  public abstract double getArea();

  /** Abstract method getPerimeter */
  public abstract double getPerimeter();
}