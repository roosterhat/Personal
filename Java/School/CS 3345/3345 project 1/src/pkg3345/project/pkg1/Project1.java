package pkg3345.project.pkg1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 *
 * @author erik ostlind
 * jeo170030
 */
public class Project1 {   
    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        while (true){
//            System.out.print("Enter value: ");
//            String input = sc.next();
//            if(Pattern.matches("[q](uit)?", input))
//                break;
//            else
//                try{
//                    EratosthenesSieve(Integer.valueOf(input));
//                }catch(Exception e){
//                    System.out.println(e.getMessage());
//                }
//        }
        System.out.println(func(new ArrayList(Arrays.asList(new Integer[]{1,-3,-1,0,3,-4,14,0,0,-1}))));
    }
    
    public static ArrayList<Integer> func(ArrayList<Integer> array){
        int sum = 0,minsum = 0;
        int index = 0;
        for(int i = 0;i<array.size();i++){
            int val = array.get(i);
            if(val+sum>0){
                ArrayList<Integer> potential = func(new ArrayList(array.subList(i+1,array.size())));
                return sumArray(potential)<minsum ? potential : new ArrayList(array.subList(0, index+1));
            }
            if(val>0){
                sum+=val;
            }
            else{
                sum = minsum+=val;
                index = i;
            }
            
        }
        return new ArrayList(array.subList(0, index+1));
    }
    
    public static int sumArray(ArrayList<Integer> array){
        int sum = 0;
        for(int i: array)
            sum+=i;
        return sum;
    }
    
    public static void EratosthenesSieve(int n){
        if(n<2){
            System.out.println("Value must be >= 2");
            return;
        }
        System.out.println("Primes from 2-"+n);
        ArrayList<Integer> sieve = new ArrayList();
        for(int i = 2 ; i<=n; i++)
            sieve.add(i);
        while(!sieve.isEmpty()){
            int i = sieve.get(0);
            int index = 0;
            System.out.println(i);
            for(int x = i;x<=n;x+=i)
                if((index = sieve.indexOf(x))>=0)
                    sieve.remove(index);
        }
            
    }    
}
