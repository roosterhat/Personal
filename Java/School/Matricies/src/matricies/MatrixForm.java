/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ostlinja
 */
public class MatrixForm extends javax.swing.JPanel {

    /**
     * Creates new form MatrixForm
     */
    MainForm _main;
    String name;
    Matrix matrix;
    JTextField[][] boxes;
    public MatrixForm(Matrix m,MainForm mf, String n) {
        _main = mf;
        name = n;
        matrix = m;
        boxes = new JTextField[m.rows][m.columns];
        initComponents();
        drawMatrix();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        jButton1.setText("Set Size");
        jButton1.setToolTipText("");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("x");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Update");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Duplicate");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 655, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel1);

        jButton5.setText("Round");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Rename");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addGap(2, 2, 2)
                .addComponent(jScrollPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JTextField row = new JTextField(5);
        row.setText(matrix.rows+"");
        JTextField col = new JTextField(5);
        col.setText(matrix.columns+"");
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Rows:"));
        myPanel.add(row);
        myPanel.add(Box.createHorizontalStrut(15));
        myPanel.add(new JLabel("Columns:"));
        myPanel.add(col);
        int res = JOptionPane.showConfirmDialog(
                    this,myPanel,"Set Size",JOptionPane.OK_CANCEL_OPTION);
        
        if(res == JOptionPane.OK_OPTION)
        {
            
            int r = matrix.rows;
            int c = matrix.columns;
            try{r = Integer.parseInt(row.getText());}catch(Exception e){System.out.println("Invalid Row Value");return;}
            try{c = Integer.parseInt(col.getText());}catch(Exception e){System.out.println("Invalid Columm Value");return;}

            matrix.resize(r,c);
            updateSize();
            drawMatrix();
        }   
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this matrix?","Confirm Delete",JOptionPane.WARNING_MESSAGE);
        if(res==JOptionPane.OK_OPTION)
            _main.delete(name);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        for(int r=0;r<matrix.rows;r++)
            for(int c=0;c<matrix.columns;c++)
                if(r<boxes.length && c<boxes[r].length)
                    matrix.set(Double.parseDouble(boxes[r][c].getText()), r, c);
        _main.newOutput("Update ["+name+"] <br>"+matrix.toHtml());
        drawMatrix();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        _main.duplicate(matrix);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        matrix.roundAll();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        JTextField namefield = new JTextField(5);
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Name: "));
        myPanel.add(namefield);
        int res = JOptionPane.showConfirmDialog(
                    this,myPanel,"Set Name",JOptionPane.OK_CANCEL_OPTION);
        
        if(res == JOptionPane.OK_OPTION)
        {
            String n = namefield.getText().replaceAll("\\s", "");
            if(!n.equals(""))
                _main.rename(name, n);
        }
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void updateSize()
    {
        JTextField[][] temp = new JTextField[matrix.rows][matrix.columns];
        for(int r=0;r<matrix.rows;r++)
            for(int c=0;c<matrix.columns;c++)
                if(r<boxes.length && c<boxes[r].length)
                    temp[r][c] = boxes[r][c];
                else
                    temp[r][c] = newMatrixBox(r,c);
        boxes = temp.clone();
        _main.newOutput("Resize ["+name+"] -> "+matrix.rows+","+matrix.columns+"<br>"+matrix.toHtml());
    }
    
    public void drawMatrix()
    {
        jPanel1.removeAll();
        jPanel1.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        for(int row = 0;row<matrix.rows;row++)
        {
            for(int col = 0;col<matrix.columns;col++)
            {
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = col;
                c.gridy = row;
                c.ipadx = 15;
                c.ipady = 15;
                c.insets = new Insets(5,5,5,5);
                JTextField t = newMatrixBox(row,col);
                boxes[row][col] = t;
                jPanel1.add(t, c);
            }
        }
        _main.repaint();
    }
    
    public JTextField newMatrixBox(int r,int c)
    {
        JTextField t = new JTextField(matrix.get(r, c)+"");
        t.setToolTipText("Press 'Enter' to set value");
        t.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = r;
                int col = c;
                matrix.set(Double.parseDouble(e.getActionCommand()), row, col);
                _main.newOutput("Change ["+name+"] "+row+","+col+"<br>"+matrix.toHtml());
                drawMatrix();
            }
        });
        t.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                t.selectAll();
            }
            public void focusLost(FocusEvent e) {
                t.select(0, -1);
            }
        });
        return t;
    }
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
