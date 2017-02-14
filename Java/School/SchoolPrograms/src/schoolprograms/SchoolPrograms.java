/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schoolprograms;

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 *
 * @author erik
 */
public class SchoolPrograms
{

    private static String path = "C:\\Users\\Erik Ostlind\\Documents\\NetBeansProjects\\SchoolPrograms\\dist\\SchoolPrograms.jar";
    static HomeGUI GUI = null;
    public static void main(String[] args)
    {
        
        initializeData();
    }
    
    public static void initializeData()
    {
        ArrayList<SchoolProgramsCommon> classes = getExternalWorkloads(path);
        GUI = new HomeGUI(classes);
        GUI.pack();
        GUI.setVisible(true);
    }
    public static ArrayList<SchoolProgramsCommon> getExternalWorkloads(String jarName)
    {
        ArrayList<SchoolProgramsCommon> myClasses = new ArrayList<SchoolProgramsCommon>();
        JarInputStream jarFile = null;
        JarEntry jarEntry = null;
        try
        {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            while (true)
            {
                jarEntry = jarFile.getNextJarEntry();

                if (jarEntry == null)
                {
                    break;
                }
                if (jarEntry.getName().endsWith(".class"))
                {
                    String classname = jarEntry.getName().replaceAll("/", "\\.");
                    classname = classname.substring(0, classname.length() - 6);
                    if (classname.contains("$") == false)
                    {
                        ClassLoader.getSystemClassLoader();
                        URL url = new URL("jar:file:" + jarName + "!/");
                        URLClassLoader ucl = new URLClassLoader(new URL[]{url});
                        try
                        {
                            //System.out.println(">"+classname);
                            Class<SchoolProgramsCommon> myLoadedClass = (Class<SchoolProgramsCommon>) ucl.loadClass(classname);
                            SchoolProgramsCommon myClass = (SchoolProgramsCommon) myLoadedClass.newInstance();
                            myClasses.add(myClass);
                            
                        } catch (Exception e)
                        {
                            //System.out.println("Sub> "+e.getMessage());
                        }
                    }
                }
            }

            jarFile.close();
        } catch (Exception e)
        {
            System.out.println("Main> "+e.getMessage());
        }

        return myClasses;
    }
    public String getPath()
    {
        return path;
    }
    public void setPath(String p)
    {
        path = p;
        GUI.setVisible(false);
        initializeData();
    }

}
