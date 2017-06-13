/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mathinterpreter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class GraphPane extends java.awt.Canvas{
    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    Equation equation;
    Range xrange;
    Range yrange;
    Range selected;
    
    public GraphPane(){
        this(new Equation());
    }
    
    public GraphPane(Equation e){
        this(e,new Range(-10,10),new Range(-10,10));
    }
    
    public GraphPane(Equation e,Range x,Range y){
        super();
        equation = e;
        xrange = x;
        yrange = y;
    }

    @Override
    public void paint(Graphics g) {
        System.out.println("test");
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (xrange.length());
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (yrange.length());

        ArrayList<DPoint> graphPoints = new ArrayList();
        for (double i = xrange.start; i < xrange.end; i+=0.1) {
            double x1 = i * xScale + padding;
            double y1 = (equation.fD(x1)) * yScale + padding;
            graphPoints.add(new DPoint(x1, y1));
        }

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i <= yrange.length() + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / yrange.length() + padding + labelPadding);
            int y1 = y0;
            g2.setColor(gridColor);
            g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
            g2.setColor(Color.BLACK);
            //String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
            String yLabel = i+"";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < xrange.length(); i++) {
            int x0 = i * (getWidth() - padding * 2 - labelPadding) / (xrange.length()) + padding + labelPadding;
            int x1 = x0;
            int y0 = getHeight() - padding - labelPadding;
            int y1 = y0 - pointWidth;
            g2.setColor(gridColor);
            g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
            g2.setColor(Color.BLACK);
            String xLabel = i + "";
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(xLabel);
            g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
            
            g2.drawLine(x0, y0, x1, y1);
            
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = (int)graphPoints.get(i).x;
            int y1 = (int)graphPoints.get(i).y;
            int x2 = (int)graphPoints.get(i + 1).x;
            int y2 = (int)graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (int i = 0; i < graphPoints.size(); i++) {
            int x = (int)graphPoints.get(i).x - pointWidth / 2;
            int y = (int)graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
   }
}

class DPoint extends Point2D{
    double x;
    double y;
    public DPoint(double x,double y){
        this.x = x;
        this.y = y;
    }
    
    public void move(double x,double y){
        setLocation(x,y);
    }
    
    public double getX(){return x;}
    
    public double getY(){return y;}
    
    public void setLocation(double x,double y){
        this.x = x;
        this.y = y;
    }
}