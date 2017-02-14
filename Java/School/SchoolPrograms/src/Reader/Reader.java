package Reader;


import java.util.ArrayList;
import schoolprograms.SchoolProgramsCommon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Erik Ostlind
 */
public class Reader implements SchoolProgramsCommon
{
    String name = "Reader";
    String path;
    int currentWord;
    int waitTime;
    boolean running;
    ArrayList words;
    ReaderFrame GUI;
    
    public void run()
    {
        GUI = new ReaderFrame(this);
        waitTime = 500;
        running = false;
        setPath("path");
        System.out.println("words size: "+words.size());
        GUI.pack();
        GUI.setVisible(true);
    }
    public String getName()
    {
        return name;
    }
    public void setPath(String p)
    {
        path = p;
        getText();
    }
    public void getText()
    {
        words = new ArrayList();
        for(int i=0;i<100;i++)
        {
            words.add(i);
        }
        currentWord = 0;
    }
    public boolean switchRunning()
    {
        running = !running;
        return running;
    }
    public void setWaitTime(int t)
    {
        waitTime = t;
    }
    public void updatePercentage()
    {
        GUI.setPercentage(currentWord/words.size()*100);
    }
    public void displayWords()
    {

        while (running)
        {
            GUI.setWord(words.get(currentWord).toString());
            currentWord++;
            updatePercentage();
            try
            {
                Thread.sleep(waitTime);
            }
            catch(InterruptedException e)
            {
                System.out.println("Reader:displayWords:sleep[] "+e.getMessage());
            }
        }


    }
}
