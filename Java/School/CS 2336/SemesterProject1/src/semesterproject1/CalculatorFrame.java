/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject1;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.text.DefaultEditorKit;

/**
 *
 * @author eriko
 */
public class CalculatorFrame extends javax.swing.JFrame {
    enum Base {HEX,DEC,OCT,BIN};
    Base currentBase = Base.DEC;
    ArrayList<String> equation;
    ArrayList<String> equalsEquation;
    long input;
    long total;
    long prevInput;
    boolean changed = false;
    String currentOperation;
    Integer[] bases = new Integer[]{16,10,8,2};
    ArrayList<String> dataType = new ArrayList(Arrays.asList(new String[]{"QWORD","DWORD","WORD","BYTE"}));
    ArrayList<String> leftOperator = new ArrayList(Arrays.asList(new String[]{"Lsh","RoL"}));
    ArrayList<String> rightOperator = new ArrayList(Arrays.asList(new String[]{"Rsh","RoR"}));
    JButton[] digitButtons;
    GradientButton[] baseButtons;
    Integer[] hexDeselect = new Integer[]{};
    Integer[] decDeselect = new Integer[]{0,1,5,6,10,11};
    Integer[] octDeselect = new Integer[]{0,1,3,4,5,6,10,11};
    Integer[] binDeselect = new Integer[]{0,1,2,3,4,5,6,7,8,9,10,11,13,14};
    Solver solver;
    GradientButton hexButton,decButton,octButton,binButton;
    Action beep;
    public CalculatorFrame() {
        input = 0;
        equation = new ArrayList();
        initComponents();
        addGradientButtons();
        beep = jTextField1.getActionMap().get(DefaultEditorKit.deletePrevCharAction);
        digitButtons = new JButton[]{jButton23, jButton24, jButton25, jButton26, jButton27,
                                    jButton29, jButton30, jButton31, jButton32, jButton33,
                                    jButton35, jButton36, jButton37, jButton38, jButton39,jButton44};   
        baseButtons = new GradientButton[]{hexButton,decButton,octButton,binButton};
        solver = new Solver();
    }
    
    private void addGradientButtons(){
        hexButton = new GradientButton("  HEX 0");
        hexButton.addActionListener(evt->{
            currentBase = Base.HEX;
            deselectDigits(hexDeselect);
            deselectButtons(hexButton);
        });
        decButton = new GradientButton("  DEC 0");
        decButton.setSelected(true);
        decButton.addActionListener(evt->{
            currentBase = Base.DEC;
            deselectDigits(decDeselect);
            deselectButtons(decButton);
        });
        octButton = new GradientButton("  OCT 0");
        octButton.addActionListener(evt->{
            currentBase = Base.OCT;
            deselectDigits(octDeselect);  
            deselectButtons(octButton);
        });
        binButton = new GradientButton("  BIN 0");
        binButton.addActionListener(evt->{
            currentBase = Base.BIN;
            deselectDigits(binDeselect);
            deselectButtons(binButton);
        });
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(decButton, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
            .addComponent(octButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(binButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(hexButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hexButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(octButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(binButton))
        );
        pack();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextField1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculator");
        setBackground(new java.awt.Color(227, 227, 227));

        jPanel1.setBackground(new java.awt.Color(227, 227, 227));
        jPanel1.setToolTipText("");

        jButton1.setBackground(new java.awt.Color(227, 227, 227));
        jButton1.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\menu.png"));

        jLabel1.setBackground(new java.awt.Color(227, 227, 227));
        jLabel1.setFont(new java.awt.Font("Verdana", 0, 30)); // NOI18N
        jLabel1.setText("Programmer");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel2.setBackground(new java.awt.Color(227, 227, 227));
        jPanel2.setToolTipText("");

        jLabel2.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jScrollPane1.setBackground(new java.awt.Color(227, 227, 227));
        jScrollPane1.setBorder(null);

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(227, 227, 227));
        jTextField1.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.setText("0");
        jTextField1.setBorder(null);
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTextField1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(112, 112, 112))
        );

        jPanel3.setBackground(new java.awt.Color(227, 227, 227));
        jPanel3.setToolTipText("");

        jPanel4.setBackground(new java.awt.Color(227, 227, 227));
        jPanel4.setToolTipText("");
        jPanel4.setLayout(new java.awt.GridLayout());

        jButton6.setBackground(new java.awt.Color(227, 227, 227));
        jButton6.setBorder(null);
        jButton6.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\keyPad.png"));
        jPanel4.add(jButton6);

        jButton7.setBackground(new java.awt.Color(227, 227, 227));
        jButton7.setBorder(null);
        jButton7.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\bitToggle.png"));
        jPanel4.add(jButton7);

        jButton8.setBackground(new java.awt.Color(227, 227, 227));
        jButton8.setText(dataType.get(0));
        jButton8.setBorder(null);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton8);

        jButton9.setBackground(new java.awt.Color(227, 227, 227));
        jButton9.setText("MS");
        jButton9.setBorder(null);
        jPanel4.add(jButton9);

        jButton10.setBackground(new java.awt.Color(227, 227, 227));
        jButton10.setText("M+");
        jButton10.setBorder(null);
        jPanel4.add(jButton10);

        jPanel5.setBackground(new java.awt.Color(227, 227, 227));
        jPanel5.setToolTipText("");
        jPanel5.setLayout(new java.awt.GridLayout(6, 5, 2, 2));

        jButton11.setBackground(new java.awt.Color(227, 227, 227));
        jButton11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton11.setText("Lsh");
        jPanel5.add(jButton11);

        jButton12.setBackground(new java.awt.Color(227, 227, 227));
        jButton12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton12.setText("Rsh");
        jPanel5.add(jButton12);

        jButton13.setBackground(new java.awt.Color(227, 227, 227));
        jButton13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton13.setText("Or");
        jPanel5.add(jButton13);

        jButton14.setBackground(new java.awt.Color(227, 227, 227));
        jButton14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton14.setText("Xor");
        jPanel5.add(jButton14);

        jButton15.setBackground(new java.awt.Color(227, 227, 227));
        jButton15.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton15.setText("Not");
        jPanel5.add(jButton15);

        jButton16.setBackground(new java.awt.Color(227, 227, 227));
        jButton16.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton16.setText("And");
        jPanel5.add(jButton16);

        jButton17.setBackground(new java.awt.Color(227, 227, 227));
        jButton17.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton17.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\upArrow.png"));
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton17);

        jButton20.setBackground(new java.awt.Color(227, 227, 227));
        jButton20.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton20.setText("C");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton20);

        jButton19.setBackground(new java.awt.Color(227, 227, 227));
        jButton19.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton19.setText("CE");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton19);

        jButton18.setBackground(new java.awt.Color(227, 227, 227));
        jButton18.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton18.setText("Mod");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton18);

        jButton21.setBackground(new java.awt.Color(227, 227, 227));
        jButton21.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton21.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\backArrow.png"));
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton21);

        jButton22.setBackground(new java.awt.Color(227, 227, 227));
        jButton22.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton22.setText("/");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton22);

        jButton23.setBackground(new java.awt.Color(255, 255, 255));
        jButton23.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton23.setText("A");
        jButton23.setEnabled(false);
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton23);

        jButton24.setBackground(new java.awt.Color(255, 255, 255));
        jButton24.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton24.setText("B");
        jButton24.setEnabled(false);
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton24);

        jButton25.setBackground(new java.awt.Color(255, 255, 255));
        jButton25.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton25.setText("7");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton25);

        jButton26.setBackground(new java.awt.Color(255, 255, 255));
        jButton26.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton26.setText("8");
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton26);

        jButton27.setBackground(new java.awt.Color(255, 255, 255));
        jButton27.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton27.setText("9");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton27);

        jButton28.setBackground(new java.awt.Color(227, 227, 227));
        jButton28.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton28.setText("*");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton28);

        jButton29.setBackground(new java.awt.Color(255, 255, 255));
        jButton29.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton29.setText("C");
        jButton29.setEnabled(false);
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton29);

        jButton30.setBackground(new java.awt.Color(255, 255, 255));
        jButton30.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton30.setText("D");
        jButton30.setEnabled(false);
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton30);

        jButton31.setBackground(new java.awt.Color(255, 255, 255));
        jButton31.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton31.setText("4");
        jButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton31ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton31);

        jButton32.setBackground(new java.awt.Color(255, 255, 255));
        jButton32.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton32.setText("5");
        jButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton32ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton32);

        jButton33.setBackground(new java.awt.Color(255, 255, 255));
        jButton33.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton33.setText("6");
        jButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton33ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton33);

        jButton34.setBackground(new java.awt.Color(227, 227, 227));
        jButton34.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton34.setText("-");
        jButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton34ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton34);

        jButton35.setBackground(new java.awt.Color(255, 255, 255));
        jButton35.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton35.setText("E");
        jButton35.setEnabled(false);
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton35);

        jButton36.setBackground(new java.awt.Color(255, 255, 255));
        jButton36.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton36.setText("F");
        jButton36.setEnabled(false);
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton36);

        jButton37.setBackground(new java.awt.Color(255, 255, 255));
        jButton37.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton37.setText("1");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton37);

        jButton38.setBackground(new java.awt.Color(255, 255, 255));
        jButton38.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton38.setText("2");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton38);

        jButton39.setBackground(new java.awt.Color(255, 255, 255));
        jButton39.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton39.setText("3");
        jButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton39ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton39);

        jButton40.setBackground(new java.awt.Color(227, 227, 227));
        jButton40.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton40.setText("+");
        jButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton40ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton40);

        jButton41.setBackground(new java.awt.Color(227, 227, 227));
        jButton41.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton41.setText("(");
        jButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton41ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton41);

        jButton42.setBackground(new java.awt.Color(227, 227, 227));
        jButton42.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton42.setText(")");
        jButton42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton42ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton42);

        jButton43.setBackground(new java.awt.Color(227, 227, 227));
        jButton43.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton43.setIcon(new ImageIcon("C:\\Users\\eriko\\OneDrive\\Documents\\NetBeansProjects\\2336\\SemesterProject1\\src\\semesterproject1\\resources\\sign.png"));
        jButton43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton43ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton43);

        jButton44.setBackground(new java.awt.Color(255, 255, 255));
        jButton44.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton44.setText("0");
        jButton44.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton44ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton44);

        jButton45.setBackground(new java.awt.Color(227, 227, 227));
        jButton45.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton45.setText(".");
        jButton45.setEnabled(false);
        jPanel5.add(jButton45);

        jButton46.setBackground(new java.awt.Color(227, 227, 227));
        jButton46.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton46.setText("=");
        jButton46.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton46ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton46);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    //QWORD
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        jButton8.setText(dataType.get((dataType.indexOf(jButton8.getText())+1)%dataType.size()));
    }//GEN-LAST:event_jButton8ActionPerformed

    //0
    private void jButton44ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton44ActionPerformed
        updateInput("0");
    }//GEN-LAST:event_jButton44ActionPerformed

    //Negate
    private void jButton43ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton43ActionPerformed
        input *= -1;
        updateValues();
    }//GEN-LAST:event_jButton43ActionPerformed

    // )
    private void jButton42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton42ActionPerformed
        addParenthesis(")");
    }//GEN-LAST:event_jButton42ActionPerformed

    // (
    private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
        addParenthesis("(");
    }//GEN-LAST:event_jButton41ActionPerformed

    private void jButton39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton39ActionPerformed
        updateInput("3");
    }//GEN-LAST:event_jButton39ActionPerformed

    //2
    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        updateInput("2");
    }//GEN-LAST:event_jButton38ActionPerformed

    //1
    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        updateInput("1");
    }//GEN-LAST:event_jButton37ActionPerformed

    //F
    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
        updateInput("f");
    }//GEN-LAST:event_jButton36ActionPerformed

    //E
    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        updateInput("e");
    }//GEN-LAST:event_jButton35ActionPerformed

    // -
    private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
        updateEquation("-");
    }//GEN-LAST:event_jButton34ActionPerformed

    //6
    private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
        updateInput("6");
    }//GEN-LAST:event_jButton33ActionPerformed

    //5
    private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
        updateInput("5");
    }//GEN-LAST:event_jButton32ActionPerformed

    //3
    private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
        updateInput("4");
    }//GEN-LAST:event_jButton31ActionPerformed

    //D
    private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
        updateInput("d");
    }//GEN-LAST:event_jButton30ActionPerformed

    //C
    private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
        updateInput("c");
    }//GEN-LAST:event_jButton29ActionPerformed

    //9
    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        updateInput("9");
    }//GEN-LAST:event_jButton27ActionPerformed

    //8
    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        updateInput("8");
    }//GEN-LAST:event_jButton26ActionPerformed

    //7
    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        updateInput("7");
    }//GEN-LAST:event_jButton25ActionPerformed

    //B
    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        updateInput("b");
    }//GEN-LAST:event_jButton24ActionPerformed

    //A
    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        updateInput("a");
    }//GEN-LAST:event_jButton23ActionPerformed

    //Up Arrow
    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        int index = (leftOperator.indexOf(jButton11.getText())+1)%leftOperator.size();
        jButton11.setText(leftOperator.get(index));
        jButton12.setText(rightOperator.get(index));
    }//GEN-LAST:event_jButton17ActionPerformed

    // mod
    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        updateEquation("Mod");
    }//GEN-LAST:event_jButton18ActionPerformed

    //CE
    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        total = 0;
        clearInput();
    }//GEN-LAST:event_jButton19ActionPerformed

    //Clear All
    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        clearAll();
    }//GEN-LAST:event_jButton20ActionPerformed

    //Backspace
    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        deleteCharacter();
    }//GEN-LAST:event_jButton21ActionPerformed

    // /
    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        updateEquation("/");
    }//GEN-LAST:event_jButton22ActionPerformed

    //=
    private void jButton46ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton46ActionPerformed
        equalSign();
    }//GEN-LAST:event_jButton46ActionPerformed

    //*
    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        updateEquation("*");
    }//GEN-LAST:event_jButton28ActionPerformed

    //+
    private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed
        updateEquation("+");
    }//GEN-LAST:event_jButton40ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        String key = String.valueOf(evt.getKeyChar());
        if(key.matches("[a-f0-9]")){
            for(JButton b:digitButtons)
                if(b.isEnabled() && b.getText().equalsIgnoreCase(key))
                    updateInput(key.toLowerCase());
        }
        else if(key.matches("[\\(\\)]"))
            addParenthesis(key);
        else if(key.matches("([\\+\\-\\/\\*\\%])"))
            updateEquation(key.equals("%")?"Mod":key);
        else if(key.equals("="))
            equalSign();
        else{
            int code = evt.getKeyCode();
            if(code==8)
                deleteCharacter();
            else if(code==27)
                clearAll();
            else if(code==10)
                equalSign();           
        }
    }//GEN-LAST:event_jTextField1KeyPressed
   
    private void deleteCharacter(){
        String s = String.valueOf(input);
        s = s.substring(0, Math.max(s.length()-1,0));
        input = Integer.valueOf(s.length()==0?"0":s);
        beep.setEnabled(s.length()==0);
        updateValues();
    }
    
    private void clearAll(){
        clearInput();
        equation = new ArrayList();
        input = 0;
        total = 0;
        updateValues();
    }
    
    private void equalSign(){
        if(changed)
            equation.add(String.valueOf(prevInput = input));
        else
            completeEquation(equation);
        completeParenthesis(equation);
        solver.setEquation(compileEquation(equation,10));
        try{total = solver.fL();}
        catch(Exception e){}
        equation = new ArrayList();
        changed = false;
        input = total;
        updateValues();
    }
    
    private void completeEquation(ArrayList<String> array){
        if(!array.isEmpty()){
            String s = array.get(array.size()-1);
            if(s.matches("([\\+\\-\\/\\*]|(Mod))"))
                array.add(String.valueOf(total));
        }
        else if(currentOperation!=null){
            array.add(String.valueOf(total));
            array.add(currentOperation);
            array.add(String.valueOf(prevInput));
        }
    }
    
    private void completeParenthesis(ArrayList<String> array){
        int count = countParenthesis();
        for(int i=0;i<count;i++)
            array.add(")");
    }
    
    private void addParenthesis(String paren){
        if(paren.equals("("))
            equation.add(paren);
        else{
            if(countParenthesis()>0){
                if(changed)
                    equation.add(String.valueOf(input));
                equation.add(paren);
                ArrayList<String> temp = getCompleteParenthesis();
                if(temp!=null){
                    solver.setEquation(compileEquation(equation,10));
                    try{input = solver.fL();}
                    catch(Exception e){}
                }
            }
            changed=false;
        }
        updateValues();
    }
    
    private ArrayList<String> getCompleteParenthesis(){
        int open = -1;
        for(int i=0;i<equation.size();i++){
            if(equation.get(i).equals("("))
                open = i;
            if(equation.get(i).equals(")"))
                if(open>=0)
                    return new ArrayList(equation.subList(open, i));
        }
        return null;
    }
    
    private int countParenthesis(){
        int count = 0;
        for(String s: equation){
            if(s.equals("("))
                count++;
            else if(s.equals(")"))
                count--;
        }
        return count;
    }
    
    private void updateEquation(String operation){
        if(changed){
            equation.add(String.valueOf(input));
            solver.setEquation(compileEquation(equation,10));
            try{total = solver.fL();}
            catch(Exception e){}
            equation.add(operation);
            prevInput = input;
            input = 0;
        }
        else if(!equation.isEmpty())
            equation.set(equation.size()-1, operation);
        else{
            equation.add(String.valueOf(input));
            equation.add(operation);
            prevInput = input;
            input = 0;
        }            
        currentOperation = operation;
        changed = false;
        displayValue(total);
        displayEquation(equation);
        
    }
    
    private String compileEquation(ArrayList<String> array,int base){
        String eq = "";
        for(String s:array){
            if(!s.matches("([\\+\\-\\/\\*\\(\\)]|(Mod))"))
                s = convertBase(s,10,base);
            eq+=" "+s;
        }
        return eq;
    }
    
    private void deselectDigits(Integer[] digits){
        selectAllDigits();
        for(Integer i: digits)
            digitButtons[i].setEnabled(false);
        updateValues();
    }
    
    private void deselectButtons(GradientButton b){
        for(GradientButton g: baseButtons)
            if(!g.equals(b))
                g.setSelected(false);
    }
    
    private void selectAllDigits(){
        for(JButton b : digitButtons)
            b.setEnabled(true);
    }
    
    private void updateValues(){
        for(int i=0;i<4;i++){
            int base = bases[i];
            baseButtons[i].setText("  "+Base.values()[i].toString()+" "+convertBase(input,10,base));
        }            
        displayEquation(equation);
        displayValue(input);
    }
    
    private void displayEquation(ArrayList<String> array){
        jLabel2.setText(compileEquation(array,bases[currentBase.ordinal()]));
    }
    
    private void displayValue(long value){
        jTextField1.setText(convertBase(value,10,bases[currentBase.ordinal()]));
    }
    
    private void clearInput(){
        input = 0;
        updateValues();
    }
    
    private void updateInput(String value){
        if(!changed)
            input= 0;
        int base = bases[currentBase.ordinal()];
        input = Long.valueOf(convertBase(convertBase(input,10,base)+value,base,10));
        changed = true;
        updateValues();
    }
    
    private String convertBase(long number, int base1, int base2){
        return convertBase(String.valueOf(number),base1,base2);
    }
    
    private String convertBase(String number, int base1, int base2){
        try{return Long.toString(Integer.parseInt(String.valueOf(number), base1), base2);}
        catch(Exception e){return number;}
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
