/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matricies;

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
        fill(0);
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
        double[][] res = new double[columns][rows];
        for(int c = 0;c<columns;c++)
            for(int r = 0;r<rows;r++)
                res[c][r] = contents[r][c];
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
    
    public void resize(int r,int c)
    {
        setRows(r);
        setColumns(c);
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
    
    public void set(double[][] v)
    {
        contents = v.clone();
    }
    
    public void fill(double v)
    {
        contents = new double[rows][columns];
        for(int r = 0;r<rows;r++)
            for(int c = 0;c<columns;c++)
                contents[r][c] = v;
    }
    
    public void add(Matrix m)throws Exception
    {
        if(checkDimensions(m))
        {
            for(int r = 0;r<rows;r++)
                for(int c = 0;c<columns;c++)
                    set(get(r,c)+m.get(r, c),r,c);
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public void add(double v)
    {
        for(int r = 0;r<rows;r++)
            for(int c = 0;c<columns;c++)
                set(get(r,c)+v,r,c);
    }
    
    public void multiply(double v)
    {
        for(int r = 0;r<rows;r++)
            for(int c = 0;c<columns;c++)
                set(get(r,c)*v,r,c);
    }
    
    public void multiply(Matrix m)throws Exception
    {
        if(checkDimensions(m))
        {
            for(int r = 0;r<rows;r++)
                for(int c = 0;c<columns;c++)
                    set(get(r,c)*m.get(r, c),r,c);
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public Matrix dot(Matrix m) throws Exception 
    {
        Matrix temp;
        if(columns==m.rows)
        {
            temp = new Matrix(rows, m.columns);
            for(int r = 0;r<rows;r++)
                for(int c = 0;c<m.columns;c++)
                    temp.set(sum(getRows()[r],m.getColumns()[c]),r,c);  
            return temp;
        }
        else
        {
            throw new Exception("Matrix Dimensions do not match");
        }
    }
    
    public void transpose()
    {
        Matrix temp = new Matrix(columns,rows);
        for(int r=0;r<rows;r++)
            for(int c=0;c<columns;c++)
                temp.set(get(r,c), c, r);
        int r = rows;
        rows = columns;
        columns = r;
        set(temp.getContents());
    }
    
    public double determinate()
    {
        double det = 1;
        Matrix m = clone();
        if(rows==columns)
        {
            for(int r=0;r<m.rows;r++)
            {
                double old = m.get(r,r);
                m = invertValue(m,r);
                m = negateValuesLower(m,r);
                Matrix id = newIdentity(m.rows);
                id.set(old,r,r);
                try{m = id.dot(m);}catch(Exception e){System.out.println(e);}
            }
            for(int r=0;r<m.rows;r++)
            {
                det *= m.get(r,r);
            }
        }
        return round(det);
    }
   
    public Matrix GaussJordan()
    {
        Matrix m = clone();
        for(int r=0;r<rows;r++)
        {
            if(m.get(r, r)==0)
                m = pivotMatrix(m,r);
            
            m = invertValue(m,r);
            m = negateValuesFull(m,r);
        }
        m.roundAll();
        return m;
    }
    
    private Matrix invertValue(Matrix m,int row)
    {
        Matrix id = newIdentity(m.rows);
        id.set(1/m.get(row, row),row,row);
        try{m = id.dot(m);}catch(Exception e){System.out.println("InverValue Failed\n"+e);}
        return m;
    }
    
    private Matrix negateValuesFull(Matrix m,int row)
    {
        Matrix id = newIdentity(m.rows);
        for(int i=0;i<rows;i++)
            if(i!=row)
                id.set(-1*m.get(i, row), i, row);
        try{m = id.dot(m);}catch(Exception e){System.out.println("NegateValues Failed\n"+e);}
        return m;
    }
    
    private Matrix negateValuesUpper(Matrix m,int row)
    {
        Matrix id = newIdentity(m.rows);
        for(int i=0;i<row;i++)
            if(i!=row)
                id.set(-1*m.get(i, row), i, row);
        try{m = id.dot(m);}catch(Exception e){System.out.println("NegateValues Failed\n"+e);}
        return m;
    }
    
    private Matrix negateValuesLower(Matrix m,int row)
    {
        Matrix id = newIdentity(m.rows);
        for(int i=row;i<rows;i++)
            if(i!=row)
                id.set(-1*m.get(i, row), i, row);
        try{m = id.dot(m);}catch(Exception e){System.out.println("NegateValues Failed\n"+e);}
        return m;
    }
    
    private Matrix pivotMatrix(Matrix m,int row)
    {
        Matrix tempID = newIdentity(m.rows);
        double[] t = tempID.getRows()[row];
        for(int r=row;r<m.rows;r++)
        {
            if(m.get(r, r)!=0)
            {
                tempID.contents[row] = tempID.contents[r];
                tempID.contents[r] = t;
                try{m = tempID.dot(m);return m;}catch(Exception e){System.out.println("PivotMatrix Failed\n"+e);}
            }
        }
        return m;
    }
    
    public Matrix newIdentity(int r)
    {
        return newIdentity(r,r);
    }
    
    public Matrix newIdentity(int r, int c)
    {
        Matrix m = new Matrix(r,c);
        for(int i=0;i<r;i++)
            if(i<m.columns)
                m.set(1, i, i);
        return m;
    }
    
    private double sum(double[] a,double[] b)
    {
        double res = 0.0;
        for(int i=0;i<a.length;i++)
            res+=a[i]*b[i];
        return res;
    }
    
    public void roundAll()
    {
        for(int r=0;r<rows;r++)
            for(int c=0;c<columns;c++)
                set(round(get(r,c)),r,c);
    }
    
    public double round(double d)
    {
        return Double.parseDouble(String.format("%.2f",d));
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
    
    private int getLongestValue()
    {
        int longest = 0;
        for(double[] l:contents)
        {
            for(double v:l)
            {
                String s = Double.toString(v).split("[.]")[0];
                longest = Math.max(longest, s.length());
            }
        }
        return longest;
    }
    
    public String toString()
    {
        String f = "%- "+(getLongestValue()+3)+".2f ";
        String res = "";
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
            {
                res += String.format(f, contents[r][c]);
            }
            res += "\n";
        }
        System.out.println(res);
        return res;
    }
    
    public String toHtml()
    {
        return toHtml(2);
    }
    
    public String toHtml(int dec)
    {
        String[] colors = {"#C0C0C0","#FFFFFF"};
        String f = "%-"+(getLongestValue()+dec+2)+"."+dec+"f";
        String res = "";
        for(int r = 0;r<rows;r++)
        {
            for(int c = 0;c<columns;c++)
            {
                res += "<span style=\"background-color:"+colors[(r+c)%2]+";\">"+
                        String.format(f, contents[r][c]).replaceAll("\\s", ((char)160)+"")+"</span>";
            }
            res += "<p>";
        }
        return res+"";
    }
    
    public Matrix clone()
    {
        double[][] c = new double[rows][columns];
        for(int a=0;a<rows;a++)
            for(int b=0;b<columns;b++)
                c[a][b] = contents[a][b];
        return new Matrix(rows,columns,c);
    }
}
