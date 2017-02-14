package compsciproject;

import java.util.*;
import javax.swing.*;

public class CompSciProject
{
    public static void main(String[] args)
    {
        CompSciGUI temp = new CompSciGUI();
        temp.pack();
        temp.setVisible(true);
    }
}

class Puzzle
{

    private ArrayList<boolean[]> booleanHistory = new ArrayList();
    private ArrayList<String> maskedHistory = new ArrayList();
    private ArrayList<String> history = new ArrayList();
    private ArrayList<String> logs = new ArrayList();
    private final String orgText;
    private String currentText, maskedText = "";
    private CompSciGUI m_CompSciGUI;
    private boolean[] beenSwitched;

    public Puzzle(String text, CompSciGUI CSG)  
    {
        m_CompSciGUI = CSG;
        orgText = text;
        maskedText = orgText;
        beenSwitched = new boolean[orgText.length()];   //creats a boolean Array
        for (int x = 0; x < orgText.length(); x++)  //this replaces all of the normal letters with '-' while keeping punctuation
        {
            if (!Character.toString(orgText.charAt(x)).matches("[A-Za-z]"))  
            {
                beenSwitched[x] = true;
            } 
            else
            {
                maskedText = maskedText.substring(0, x) + "-" + maskedText.substring(x + 1);
                beenSwitched[x] = false;
                
            }
        }
        currentText = orgText;                          //set the current text to the original text
        history.add(currentText);                       //adds it to the history
        maskedHistory.add(maskedText);                  //adds the masked text to the history
        booleanHistory.add(beenSwitched.clone());       //adds it to the history as a clone(a clone is a "deep copy" which means instead of copying the pointer for the array it copys all the data of the array)
    }

    public void switchLetters(String letter1, String letter2, boolean o)   //switches in the puzzle that are given to it
    {
        boolean option = o;
        if (!letter1.matches("[A-Za-z]")||!letter2.matches("[A-Za-z]"))//checks to see if the two characters are not invalid
        {
            JOptionPane.showMessageDialog(null, "You cannot use that character", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        else if(letter1.equals(letter2))
        {
            JOptionPane.showMessageDialog(null, "You cannot use the same letters", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            String newString = currentText;
            for (int x = 0; x < newString.length(); x++)
            {
                if (Character.toString(newString.charAt(x)).equals(letter1.toUpperCase()))//checks to see if letter1 equals the current letter in the charArray
                {
                    if (beenSwitched[x]==option)//checks to see if the letter has been edited before
                    {
                        continue;
                    }
                    if(option)
                    {
                        maskedText = maskedText.substring(0, x) + letter2.toUpperCase() + maskedText.substring(x + 1);  //replaces the '-' with the new letter
                    }
                    else
                    {
                        maskedText = maskedText.substring(0, x) + "-" + maskedText.substring(x + 1);
                    }
                    newString = newString.substring(0, x) + letter2.toUpperCase() + newString.substring(x + 1); //replace the letter with the new letter
                    beenSwitched[x] = option;
                }
            }
            if(option)
            {
                logs.add(letter1.toUpperCase() + " to " + letter2.toUpperCase());   //adds the 2 new letters to the change logs
            }
            booleanHistory.add(beenSwitched.clone());   //adds the boolean Array to the history
            history.add(newString); //adds the edited string to the history
            maskedHistory.add(maskedText);  //adds the masked text to the history
            setCurrent(newString);  //set the current puzzle
            boolean contains = false;
            for(boolean b: beenSwitched)
            {
                if(!b)
                {
                    contains = true;
                }
            }
            if(!contains)
            {
                int result = JOptionPane.showConfirmDialog(null, "You have changed all of the letters\nAre you done?", "",JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION)
                {
                    m_CompSciGUI.setVisible(false);
                    System.exit(0);
                }
            }
        }
    }
   
    public String getOriginal()
    {
        return orgText;
    }

    public String getCurrent()
    {
        return currentText;
    }

    public String getMaskedCurrent()
    {
        return maskedText;
    }
    public ArrayList getLogsArray()
    {
        return logs;
    }
    
    public Object[] getHistory()
    {
        return history.toArray();
    }
    public void removeLog(int index)
    {
        logs.remove(index);
    }
    public void revertMaskedText(int index) //reverts the masked text back to the history index of "index"
    {
        maskedText = maskedHistory.get(index);
        maskedHistory.add(maskedText);
        m_CompSciGUI.setCurrentText(currentText, maskedText);   //calls CompSciGUI and tells it to set the CurrentTextFeild to current text
    }

    public void revertCurrent(int index)    //reverts the current text back to the history index of "index"
    {
        currentText = history.get(index);
        history.add(currentText);
        boolean[] clone = booleanHistory.get(index);
        beenSwitched = clone.clone();
        booleanHistory.add(clone.clone());
        m_CompSciGUI.setCurrentText(currentText, maskedText);
    }

    public void setCurrent(String current)
    {
        currentText = current;
        m_CompSciGUI.setCurrentText(currentText, maskedText);
    }
}

//             vv look pretty inheritance
class TextNote extends JTextArea//<--- see, text area 
{

    public TextNote(String text) //this is a modified text field that allows me to put multi lined text in (cause JLable is stupid)
    {
        super(text);
        setBackground(null);
        setEditable(false); //disables editing of textfeild
        setBorder(null);
        setLineWrap(true);  //<---- word wrap
        setWrapStyleWord(true);
        setFocusable(false);
    }
}