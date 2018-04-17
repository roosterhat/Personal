/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework8;

import java.util.Comparator;

/**
 *
 * @author eriko
 */
public class Class{
    String department;
    int classNumber;
    String sectionNumber;
    int creditHours;
    public Class(String dept, int classNum, String classSec, int credit){
        department = dept;
        classNumber = classNum;
        sectionNumber = classSec;
        creditHours = credit;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Class)
            return department.equalsIgnoreCase(((Class)obj).department) && 
                    classNumber==((Class)obj).classNumber && 
                    sectionNumber.equals(((Class)obj).sectionNumber);
        return super.equals(obj);
    }
    
    public String toString(){
        return department+" "+classNumber+"."+sectionNumber+" credits: "+creditHours;
    }
}

class ClassComparator implements Comparator{
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null) 
            return -1;
        if (o2 == null) 
            return 1;
        if (o1.equals(o2))
            return 0;
        String c1 = ((Class)o1).toString();
        String c2 = ((Class)o2).toString();
        return c1.compareTo(c2);
    }
    
}
