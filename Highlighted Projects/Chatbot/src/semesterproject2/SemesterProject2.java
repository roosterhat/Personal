/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

/**
 *
 * @author eriko
 */
public class SemesterProject2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)throws Exception {
        ChatBot bot = new ChatBot();
        bot.setVerbose(true);
        bot.connect("irc.us.freenode.net");
        bot.joinChannel("#pircbot");
}
	

    
    
}


