/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logicmachine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
/**
 *
 * @author erik
 */
public class LogicMachine {
    
    String[] binaryOut;
    String[] operators = {"&","v","~"}; 
    String[] booleanStates = {"true","false","undefined"};
    ArrayList<String> variables = new ArrayList();
    ArrayList<Boolean> values = new ArrayList();
    public static void main(String[] args) {
        new LogicMachine().run();
    }
    
    public void run()
    {
        
        Scanner sc = new Scanner( System.in );
        String argument;
        System.out.print("Characters"+Arrays.toString(operators)+"\n:>");
        argument = sc.next();
        parseArgument(argument);
        boolean continuem = true;
        while(continuem)
        {
            System.out.println("specific/table output");
            String choice = sc.next();
            if(choice.equals("specific"))
            {
                System.out.println("Please state the value of each variable (true, false, undefined)");
                for(String a:variables)
                {
                    boolean running = true;
                    while(running)
                    {
                        String temp;
                        System.out.print(a+": ");
                        temp = sc.next();
                        if(Arrays.asList(booleanStates).contains(temp))
                        {
                            values.add(Boolean.valueOf(temp));
                            running = false;
                        }
                        else
                            System.out.println("invalid state");
                    }
                }
                System.out.println(examinStatement(argument));
                continuem = false;
            }
            else if(choice.equals("table"))
            {
                binaryOut = new String[(int)Math.pow(2,variables.size())];
                binaryOut = getBinaryOutput(variables.size());
                String title = " ";
                for(String a:variables)
                {
                    title+=(a+" ");
                    values.add(false);
                }
                title+=(" "+argument);
                System.out.println(title);
                for(int i=0;i<title.length();i++)
                    System.out.print("-");
                System.out.println("-");
                for(int i=0;i<binaryOut.length;i++)
                {
                    String temp = " ";
                    for(int a=0;a<variables.size();a++)
                    {
                        if(binaryOut[i].charAt(a)=='0')
                        {
                            temp+="T ";
                            values.set(a, true);
                        }
                        if(binaryOut[i].charAt(a)=='1')
                        {
                            temp+="O ";
                            values.set(a, false);
                        }
                    }
                    temp+=(" "+Boolean.toString(examinStatement(argument)));
                    System.out.println(temp);
                }
                continuem = false;
            }
            else
            {
                System.out.println("Incorrect input");
            }
        }
    }
    
    public String[] getBinaryOutput(int val)
    {
        String[] temp = new String[(int)Math.pow(2,val)];
        String out = "";
        for (int i=0; i<val; ++i) {
            out+="0";   
        }
        for (int i = 0; i < Math.pow(2,val); i++) {
            if (val-Integer.toBinaryString(i).length() > 0) { 
                temp[i] = out.substring(1,val-Integer.toBinaryString(i).length()+1) +Integer.toBinaryString(i);
            } else {
                temp[i] = Integer.toBinaryString(i);
            }
        }
        return temp;
    }
    
    public void parseArgument(String a)
    {
        for(int i=0;i<a.length();i++)
        {
            char c = a.charAt(i);
            if(Character.isAlphabetic(c))
                if(!(c=='v'))
                    if(!variables.contains(Character.toString(c)))
                        variables.add(Character.toString(c));
        }
    }
    
    public boolean findNextStatement(String s)
    {
        int breakPoint = findFirstOp(s);
        if(s.charAt(0)=='(')
        {
            int close = findClosingParan(s);
            if(close>-1)
                if(close<s.length())
                    findNextStatement(translateBool(findNextStatement(s.substring(1,close)))+s.substring(close+1));
                else
                    return findNextStatement(s.substring(1,close));
        }
        else if(breakPoint>-1)
        {
            if(breakPoint==0&&s.charAt(breakPoint)=='~')
            {
                return !findNextStatement(s.substring(1));
            }
            else
            {
                return examinStatement(translateBool(findNextStatement(s.substring(0,breakPoint)))+
                                       s.substring(breakPoint,breakPoint+1)+
                                       translateBool(findNextStatement(s.substring(breakPoint+1))));
            }
        }
        else
        {
            return examinStatement(s);
        }
        return false;
        
    }
    
    public boolean examinStatement(String s)
    {
        String c = Character.toString(s.charAt(0));
        if(c.equals("~"))
            //if(Character.isAlphabetic(s.charAt(1)))
            //{
            //    values.set(variables.indexOf(Character.toString(s.charAt(1))),!values.get(variables.indexOf(Character.toString(s.charAt(1)))));
            //    return examinStatement(s.substring(1, s.length()));
            //}
            //else
                return !examinStatement(s.substring(1, s.length()));
        else if(c.equals("("))
            if(findClosingParan(s)!=s.length()-1)
                if(Arrays.asList(operators).contains(Character.toString(s.charAt(findClosingParan(s)+1))))
                    return operate(s.substring(findClosingParan(s)+1,findClosingParan(s)+2),examinStatement(s.substring(1, findClosingParan(s))),examinStatement(s.substring(findClosingParan(s)+2, s.length())));
                else
                    return examinStatement(s.substring(1, findClosingParan(s)));
            else    
                return examinStatement(s.substring(1, findClosingParan(s)));
        else if(variables.contains(c))
            if(s.endsWith(c))
                return values.get(variables.indexOf(c));
            else
                return operate(s.substring(1, 2),values.get(variables.indexOf(c)),examinStatement(s.substring(2, s.length())));
        else
        {
            System.out.println("failed: "+s);
            return false;
        }
    }
    
    public int findFirstOp(String s)
    {
        for(char c:s.toCharArray())
        {
            if(Arrays.asList(operators).contains(Character.toString(c)))
                return s.indexOf(c);
        }
        return -1;
    }
    
    public String translateBool(boolean b)
    {
        if(b)
            return "/true/";
        else 
            return "/false/";
    }
    
    public int findClosingParan(String s)
    {
        int count = 0;
        for(int i=0;i<s.length();i++)
        {
            if(Character.toString(s.charAt(i)).equals("("))
                count++;
            else if(Character.toString(s.charAt(i)).equals(")"))
                count--;
            if(count==0)
                return i;
        }
        System.out.println("Failed to find closing parenthesis: "+s);
        return -1;
    }
    
    public boolean operate(String arg,boolean a, boolean b)
    {
        if(arg.equals("&"))
            return a&&b;
        else if(arg.equals("v"))
            return a||b;
        return false;
    }
}
