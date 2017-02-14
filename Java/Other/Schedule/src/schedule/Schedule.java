/**
 *
 * @author Erik Ostlind
 */
package schedule;

import java.awt.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import java.io.*;
import java.text.SimpleDateFormat;

public class Schedule
{

    static ArrayList<Assignment> assignments = new ArrayList();

    static MainWindow main;

    public static void main(String[] args)
    {
        Schedule x = new Schedule();
        main = new MainWindow();
        x.getScheduleData();
        x.addPanels();
        main.pack();
        main.setVisible(true);

    }

    public void getScheduleData()
    {
        Path path = Paths.get("C:/Users/erik/Documents/school/Schedule.txt");
        try
        {
            Scanner scanner = new Scanner(path, StandardCharsets.UTF_8.name());
            while (scanner.hasNextLine())
            {
                boolean temp = false;
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts[3].equalsIgnoreCase("true"))
                {
                    temp = true;
                }
                String[] dates = parts[2].split("/");
                Calendar c = new GregorianCalendar(Integer.parseInt(dates[2]),Integer.parseInt(dates[0])-1,Integer.parseInt(dates[1]));
                Assignment a1 = new Assignment(parts[0], parts[4], parts[1], c, temp, getPanelColor(parts[4]));
                assignments.add(a1);
            }
        } catch (Exception e)
        {
            System.out.println(">" + e.toString());
        }
        
    }

    public void addPanels()
    {
        MainWindow.ContentPanel temp = main.new ContentPanel();
    }

    public Color getPanelColor(String type)
    {

        Color c1 = new Color(153, 217, 234);
        Color c2 = new Color(0, 162, 232);
        if (type.equals("Project"))
        {
            c1 = new Color(214, 160, 214);
            c2 = new Color(163, 73, 164);
        } 
        else if (type.equals("Test") || type.equals("Quiz"))
        {
            c1 = new Color(255, 249, 132);
            c2 = new Color(255, 242, 0);
        } 
        else if (type.equals("Study"))
        {
            c1 = new Color(116, 228, 150);
            c2 = new Color(34, 177, 76);
        } 
        else if (type.equals("Other"))
        {
            c1 = new Color(255, 174, 201);
            c2 = new Color(232, 0, 75);
        }
        return c1;
    }
    public void deleteAssignemnts()
    {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to Delete all assignments", "Warning", JOptionPane.YES_NO_OPTION) == 0)
        {
            assignments.clear();
        }
    }
    public void deleteAssignemnt(Assignment a)
    {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to Delete this assignments", "Warning", JOptionPane.YES_NO_OPTION) == 0)
        {
            assignments.remove(assignments.indexOf(a));
        }
    }
    
    public void showInfo()
    {
        int hw=0;
        int t=0;
        int q=0;
        int p=0;
        int s=0;
        int o=0;
        int f=0;
        
        for (int x = 0; x < assignments.size(); x++)
        {
            String type = assignments.get(x).getAssignmentType();
            //System.out.println(assignmentList.get(x).getType());
            if(type.equals("HomeWork"))
                hw++;
            
            else if(type.equals("Test"))
                t++;
            
            else if(type.equals("Quiz"))
                q++;
            
            else if(type.equals("Project"))
                p++;
            
            else if(type.equals("Study"))
                s++;

            else if(type.equals("Other"))
                o++;

            if(type.equals("true"))
                f++;

        }
        JOptionPane.showMessageDialog(null, "Home Work: "+hw+"\nTests: "+t+"\nQuizes: "+q+"\nProjects: "+p+"\nStudy: "+s+"\nOther: "+o+"\n\nFlagged: "+f, "Assignment Info", 1);

    }
    public void saveAssignments()
    {

        BufferedWriter writer = null;
        try
        {
            File logFile = new File("C:/Users/erik/Documents/school/Schedule.txt");
            writer = new BufferedWriter(new FileWriter(logFile));
            for (Assignment a:assignments)
            {
                writer.write(a.getName() + "," + a.getDescription() + "," + new SimpleDateFormat("MM/dd/yyyy").format(a.getDate().getTime()) + ","
                        + a.getFlagged() + "," + a.getAssignmentType() + "\r\n");
            }
            JOptionPane.showMessageDialog(null, "Saved assignments", "Schedule", 1);
        } 
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Failed to save data: " + e.toString(), "Error", 1);
        } 
        finally
        {
            try
            {
                writer.close();
            } 
            catch (Exception e)
            {
                System.out.println(e.toString());
            }
        }
    }
    public void addAssignment(int index, Assignment a)
    {
        assignments.add(a);
        if (index != -1)
        {
            assignments.remove(index);
        }
    }
    public ArrayList<Assignment> getAssignments()
    {
        return assignments;
    }
    public void newAssignment(int index)
    {
        AssignmentCreator temp = new AssignmentCreator(index,assignments);
        temp.pack();
        temp.setVisible(true);
        
            
        
       
    }

}
