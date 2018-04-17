/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package homework8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 *
 * @author eriko
 */
public class GUI extends javax.swing.JFrame {

    ArrayList<Student> students;
    public GUI() {
        students = new ArrayList();
        initComponents();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Students ");

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setToolTipText("Double Click to Edit");
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jMenu1.setText("File");

        jMenuItem1.setText("Import");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Save");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem3.setText("Add");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem4.setText("Remove");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        JPanel p = new JPanel();
        p.add(new JLabel("Enter Student ID"));
        JTextField tf = new JTextField(8);
        p.add(tf);
        int result = JOptionPane.showConfirmDialog(this,p,"Add Student",JOptionPane.PLAIN_MESSAGE);
        if(result==JOptionPane.OK_OPTION){
            long id;
            try{
                id = Long.valueOf(tf.getText());
                if(studentsContain(id))
                    JOptionPane.showMessageDialog(this,"'"+tf.getText()+"' already exists","Creation Error",JOptionPane.ERROR_MESSAGE);
                else
                    students.add(new Student(id));
                updateStudentList();
            }
            catch(Exception e){JOptionPane.showMessageDialog(this,"'"+tf.getText()+"' is an invalid ID","Creation Error",JOptionPane.ERROR_MESSAGE);}
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        JPanel p = new JPanel();
        p.add(new JLabel("Select ID"));
        JList l = new JList(getStudentIDs());
        l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(l);
        Object[] options = {"Remove","Cancel"};
        int result = JOptionPane.showOptionDialog(this,p,"Remove Student",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
        if(result==JOptionPane.YES_NO_OPTION){
            int index = l.getSelectedIndex();
            if(index>=0)
                students.remove(index);
            else
                JOptionPane.showMessageDialog(this,"No Student Selected","Removal Error",JOptionPane.ERROR_MESSAGE);
            updateStudentList();
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        if(evt.getClickCount()==2){
            int index = jList1.getSelectedIndex();
            if(index>=0){
                Student s = getStudent(Long.valueOf(jList1.getModel().getElementAt(index)));
                if(s!=null)
                    showStudentInformation(s);
            }
        }            
    }//GEN-LAST:event_jList1MouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fc.showSaveDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            File dir = fc.getSelectedFile();
            for(Student s: students){
                try{
                    PrintStream fs = new PrintStream(new File(dir.getAbsolutePath()+"\\"+s.id+".txt"));
                    for(LinkedList cur = s.classes;cur!=null;cur=cur.next){
                        if(cur.hasValue()){
                            Class c = (Class)cur.getValue();
                            fs.println(c.department);
                            fs.println(c.classNumber);
                            fs.println(c.sectionNumber);
                            fs.println(c.creditHours);
                        }
                    }                         
                    fs.close();
                }catch(Exception e){System.out.println("Failed to write file for '"+s.id+"'\nERROR: "+e.getMessage());}
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fc.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            File dir = fc.getSelectedFile();
            for(File f: dir.listFiles()){
                try{
                    long id = Long.valueOf(f.getName().split("\\.")[0]);
                    Student s = new Student(id);
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    String dept;
                    while((dept=reader.readLine())!=null){
                        int number = Integer.valueOf(reader.readLine());
                        String sec = reader.readLine();
                        int credit = Integer.valueOf(reader.readLine());
                        s.addClass(new Class(dept,number,sec,credit));
                    }
                    reader.close();
                    students.add(s);
                }catch(Exception e){System.out.println("Failed to read file '"+f.getName()+"'\nERROR: "+e.getMessage());}
            }
            updateStudentList();
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    
    private void showStudentInformation(Student student){
        JOptionPane.showConfirmDialog(this,new StudentForm(student),"Student Information",JOptionPane.PLAIN_MESSAGE);
    }
    
    private Student getStudent(long id){
        for(Student s: students)
            if(s.id==id)
                return s;
        return null;
    }
    
    private void updateStudentList(){
        students.sort((x,y)->(int)(x.id-y.id));
        jList1.setListData(getStudentIDs());
    }
    
    private String[] getStudentIDs(){
        String[] data = new String[students.size()];
        for(int i=0;i<students.size();i++)
            data[i] = String.valueOf(students.get(i).id);
        return data;
    }
    
    private boolean studentsContain(long id){
        for(Student s: students)
            if(s.id==id)
                return true;
        return false;
    }
        
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
