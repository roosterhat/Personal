/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Sudoku;

import games.GamesCommon;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author erik
 */
public class Suduko extends javax.swing.JFrame implements GamesCommon
{
    public String name = "Suduko";
    ArrayList<ArrayList<Cell>> rows;
    ArrayList<ArrayList<Cell>> columns;
    ArrayList<ArrayList<Cell>> squares;
    Cell[][] cells = new Cell[9][9];
    
    
    public String getName()
    {
        return name;
    }
    
    public void run()
    {
        initComponents();
        initCells();
        this.setVisible(true);
    }
    
    public void initCells()
    {
        rows = new ArrayList();
        columns = new ArrayList();
        squares = new ArrayList();
        for(int y=0;y<9;y++)
        {
            for(int x=0;x<9;x++)
            {
                cells[x][y] = new Cell(jTable2);
            }
            rows.add(new ArrayList());
            columns.add(new ArrayList());
            squares.add(new ArrayList());
        }
        for(ArrayList a:rows)
        {
            for(int x=0;x<9;x++)
            {
                a.add(cells[rows.indexOf(a)][x]);
            }
        }
        for(ArrayList a:columns)
        {
            for(int x=0;x<9;x++)
            {
                a.add(cells[x][columns.indexOf(a)]);
            }
        }
  
        for(int z=0;z<3;z++)
        {
            for(int i=0;i<3;i++)
            {
                for(int y=0;y<3;y++)
                {
                    for(int x=0;x<3;x++)
                    {
                        squares.get(z*3+i).add(cells[i*3+x][z*3+z]);
                    }
                }
            }
        }
   
    }
    
    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        button1 = new java.awt.Button();
        button2 = new java.awt.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Suduko Solver");

        jTable2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9"
            }
        ));
        jTable2.setGridColor(new java.awt.Color(0, 0, 0));
        jTable2.setRowHeight(40);
        jTable2.setShowGrid(true);
        jTable2.setRowSelectionAllowed(false);
        jTable2.setTableHeader(null);
        jScrollPane2.setViewportView(jTable2);

        button1.setLabel("Solve");
        button1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button1ActionPerformed(evt);
            }
        });

        button2.setLabel("Clear");
        button2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                button2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(246, 246, 246)
                .addComponent(button2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 10, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button2ActionPerformed
    {//GEN-HEADEREND:event_button2ActionPerformed
        DefaultTableModel dtm = (DefaultTableModel) jTable2.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        ArrayList temp = new ArrayList();
        for (int y = 0 ; y < nRow ; y++)
            for (int x = 0 ; x < nCol ; x++)
                dtm.setValueAt(null, y, x);
        initCells();
    }//GEN-LAST:event_button2ActionPerformed

    private void button1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_button1ActionPerformed
    {//GEN-HEADEREND:event_button1ActionPerformed
        getCellData();      
        for (int y = 0 ; y < 9 ; y++)
        {
            for (int x = 0 ; x < 9 ; x++)
            {
                findPossibles(cells[x][y]);
            }
        }
        for (int y = 0 ; y < 9 ; y++)
        {
            for (int x = 0 ; x < 9 ; x++)
            {
                System.out.print(cells[y][x].getNumber()+"-");
                for(int i:cells[y][x].getPossibleNumbers())
                {
                    System.out.print(i+",");
                }
                System.out.println();
            }
        }
        //completeCells();
    }//GEN-LAST:event_button1ActionPerformed
    
    public void getCellData()
    {
        DefaultTableModel dtm = (DefaultTableModel) jTable2.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        ArrayList temp = new ArrayList();
        for (int y = 0 ; y < nRow ; y++)
        {
            for (int x = 0 ; x < nCol ; x++)
            {
                if(dtm.getValueAt(y,x)!=null)
                {
                    cells[x][y].setNumber(dtm.getValueAt(y,x).toString());
                }
            }
        }
    }
    
    public ArrayList getRow(Cell c)
    {
        ArrayList temp = null;
        for(ArrayList a:rows)
        {
            if(a.contains(c))
            {
                temp = a;
                System.out.println("found row");
            }
        }
        return temp;
    }
    public ArrayList getColumn(Cell c)
    {
        ArrayList temp = null;
        for(ArrayList a:columns)
        {
            if(a.contains(c))
            {
                temp = a;
                System.out.println("found column");
            }
        }
        return temp;
    }
    public ArrayList getSquare(Cell c)
    {
        ArrayList temp = null;
        for(ArrayList a:squares)
        {
            if(a.contains(c))
            {
                temp = a;
                System.out.println("found square");
            }
        }
        return temp;
    }
    public boolean has(ArrayList<Cell> a,int index)
    {
        boolean contains = false;
        for(Cell c:a)
        {
            String temp = c.getNumber(); 
            if(temp==null)
            {}
            else if(Integer.parseInt(temp)==index)
            {
                contains = true;
            }
        }
        return contains;
    }
    public void findPossibles(Cell c)
    {
        System.out.print(c.toString()+">>");
        if(c.getNumber()!=null)
        {
            System.out.print(">>"+c.getNumber()+"\n");
            return;
        }
        ArrayList<Cell> row = getRow(c);
        ArrayList<Cell> column = getColumn(c);
        ArrayList<Cell> square = getSquare(c);
        //System.out.println("row"+row.toString());
        //System.out.println("column"+column.toString());
        //System.out.println("square"+square.toString());
        for(int x=1;x<=9;x++)
        {
            System.out.print(x);
            if(!has(row,x) && !has(column,x) && !has(square,x))
            {
                if(!c.getPossibleNumbers().contains(x))
                {
                    c.addPossibleNumber(x);
                }
            }
        }
        System.out.print(">>"+c.getNumber()+"\n");
    }
    public void completeCells()
    {
        for (int y = 0 ; y < 9 ; y++)
        {
            for (int x = 0 ; x < 9 ; x++)
            {
                if(cells[x][y].getPossibleNumbers().size()==1)
                {
                    cells[x][y].setNumber(cells[x][y].getPossibleNumbers().get(0).toString());
                    cells[x][y].removePossibleNumber(cells[x][y].getPossibleNumbers().get(0));
                }
            }
        }
    }
    
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Suduko.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Suduko.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Suduko.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Suduko.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new Suduko().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button button1;
    private java.awt.Button button2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
class Cell
{
    JTable table;
    String number;
    ArrayList<Integer> possibleNumbers = new ArrayList();
    public Cell(JTable t)
    { 
        table = t;
    }
    public Cell(JTable t, String i)
    {
        number = i;
        table = t;
    }
    public void setNumber(String n)
    {
        number = n;
    }
    public void addPossibleNumber(int n)
    {
        possibleNumbers.add(n);
    }
    public void removePossibleNumber(int n)
    {
        possibleNumbers.remove(possibleNumbers.indexOf(n));
    }
    public String getNumber()
    {
        return number;
    }
    public ArrayList<Integer> getPossibleNumbers()
    {
        return possibleNumbers;
    }
}
