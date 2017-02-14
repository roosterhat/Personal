/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package realfinal;

/**
 *
 * @author ostlinja
 */

//a class that contains a list of all upper and lower case letters
public class Alphabet {
    
    private String letters;
    
    public Alphabet()
    {
        letters = generateLetters();
    }
    
    //creates a string of all upper and lower case letters 
    private String generateLetters()
    {
        String temp = "";
        for (char ch = 'a'; ch <= 'z'; ++ch)
            temp+=Character.toString(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            temp+=Character.toString(ch);
        return temp;
    }
    
    //gets a character from all the characters at index 'index'
    public Character getLetter(int index)
    {
        return letters.charAt(index);
    }
    
    //gets the length of the string containing all the characters
    public int getSize()
    {
        return letters.length();
    }
}
