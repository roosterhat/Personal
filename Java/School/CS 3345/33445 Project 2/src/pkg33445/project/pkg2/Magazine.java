/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg33445.project.pkg2;

/**
 *
 * @author eriko
 */
public class Magazine implements IDedObject{
    private int magazineID;
    private String magazineName, publisherName;
    
    public Magazine(int id, String magName, String pubName){
        magazineID = id;
        magazineName = magName;
        publisherName = pubName;
    }
    
    public String getName(){
        return magazineName;
    }
    
    public String getPublisher(){
        return publisherName;
    }
    
    public int getID() {
        return magazineID;
    }

    public void printID() {
        System.out.println(this);
    }
    
    public String toString(){
        return "Magazine: "+magazineName+"\nPublisher: "+publisherName+"\nMagazine ID: "+magazineID;
    }
    
}
