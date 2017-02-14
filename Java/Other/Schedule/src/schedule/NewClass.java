/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schedule;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

// TODO: uncomment line
//import org.jdesktop.swingx.JXCollapsiblePane;

public class NewClass extends JFrame {
    private static final int TEST_NUMBER_OF_PANELS = 50;
    private static JTabbedPane tabbedPane = new JTabbedPane();
    NewClass() {
        this.setLayout(new BorderLayout());
        this.setSize(1050, 700);
        this.setMinimumSize(new Dimension(400,200));
        this.add(new TestTabContent(), BorderLayout.CENTER);
        //        this.add(tabbedPane, BorderLayout.CENTER);


        tabbedPane.addTab("tab1", new TestTabContent());
        tabbedPane.addTab("tab2", new TestTabContent());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new NewClass().setVisible(true);
            }
        });
    }

    private class TestTabContent extends JPanel {
        TestTabContent() {
            JPanel boxContainer = new JPanel();
            boxContainer.setLayout(new BoxLayout(boxContainer, BoxLayout.Y_AXIS));
            JScrollPane mainScrollPane = new JScrollPane(boxContainer);

            // create toolbar
            JPanel toolBar = new JPanel();
            toolBar.setLayout(new BorderLayout());

            //east
            JPanel InfoPanel = new JPanel();
            InfoPanel.setLayout(new BoxLayout(InfoPanel, BoxLayout.X_AXIS));
            InfoPanel.add(new JLabel("test: some info ..."));
            toolBar.add(InfoPanel, BorderLayout.WEST);
            //west
            JPanel viewOptionPanel = new JPanel();
            viewOptionPanel.setLayout(new BoxLayout(viewOptionPanel, BoxLayout.X_AXIS));
            viewOptionPanel.add(new JLabel("some controls.."));
            toolBar.add(viewOptionPanel, BorderLayout.EAST);

            // set main panel´s layout
            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(toolBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainScrollPane)
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(toolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(mainScrollPane, GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE))
                    );

            // create controls 4 test ...
            for (int i = 0; i < TEST_NUMBER_OF_PANELS; i++) {
                TestPanel newTestPanel = new TestPanel();

                // seperator panels for spacing
                JPanel seperator = new JPanel(new BorderLayout());
                seperator.setBackground(Color.black);
                seperator.add(newTestPanel);
                System.out.println(newTestPanel.toString());
                boxContainer.add(seperator);
            }
        }
    }
    private class TestPanel extends JPanel { 
        //private static final Icon COLLAPSE_ICON = new ImageIcon(Test.class.getResource("images/collapse_1616.png"));
        //private static final Icon EXPAND_ICON = new ImageIcon(Test.class.getResource("images/expand_1616.png"));
        private JTable table;
        private DefaultTableModel tableModel;
        private JButton collapsingButton;

        // TODO: uncomment line
        //private JXCollapsiblePane collapsiblePane = new JXCollapsiblePane();
        // TODO: comment line
        private JPanel collapsiblePane = new JPanel();

        public TestPanel() {

            this.setLayout(new BorderLayout()); 

            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // container with boxLayout for collapsiblePane
            JPanel boxContainer = new JPanel();
            boxContainer.setLayout(new BoxLayout(boxContainer, BoxLayout.Y_AXIS));
            boxContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));

            // set table stuff
            tableModel = new DefaultTableModel();
            // column headers
            Vector<String> title = new Vector<String>();
            title.add("A");
            title.add("B");
            title.add("C");
            title.add("D");
            // some random data
            Random randomGenerator = new Random();
            int rnd = randomGenerator.nextInt(10) + 1;
            Vector<Vector<String>> data = new Vector<Vector<String>>();
            Vector<String> row = new Vector<String>();
            for (int i=0; i<rnd; i++) {
                row.add("1");
                row.add("2");
                row.add("3");
                row.add("4");
                data.add(row);
            }
            tableModel.setDataVector(data, title);
            table = new JTable(tableModel);

            boxContainer.add(table.getTableHeader(), BorderLayout.NORTH);
            boxContainer.add(table, BorderLayout.CENTER);

            // other controls / toolbar
            JPanel toolbar = new JPanel();
            toolbar.setLayout(new BorderLayout());

            // buttons to the right
            JPanel toolbarButtonGroup = new JPanel();
            toolbarButtonGroup.setLayout(new BoxLayout(toolbarButtonGroup, BoxLayout.X_AXIS));

            // Button1
            JButton button = new JButton("Button1");
            JPanel sepPanel = new JPanel();
            sepPanel.add(button);
            toolbarButtonGroup.add(sepPanel);

            // Button2
            button = new JButton("Button2");
            sepPanel = new JPanel();
            sepPanel.add(button);
            toolbarButtonGroup.add(sepPanel);

            // Button3
            button = new JButton("Button3");
            sepPanel = new JPanel();
            sepPanel.add(button);

            toolbarButtonGroup.add(sepPanel);

            toolbar.add(toolbarButtonGroup, BorderLayout.EAST);
            boxContainer.add(toolbar);

            JPanel subPanel = new JPanel();
            subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));          

            // add panel with collapse/expand button
            JPanel buttonPanel = new JPanel(); 
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

            collapsingButton = new JButton("foo"); // , COLLAPSE_ICON
            collapsingButton.setName("toggleButton");
            collapsingButton.setHorizontalAlignment(SwingConstants.LEFT);
            collapsingButton.setBorderPainted(false);
            collapsingButton.setFocusPainted(false);

            buttonPanel.add(collapsingButton, BorderLayout.CENTER); 
            buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            subPanel.add(buttonPanel);

            collapsiblePane.setName("collapsiblePane"); 
            collapsiblePane.setLayout(new CardLayout()); 

            collapsiblePane.add(boxContainer, ""); 
            subPanel.add(collapsiblePane); 

            add(subPanel);

            // TODO: uncomment line
            //collapsingButton.addActionListener(collapsiblePane.getActionMap().get( 
            //      JXCollapsiblePane.TOGGLE_ACTION)); 
        } 
    } 
}
