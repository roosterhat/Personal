/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package games;

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
public class Games
{
    private static String path = "dist\\Games.jar";
    static HomeGUI GUI = null;
    public static void main(String[] args)
    {
        new Games().initializeData();
    }
    
    public void initializeData()
    {
        ArrayList<GamesCommon> classes = getExternalWorkloads(path);
        GUI = new HomeGUI(classes,this);
        GUI.pack();
        GUI.setVisible(true);
    }
    
    public void refreshData()
    {
        GUI.setVisible(false);
        initializeData();
    }
    
    public static ArrayList<GamesCommon> getExternalWorkloads(String jarName)
    {
        ArrayList<GamesCommon> myClasses = new ArrayList<GamesCommon>();
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
                            //System.out.println("[]"+classname);
                            Class<GamesCommon> myLoadedClass = (Class<GamesCommon>) ucl.loadClass(classname);
                            GamesCommon myClass = (GamesCommon) myLoadedClass.newInstance();
                            myClasses.add(myClass);
                            System.out.println(">"+myClass.getName());
                            
                        } catch (Exception e)
                        {
                            System.out.println("Sub> "+e.getMessage());
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
