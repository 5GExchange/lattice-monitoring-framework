/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractProbeCatalogue {
    String probesPackage;
    Set <Class> probeClasses;
    

    public AbstractProbeCatalogue(String probesPackage) {
        this.probesPackage = probesPackage.replace(".", "/");
        this.probeClasses = new HashSet <>();
        
    }
    
    public void searchForProbesInDirectory() throws ClassNotFoundException, IOException {
        System.out.println("Search for Probes in directories:");
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = cld.getResources(this.probesPackage);

        while (resources.hasMoreElements()) {
            String path = resources.nextElement().getPath();
            File directory = new File(URLDecoder.decode(path, "UTF-8"));
            System.out.println(directory);

            if (directory.exists()) {
                String[] files = directory.list();
                directory.listFiles();
                for (String file : files) {
                    if (file.endsWith(".class")) {
                        Class entryClass = Class.forName(this.probesPackage.replace("/", ".") + "." + file.substring(0, file.length() - 6));
                        probeClasses.add(entryClass);
                        System.out.println(entryClass.getName());
                    }
                }
            }
        }
    }
    
    
    public void searchForProbesInJars() throws ClassNotFoundException, IOException {
        System.out.println("Search for Probes in jars");
        String jarPath; 
        URL[] jarURLArray = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        
        for (URL u : jarURLArray)
            System.out.println(u.getPath());
        
        if (jarURLArray == null) {
            throw new ClassNotFoundException("Can't get class loader.");
        }
        else 
            jarPath = jarURLArray[0].getPath(); // we assume we have the current jar only in the array

        JarFile jarFile = null;

        jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> en = jarFile.entries();
        while (en.hasMoreElements()) {
            JarEntry entry = en.nextElement();
            String entryName = entry.getName();
            // although directories are hierarchical java packages shouldn't be: looking inside p1.p2.p3 should not look into p1.p2.p3.p4
            if (entryName != null && entryName.matches(probesPackage + "/[^/|\\.]*\\.class")) {
                    //System.out.println("entryName: "  + entryName);
                    Class entryClass = Class.forName(entryName.substring(0, entryName.length() - 6).replace("/", "."));
                    if (entryClass != null) {
                        System.out.println(entryClass.getName());
                        probeClasses.add(entryClass);
                    }
            }
        }    
    }
    
}
