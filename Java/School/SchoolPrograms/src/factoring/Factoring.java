/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package factoring;
import java.util.*;
import schoolprograms.SchoolProgramsCommon;
public class Factoring implements SchoolProgramsCommon
{

    String name = "Factoring";
    public static void main(String[] args)
    {
        
    }
    public void run()
    {
        ArrayList<Integer> components = new ArrayList();
        ArrayList<Double> factors = new ArrayList();
        ArrayList<Double> roots = new ArrayList();
        Scanner scanner = new Scanner(System.in);
        System.out.println("What degree is the polynomial?");
        int degree = scanner.nextInt();
        for(int x=0;x<=degree;x++)
        {
            System.out.println("Enter value for degree "+(degree-x));
            components.add(scanner.nextInt());
        }
        for(int x=0;x<components.size()-1;x++)
        {
            if(components.get(x)!=0)
            {
                System.out.print(components.get(x)+"X^"+(components.size()-x)+"+");
            }
        }
        System.out.println(components.get(components.size()-1));
//        
            
            for(int i=1;i<=components.get(components.size()-1);i++)
            {
                //System.out.println("derp"+i);
                if(components.get(components.size()-1)%i==0)
                {
                    factors.add((double)(i));
                    System.out.print(factors.get(factors.size()-1)+",");
                }
            }
            int size = factors.size();
            for(int i=1;i<=components.get(0);i++)
            {
                //System.out.println("derp");
                if(components.get(0)%i==0)
                {
                    for(int a=0;a<size;a++)
                    {
                        //System.out.println(i+"-"+factors.get(a));
                        double PF = (double)(factors.get(a))/i;
                        //System.out.println(Boolean.toString(contains(factors,PF)));
                        if(!contains(factors,PF))
                        {
                            factors.add(PF);
                            System.out.print(PF+",");
                        }
                    }
                }
            }
            System.out.println();
            for(int x=0;x<components.size()-3;x++)
            {
                for(int i=0;i<factors.size();i++)
                {
                    int total=components.get(0);
                    for(int a=1;a<components.size();a++)
                    {
                        total*=factors.get(i);
                        total+=components.get(a);
                    }
                    if(total==0)
                    {
                        roots.add(factors.get(i));
                        break;
                    }
                }
                
           }
    }
    public String getName()
    {
        return name;
    }
    public static boolean contains(ArrayList temp,double index)
    {
        boolean cont = false;
        for(int i=0;i<temp.size();i++)
        {
            if(index==(double)(temp.get(i)))
            {
                cont = true;
                break;
            }
        }
        return cont;
    }
}
