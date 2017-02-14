/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

import java.util.ArrayList;

/**
 *
 * @author ostlinja
 */
public class Matrix {
    int rows,columns;
    double[][] contents;
    
    public Matrix()
    {
        rows = 0;
        columns = 0;
        contents = new double[0][0];
    }
    public Matrix(int a,int b)
    {
        rows = a;
        columns = b;
        contents = new double[rows][columns];
    }
    public Matrix(int a,int b,double[][] c)
    {
        rows = a;
        columns = b;
        contents = c;
    }
    
    public int getNumRows(){return rows;}
    public int getNumColumns(){return columns;}
    public double[][] getContents(){return contents;}
    
    public double[][] getRows(){return getContents();}
    public double[][] getColumns()
    {
        double[][] res = new double[rows][columns];
        for(int c = 0;c<columns;c++)
        {
            for(int r = 0;r<rows;r++)
            {
                res[c][r] = contents[r][c];
            }
        }
        return res;
    }
    
    public void setRows(int r)
    {
        rows = r;
        checkContents();
    }
    
    public void setColumns(int c)
    {
        columns = c;
        checkContents();
    }
    
    public double get(int r,int c)
    {
        if(checkCoordinates(r,c))
            return contents[r][c];
        else
            return 0;
    }
    
    public void set(double v,int r, int c)
    {   
        if(checkCoordinates(r,c))  
            contents[r][c] = v;
    }
    
    public void fill(double v)
    {
        contents = new double[rows][columns];
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
                contents[r][c] = v;
        }
    }
    
    public void add(Matrix m)throws Exception
    {
        if(checkDimensions(m))
        {
            for(int r = 0;r<rows;r++)
            {
                for(int c = 0;c<columns;c++)
                {
                    set(get(r,c)+m.get(r, c),r,c);
                }
            }
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public void add(double v)
    {
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
            {
                set(get(r,c)+v,r,c);
            }
        }
    }
    
    public void multiply(double v)
    {
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
            {
                set(get(r,c)*v,r,c);
            }
        }
    }
    
    public void multiply(Matrix m)throws Exception
    {
        if(checkDimensions(m))
        {
            for(int r = 0;r<rows;r++)
            {
                for(int c = 0;c<columns;c++)
                {
                    set(get(r,c)*m.get(r, c),r,c);
                }
            }
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public Matrix dot(Matrix m) throws Exception 
    {
        Matrix temp;
        if(checkDimensions(m,m.columns,rows))
        {
            temp = new Matrix(Math.min(rows, m.rows),Math.min(columns, m.columns));
            for(int r = 0;r<rows;r++)
            {
                for(int c = 0;c<columns;c++)
                {
                    temp.set(sum(getRows()[r],m.getColumns()[c]),r,c);
                }
            }
            return temp;
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public double determinate()
    {
        return 0;
    }
    
    private double sum(double[] a,double[] b)
    {
        double res = 0.0;
        for(int i=0;i<a.length;i++)
            res+=a[i]*b[i];
        return res;
    }
    
    private void checkContents()
    {
        double[][] temp = new double[rows][columns];
        for(int r = 0;r<rows;r++)
            for(int c = 0;c<columns;c++)
                if(r<contents.length && c<contents[0].length)
                    temp[r][c] = contents[r][c];
                else
                    temp[r][c] = 0;
        contents = temp.clone();
    }
    
    private boolean checkCoordinates(int r,int c)
    {
        return rows>=r && columns>=c;
    }
    
    private boolean checkDimensions(Matrix m)
    {
        return rows==m.rows && columns==m.columns;
    }
    
    private boolean checkDimensions(Matrix m,int r,int c)
    {
        return m.rows==r && m.columns==c;
    }
    
    private boolean checkDimensions(Matrix m,int[] r,int[] c)
    {
        for(int i=0;i<r.length;i++)
            if(m.rows==r[i] && m.columns==c[i])
                return true;
        return false;
    }
    
    public String toString()
    {
        String res = "";
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
            {
                res += contents[r][c]+" ";
            }
            res += "\n";
        }
        return res;
    }
}
