package assignmentschedule;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import java.net.URL;
import javax.swing.*;
import java.io.*;

public class AssignmentSchedule extends Applet implements MouseListener, MouseWheelListener
{

    private int scroll, width, length, scrollBarSize;
    private Image dbImage;
    private Graphics dbg;
    ArrayList<Assignments> assignmentList = new ArrayList();
    ArrayList<Button> buttons = new ArrayList();
    
    
    public void init()
    {
        Button b = new Button(0, 0, 40, 20, "New", true, Color.GRAY);
        buttons.add(b);
        Button b1 = new Button(41, 0, 40, 20, "Save", true, Color.GRAY);
        buttons.add(b1);
        Button b2 = new Button(82, 0, 80, 20, "Delete All", true, Color.GRAY);
        buttons.add(b2);
        Button b3 = new Button(163, 0, 40, 20, "Info", true, Color.GRAY);
        buttons.add(b3);
        int count = 0;
        scroll = 0;
        this.setSize(400, 650);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        width = this.getSize().width;
        length = this.getSize().height;
        Path path = Paths.get("C:/Users/erik/Documents/school/Schedule.txt");
        try
        {
            Scanner scanner = new Scanner(path, StandardCharsets.UTF_8.name());
            while (scanner.hasNextLine())
            {
                boolean temp = false;
                String line = scanner.nextLine();
                //System.out.println(line);
                String[] parts = line.split(",");
                //System.out.println(parts[3]);
                if (parts[3].equalsIgnoreCase("true"))
                {
                    temp = true;
                }

                Assignments a1 = new Assignments(parts[0], parts[1], count, parts[2], temp, parts[4]);
                assignmentList.add(a1);
                count++;
            }
        } catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
        }

    }

    public void paint(Graphics g)
    {
        width = this.getSize().width;
        length = this.getSize().height;
        for (int i = 0; i < assignmentList.size(); i++)
        {
            assignmentList.get(i).drawAssignment(g, scroll);
        }
        drawScrollBar(g);
        drawMenus(g);
        repaint();
    }

    public void update(Graphics g)
    {
        // initialize buffer
        if (dbImage == null)
        {

            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics();

        }

        // clear screen in background
        dbg.setColor(getBackground());
        dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);

        // draw elements in background
        dbg.setColor(getForeground());
        paint(dbg);

        // draw image on the screen
        g.drawImage(dbImage, 0, 0, this);
    }

    public void drawMenus(Graphics g)
    {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width, 20);
        for (int i = 0; i < buttons.size(); i++)
        {
            buttons.get(i).drawButton(g);
        }
        g.drawString("Assignments: "+assignmentList.size(),width-100,15);
    }

    public void drawScrollBar(Graphics g)
    {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(width - 20, 0, width, length);
        g.setColor(Color.GRAY);
        if (assignmentList.size() * 130 > length)
        {

            scrollBarSize = (int) (length - ((50 - (50 - (Math.round(50 * 130.0 / length)))) * 130));

        } else
        {
            scrollBarSize = length;
        }
        g.fillRect(width - 20, -1 * scroll, width, scrollBarSize);
    }

    public void newAssignment(int index)
    {
        int place=0;
        String[] assignmentTypes = { "HomeWork", "Test", "Quiz", "Project", "Study", "Other" };
        JTextField nameField = new JTextField(20);
        JTextField dateField = new JTextField(5);
        JTextField descriptionField = new JTextField(50);
        JComboBox typeComboBox = new JComboBox(assignmentTypes);

        JPanel myPanel = new JPanel(new GridLayout(0,1));
        myPanel.add(new JLabel("Type:"));
        myPanel.add(typeComboBox);
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(new JLabel("Due Date:"));
        myPanel.add(dateField);
        
        myPanel.add(new JLabel("Description:"));
        myPanel.add(descriptionField);
        if(index>-1)
        {
            typeComboBox.setSelectedItem(assignmentList.get(index).getType());
            nameField.setText(assignmentList.get(index).getName());
            dateField.setText(assignmentList.get(index).getDate());
            descriptionField.setText(assignmentList.get(index).getDescription());
            System.out.println(">"+assignmentList.get(index).getDescription());
            place = assignmentList.get(index).getPosition();
        }
        else
        {
            place = assignmentList.size();
        }
        int result = JOptionPane.showConfirmDialog(null, myPanel, "New Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) 
        {
           Assignments a = new Assignments(nameField.getText(),descriptionField.getText(),place,dateField.getText(),
                   false,typeComboBox.getSelectedItem().toString());
           assignmentList.add(a);
           if(index!=-1)
           {
                assignmentList.remove(index);
           }
        }
    }
    public void validate(String name, String disc, String date) throws Exception
    {
        if (name==null||name.isEmpty())
        {
            throw new Exception();
        }
    }
    public void refreshPositions(int pos)
    {
        for (int i = 0; i < assignmentList.size(); i++)
        {
            if (assignmentList.get(i) != null)
            {
                if (assignmentList.get(i).getPosition() - 1 == pos)
                {
                    pos = assignmentList.get(i).getPosition();
                    assignmentList.get(i).setPosition(assignmentList.get(i).getPosition() - 1);
                }
            }
        }
    }

    public void saveAssignments()
    {

        BufferedWriter writer = null;
        try
        {
            File logFile = new File("C:/Users/erik/Documents/school/Schedule.txt");
            writer = new BufferedWriter(new FileWriter(logFile));
            for (int i = 0; i < assignmentList.size(); i++)
            {
                writer.write(assignmentList.get(i).getName() + "," + assignmentList.get(i).getDescription() + "," + assignmentList.get(i).getDate() + ","
                        + assignmentList.get(i).getChecked() + "," + assignmentList.get(i).getType()+"\r\n");
            }
            JOptionPane.showMessageDialog(this, "Saved assignments", null, 1);
        } catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Failed to save data: " + e.getMessage(), null, 1);
        } finally
        {
            try
            {
                writer.close();
            } 
            catch (Exception e)
            {
            }
        }


    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        for (int i = 0; i < buttons.size(); i++)
        {
            if (buttons.get(i).clickedButton(e.getX(), e.getY()))
            {
                if (buttons.get(i).getName() == "New")
                {
                    newAssignment(-1);
                } 
                else if (buttons.get(i).getName() == "Save")
                {
                    saveAssignments();
                }
                else if (buttons.get(i).getName() == "Delete All")
                {
                    if (JOptionPane.showConfirmDialog(this, "Are you sure you want to Delete all assignments", null, 1) == 0)
                    {
                        assignmentList.clear();
                    }
                }
                else if(buttons.get(i).getName()=="Info")
                {
                    int hw=0;
                    int t=0;
                    int q=0;
                    int p=0;
                    int s=0;
                    int o=0;
                    int f=0;
                    for (int x = 0; x < assignmentList.size(); x++)
                    {
                        //System.out.println(assignmentList.get(x).getType());
                        if(assignmentList.get(x).getType().equals("HomeWork"))
                        {
                            hw++;
                        }
                        else if(assignmentList.get(x).getType().equals("Test"))
                        {
                            t++;
                        }
                        else if(assignmentList.get(x).getType().equals("Quiz"))
                        {
                            q++;
                        }
                        else if(assignmentList.get(x).getType().equals("Project"))
                        {
                            p++;
                        }
                        else if(assignmentList.get(x).getType().equals("Study"))
                        {
                            s++;
                        }
                        else if(assignmentList.get(x).getType().equals("Other"))
                        {
                            o++;
                        }
                        if(assignmentList.get(x).getChecked().equals("true"))
                        {
                            f++;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Home Work: "+hw+"\nTests: "+t+"\nQuizes: "+q+"\nProjects: "+p+"\nStudy: "+s+"\nOther: "+o+"\n\nFlagged: "+f, "Assignment Info", 1);
                }
            }
        }
        for (int i = 0; i < assignmentList.size(); i++)
        {
            assignmentList.get(i).checkClickedChecked(e.getX(), e.getY(), scroll);
            if (assignmentList.get(i).checkClickedDelete(e.getX(), e.getY(), scroll))
            {
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to Delete this assignment", null, 1) == 0)
                {
                    assignmentList.remove(i);
                    refreshPositions(i);
                }
            }
            if (assignmentList.get(i).checkClickedArrows(e.getX(), e.getY(), scroll) == 1)
            {
                //System.out.println("UP");
                if (assignmentList.get(i).getPosition() > 0)
                {
                    for (int x = 0; x < assignmentList.size(); x++)
                    {
                        if (assignmentList.get(i).getPosition() - 1 == assignmentList.get(x).getPosition())
                        {
                            assignmentList.get(x).setPosition(assignmentList.get(i).getPosition());
                            assignmentList.get(i).setPosition(assignmentList.get(i).getPosition() - 1);

                            break;
                        }
                    }

                }
            } 
            else if (assignmentList.get(i).checkClickedArrows(e.getX(), e.getY(), scroll) == 2)
            {
                //System.out.println("DOWN");
                if (assignmentList.get(i).getPosition() < assignmentList.size() - 1)
                {
                    for (int x = 1; x < assignmentList.size(); x++)
                    {
                        if (assignmentList.get(i).getPosition() + 1 == assignmentList.get(x).getPosition())
                        {
                            assignmentList.get(x).setPosition(assignmentList.get(i).getPosition());
                            assignmentList.get(i).setPosition(assignmentList.get(i).getPosition() + 1);

                            break;
                        }
                    }
                }
            }
            else if(assignmentList.get(i).checkClickedEdit(e.getX(), e.getY(), scroll))
            {
                newAssignment(i);
                break;
            }
        }

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        scroll += (-10 * e.getWheelRotation());
        if (scroll > 0)
        {
            scroll = 0;
        }
        if ((-1 * scroll) + scrollBarSize > length)
        {
            scroll = (scroll + 10);
        }
        //System.out.println(scroll);
    }
}

class Assignments
{

    String name, description, dueDate, type;
    int place;
    boolean checked;

    public Assignments(String n, String disc, int p, String date, boolean c, String t)
    {
        name = n;
        description = disc;
        place = p;
        dueDate = date;
        checked = c;
        type = t;
    }

    public void drawAssignment(Graphics g, int s)
    {
        Color c1;
        Color c2;
        c1 = new Color(153, 217, 234);
        c2 = new Color(0, 162, 232);
        if (type.equals("Project"))
        {
            c1 = new Color(214, 160, 214);
            c2 = new Color(163, 73, 164);
        } else if (type.equals("Test") || type.equals("Quiz"))
        {
            c1 = new Color(255, 249, 132);
            c2 = new Color(255, 242, 0);
        } else if (type.equals("Study"))
        {
            c1 = new Color(116, 228, 150);
            c2 = new Color(34, 177, 76);
        }
        else if(type.equals("Other")) 
        {
            c1 = new Color(255, 174, 201);
            c2 = new Color(232, 0, 75);
        }
        g.setColor(c1);
        g.fillRect(10, (place * 130) + s + 20, 300, 100);
        g.setColor(c2);
        g.fillRect(10, (place * 130) + s + 20, 300, 20);
        g.setColor(Color.BLACK);
        g.drawString(name + "  Due: " + dueDate + "   ["+type+"]", 15, (place * 130) + s + 35);
        g.drawString(description, 15, (place * 130) + s + 50);
        g.setColor(Color.RED);
        g.fillRect(290, (place * 130) + s + 20, 20, 20);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(260, (place * 130) + s + 20, 20, 20);
        g.fillRect(290, (place * 130) + s + 80, 20, 40);
        g.setColor(Color.GRAY);
        int[] xPoints ={295, 300, 305};
        int[] yPoints ={(place * 130) + s + 95, (place * 130) + s + 85, (place * 130) + s + 95};
        g.fillPolygon(xPoints, yPoints, 3);
        int[] yxPoints ={(place * 130) + s + 105, (place * 130) + s + 115, (place * 130) + s + 105};
        g.fillPolygon(xPoints, yxPoints, 3);
        if (checked)
        {
            g.setColor(Color.GREEN);
            g.fillRect(260, (place * 130) + s + 20, 20, 20);
        }
        g.setColor(Color.GRAY);
        g.fillRect(240,(place * 130) + s + 100,40,20);
        g.setColor(Color.BLACK);
        g.drawString("Edit",250,(place * 130) + s + 115);
        
        //g.drawString("Due: "+dueDate,260,(place * 130) + s + 35);

    }

    public boolean checkClickedDelete(int xpos, int ypos, int s)
    {
        boolean inside = false;
        if (xpos >= 290 && xpos <= 310)
        {
            if (ypos >= (place * 130) + s + 20 && ypos <= (place * 130) + s + 40)
            {
                inside = true;
            }
        }
        return inside;
    }

    public void checkClickedChecked(int xpos, int ypos, int s)
    {
        if (xpos >= 260 && xpos <= 280)
        {
            if (ypos >= (place * 130) + s + 20 && ypos <= (place * 130) + s + 40)
            {
                checked = !checked;
            }
        }
    }
    public boolean checkClickedEdit(int xpos, int ypos, int s)
    {
        boolean inside = false;
        if (xpos >= 240 && xpos <= 280)
        {
            if (ypos >= (place * 130) + s + 100 && ypos <= (place * 130) + s + 120)
            {
                inside = true;
            }
        }
        return inside;
    }

    public int checkClickedArrows(int xpos, int ypos, int s)
    {
        int arrow = 0;
        if (xpos >= 290 && xpos <= 310)
        {
            if (ypos >= (place * 130) + s + 80 && ypos <= (place * 130) + s + 100)
            {
                arrow = 1;
            }
            if (ypos >= (place * 130) + s + 100 && ypos <= (place * 130) + s + 120)
            {
                arrow = 2;
            }
        }
        return arrow;
    }

    public void setPosition(int pos)
    {
        place = pos;
    }

    public int getPosition()
    {
        return place;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDate()
    {
        return dueDate;
    }

    public String getChecked()
    {
        return String.valueOf(checked);
    }

    public String getType()
    {
        return type;
    }
}

class Button
{

    int x, y, width, length;
    String name;
    boolean border;
    Color color;

    public Button(int xpos, int ypos, int w, int l, String text, boolean b, Color c)
    {
        x = xpos;
        y = ypos;
        width = w;
        length = l;
        name = text;
        border = b;
        color = c;
    }

    public void drawButton(Graphics g)
    {
        g.setColor(color);
        g.fillRect(x, y, width, length);
        g.setColor(Color.BLACK);
        if (border)
        {
            g.drawRect(x, y, width, length);
        }
        g.drawString(name, (int) ((width / 2) - ((name.length() * 7) / 2)) + x, (int) (length / 1.5) + y);
    }

    public boolean clickedButton(int xpos, int ypos)
    {
        boolean inside = false;
        if (xpos >= x && xpos <= x + width)
        {
            if (ypos >= y && ypos <= y + length)
            {
                inside = true;
            }
        }
        return inside;
    }

    public String getName()
    {
        return name;
    }
}