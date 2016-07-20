/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractProbeCatalogue {
    String probesPackage;
    List <Class> probeClasses;
    
    

    public AbstractProbeCatalogue(String probesPackage) {
        this.probesPackage = probesPackage;
        this.probeClasses = new ArrayList <>();
        
    }
    
    
    
    public void searchForProbes() throws ClassNotFoundException, IOException {
        String jarPath; 
        URL[] jarURLArray = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
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
                probesPackage = probesPackage.replace(".", "/");
                if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(probesPackage)) {
                        //System.out.println("entryName: "  + entryName);
                        Class entryClass = Class.forName(entryName.substring(0, entryName.length() - 6).replace("/", "."));
                        if (entryClass != null) {
                            probeClasses.add(entryClass);
                        }
                }
            }    
    }
    
}
