/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
    
    //finds the index of a number based on index and direction
    //direction is either -1 or 1 (left or right)
    //i.e. findSubPart("12+34",2,-1) = 0 >> "12"
    private int findSubPart(String s,int index,int dir){
        if(index>=0 && index<s.length()){
            if(String.valueOf(s.charAt(index)).matches("[()]"))
                return findMatchingParen(s,index,dir);
            else{
                int pos = 0;
                String exp = getOperatorExpression();
                for(int i=index;i!=Math.max(-1, (dir*s.length())%(s.length()+1));i+=dir){
                    if(!(""+s.charAt(i)).matches(exp))
                        pos = i;
                    else if(s.charAt(i)=='-'){
                        if(findNegativeNumber(s,i))
                            pos = i;
                        else
                            return pos;
                    }
                    else
                        return pos;
                }
                return pos;
            }
        }
        return index;
    }
    
    //determines if there is a negative number at the given index
    private boolean findNegativeNumber(String s,int index)
    {
        if(s.charAt(index)=='-')
            if(index==0 || !(""+s.charAt(index-1)).matches("[\\d\\.]"))
                return true;
        return false;
    }
    
    //replaces all variables in equation with given value 'x'
    public String substituteVariable(String s,double x)
    {
        if(s.contains(variable))
            s = s.replaceAll("["+variable+"]", "("+x+")");
        return s;
    }
    
    
    
    
    //returns the index of the matching parenthesis to the given parenthesis
    //direction functions the same as in findSubPart, must be -1 or 1 
    private int findMatchingParen(String s,int index,int dir){
        int parens = 0;
        for(int i=index;i!=Math.max(-1, (dir*s.length())%(s.length()+1));i+=dir)
        {
            char c = s.charAt(i);
            if(c=='(')
                parens+=dir;
            else if(c==')')
                parens-=dir;
            if(parens==0)
                return i;
        }
        return -1;
    }
    
    public boolean isNumber(String s){
        return s.matches("\\d+(\\.\\d+)?");
    }
    
    //processes parenthesis pairs
    //given an equation, a range representing the parenthesis pair and value
    //computes the value of equation contained in the parenthesis pair
    private String reduce(String s,Range r)
    {
        try{
            String temp = s.substring(0,r.start);
            if(r.start>0 && !touchingOperator(s,r.start,-1))
                temp += "*";
            temp += evaluate(s.substring(r.start+1, r.end));
            if(r.end<s.length()-1 && !touchingOperator(s,r.end+1,1))
                temp += "*";
            temp += s.substring(r.end+1);
            return temp;
        }catch(Exception e){System.out.println(String.format("Parenthesis mismatch in '"+s +"' \n%"+
                                                (26+s.indexOf("("))+"s\n"+e,"^"));}
        return "";
    }
    
    //determines if a given string contains one of the predefined functions next to the given index
    //dir determines which side of the index to check, 0 if inspecific
    public boolean touchingOperator(String s,int index,int dir)
    {
        for(Operation o: operators){
            int start = 0;
            while(s.substring(start).matches(".*"+o.regex+".*")){
                int t = s.indexOf(o.operator, start);
                Range r = new Range(t,t+o.operator.length());
                if(index>=Math.max(-1*dir, -1)+r.start && index<=Math.min(-1*dir, 1)+r.end)
                    return true;
                else
                    start = s.indexOf(o.operator,start)+o.operator.length();
            }
        }
        return false;
    }
   
    //searches for the given operator and evaulates any found in the given equation
    //operators require two values to operate on
    public String evaluateOperator(String eq,Operation o)
    {
        try{
            int start = 0;
            while(eq.substring(start).contains(o.operator)){
                if(findNegativeNumber(eq,eq.indexOf(o.operator,start))){
                    start += eq.indexOf("-",start)+1;
                }
                else{
                    int index = eq.indexOf(o.operator,start);
                    ArrayList part = new ArrayList();
                    Range r = new Range(index,index + o.operator.length());
                    if(o.inputSide<=0){//LEFT
                        int leftSide = index;
                        r.start = Math.max(findSubPart(eq,leftSide-1,-1),0);
                        part.add(evaluate(eq.substring(r.start,leftSide)));
                    }
                    if(o.inputSide>=0){//RIGHT
                        int rightSide = index+o.operator.length();
                        r.end = Math.min(findSubPart(eq,rightSide,1)+1,eq.length());
                        part.add(evaluate(eq.substring(rightSide,r.end)));
                    }
                    padParts(part);
                    eq = eq.substring(0, r.start) + o.execute(part) + eq.substring(r.end);
                }
            }
            return eq;
        }catch(Exception e){System.out.println(String.format(e+"\nError in evaluating '"+eq +"' \n%"+
                                                (22+eq.indexOf(o.operator))+"s\n","^"));}
        return "";
    }
    
    private void padParts(ArrayList<String> p){
        for(int i=p.size();i<5;i++)
            p.add("");
    }
    
    
    public String evaluate(String eq)
    {
        while(eq.contains("(")){
            int index = eq.indexOf("(");
            eq = reduce(eq,new Range(index,findMatchingParen(eq,index,1)));
        }
        for(Operation o: operators)
            eq = evaluateOperator(eq,o);
        return eq;
    }*/