                                    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
//
/**
 *
 * @author erik
 */
public class MainWindow extends javax.swing.JFrame
{

    /**
     * Creates new form MainWindow
     */
    public MainWindow()
    {
        initComponents();
        createMenuBar();
        
    }
    public void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JButton newBtn = new JButton("New");
        newBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                newActionPreformed(evt);
            }
        });
        menuBar.add(newBtn);
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveActionPreformed(evt);
            }
        });
        menuBar.add(saveBtn);
        JButton deleteBtn = new JButton("Delete All");
        deleteBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteActionPreformed(evt);
            }
        });
        menuBar.add(deleteBtn);
        JMenu grouping = new JMenu("Grouping");
            JMenuItem normal = new JMenuItem("Normal");            
            grouping.add(normal);
            JMenuItem dueDate = new JMenuItem("Due Date");
            grouping.add(dueDate);
            JMenuItem importance = new JMenuItem("Importance");
            grouping.add(importance);
            JMenuItem flag = new JMenuItem("Flagged");
            grouping.add(flag);
            JMenuItem type = new JMenuItem("Type");
            grouping.add(type);
            JMenu SOM = new JMenu("Show Only");
                JMenuItem project = new JMenuItem("Project");
                SOM.add(project);
                JMenuItem test = new JMenuItem("Test");
                SOM.add(test);
                JMenuItem quiz = new JMenuItem("Quiz");
                SOM.add(quiz);
                JMenuItem study = new JMenuItem("Study");
                SOM.add(study);
                JMenuItem homework = new JMenuItem("HomeWork");
                SOM.add(homework);
                JMenuItem other = new JMenuItem("Other");
                SOM.add(other);
            grouping.add(SOM);
        SOM.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                //method
            }
        });
        grouping.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                //method
            }
        });
        
        JButton infoBtn = new JButton("Info");
        infoBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                infoActionPreformed(evt);
            }
        });
        menuBar.add(infoBtn);
        menuBar.add(grouping);
        setJMenuBar(menuBar);
        this.pack();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jMenuBar1 = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Assignment Schedule ");
        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 447, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 298, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public void addPane(JPanel a)
    {
       System.out.println(a.toString());
       this.add(a);

    }
    private void newActionPreformed(java.awt.event.ActionEvent evt)                                         
    {                                             
        new Schedule().newAssignment(-1);
    }   
    private void saveActionPreformed(java.awt.event.ActionEvent evt)                                         
    {                                             
        new Schedule().saveAssignments();
    }  
    private void deleteActionPreformed(java.awt.event.ActionEvent evt)                                         
    {                                             
        new Schedule().deleteAssignemnts();
    }  
    private void infoActionPreformed(java.awt.event.ActionEvent evt)                                         
    {                                             
        new Schedule().showInfo();
    }  
    
    public class ContentPanel extends JPanel //contentpanel is inside mainwindow
    {
        ContentPanel() 
        {
            ArrayList<Assignment> temp = new ArrayList(new Schedule().getAssignments());
            JPanel boxContainer = new JPanel();
            boxContainer.setLayout(new BoxLayout(boxContainer, BoxLayout.Y_AXIS));
            JScrollPane mainScrollPane = new JScrollPane(boxContainer);
            GroupLayout layout = new GroupLayout(this); 
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(mainScrollPane)
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addGap(0, 0, 0)
                            .addComponent(mainScrollPane, GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))
                    );
            for (int i = 0; i < temp.size(); i++) 
            {
                //System.out.println(temp.get(i).getName());
                SchedulePanel newTestPanel = new SchedulePanel(temp.get(i));

                // seperator panels for spacing
                JPanel seperator = new JPanel(new BorderLayout());
                seperator.setBackground(Color.black);
                seperator.add(newTestPanel);
                System.out.println(newTestPanel.getPanel().toString());
                boxContainer.add(seperator);
            }
            //System.out.println(boxContainer.toString());
            addPane(boxContainer);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    // End of variables declaration//GEN-END:variables
}

