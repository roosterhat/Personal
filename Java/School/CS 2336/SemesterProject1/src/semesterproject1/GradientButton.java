/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import javax.swing.JToggleButton;

/**
 *
 * @author eriko
 */
public class GradientButton extends JToggleButton{
    public GradientButton(String text){
        super(text);
        setContentAreaFilled(false);
        setBackground(new java.awt.Color(227, 227, 227));
        setFont(new Font("Monospaced", Font.BOLD, 16));
        setBorder(null);
        setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    }
    protected void paintComponent(Graphics g) {
        if(this.isSelected()){
            final Graphics2D g2 = (Graphics2D) g.create();
            int mid = getHeight()/2;
            float[] dist = {0.0f, 0.01f};
            Point2D start = new Point2D.Float(0, mid);
            Point2D end = new Point2D.Float(getWidth(), mid);
            Color[] colors = {Color.BLUE,getBackground()};
            g2.setPaint(new LinearGradientPaint(start,end,dist,colors));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
