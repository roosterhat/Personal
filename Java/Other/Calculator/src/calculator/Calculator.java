/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package calculator;

import java.util.*;

/**
 *
 * @author erik
 */
public class Calculator
{

    Scanner scanner = new Scanner(System.in);
    public static void main(String[] args)
    {
        new Calculator().getEquation();
    }
    
    public void getEquation()
    {
        System.out.print(":> ");
        String equation = scanner.nextLine();
        System.out.println("= "+parseEquation(equation));
    }
    
    public String parseEquation(String e)
    {
        while(e.contains("!"))
        {
            int count = e.indexOf("!");
            while(true&&count>=0)
            {
                if(isInteger(e.substring(count-1,count)))
                {
                    break;
                }
                count--;
            }
            String temp = String.valueOf(simpleMath(e.substring(count-1,e.indexOf("!")+1)));
            e = e.substring(0, count-2)+temp+e.substring(e.indexOf("!")+1);
        }
        while(e.contains("^"))  
        {
            int count = e.indexOf("^");
            while(true&&count>=0)
            {
                if(isInteger(e.substring(count-1,count)))
                {
                    break;
                }
                count--;
            }
            int count2 = e.indexOf("^");
            while(true&&count>=0)
            {
                if(isInteger(e.substring(count2-1,count2)))
                {
                    break;
                }
                count2++;
            }
            String temp = String.valueOf(simpleMath(e.substring(count-1,e.indexOf("^")+count2+1)));
            e = e.substring(0, count-1)+temp+e.substring(e.indexOf("^")+count2+1);
        }
        while(e.contains("("))
        {
            String temp = String.valueOf(simpleMath(e.substring(e.lastIndexOf("(")+1, e.indexOf(")",e.lastIndexOf("(")))));
            //System.out.println("IndexStart "+e.lastIndexOf("(")+1+" IndexEnd "+e.indexOf(")",e.lastIndexOf("(")));
            //System.out.println("Test "+e.substring(e.lastIndexOf("(")+1, e.indexOf(")",e.lastIndexOf("("))));
            //System.out.println("Before "+e);
            //System.out.println("add "+temp);
            e = e.substring(0,e.lastIndexOf("("))+temp+e.substring(e.indexOf(")",e.lastIndexOf("("))+1);
            //System.out.println("After "+e+"\n");
        }
        return e;
    }
    
    public double simpleMath(String e)
    {
        System.out.println("equ "+e);
        double result = 0;
        String operator = "";
        String[] operators = {"+","-","*","/","^"};
        for(String s:operators)
        {
            if(e.contains(s))
            {
                operator = s;
                break;
            }
        }
        if(!operator.equals(""))
        {
            if(operator.equals("+"))
            {
                operator = "\\+";
            }
            else if(operator.equals("*"))
            {
                operator = "\\*";
            }
            else if(operator.equals("^"))
            {
                operator = "\\^";
            }
            
            String[] parts = e.split(operator);
            double var1 = Double.parseDouble(parts[0]);           
            double var2 = Double.parseDouble(parts[1]);
            if(operator.equals("\\+"))
            {
                result = var1+var2;
            }
            else if(operator.equals("-"))
            {
                result = var1-var2;
            }
            else if(operator.equals("\\*"))
            {
                result = var1*var2;
            }
            else if(operator.equals("/"))
            {
                result = var1/var2;
            }
            else if(operator.equals("\\^"))
            {
                result = Math.pow(var1, var2);
            }
        }
        else
        {
            if(e.contains("!"))
            {
                result = 1;
                int a = Integer.parseInt(e.split("!")[0]);
                for(int x=a;x>0;x--)
                {
                    result*=x;
                }
            }
        }
        return result;
    }
    
    public static boolean isInteger(String s) 
    {
        try 
        { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) 
        { 
            return false; 
        }
        return true;
    }
    
}
