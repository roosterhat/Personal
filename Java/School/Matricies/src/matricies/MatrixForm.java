/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
    int index;
    Matrix matrix;
    public MatrixForm(Matrix m,MainForm mf, int i) {
        _main = mf;
        index = i;
        matrix = m;
        initComponents();
        drawMatrix();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
                .addComponent(jButton2))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 21, Short.MAX_VALUE))
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
            matrix.setRows(r);
            matrix.setColumns(c);
            drawMatrix();
        }   
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        _main.delete(index);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void drawMatrix()
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
                jPanel1.add(new JTextField(matrix.get(row, col)+""), c);
            }
        }
        _main.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
