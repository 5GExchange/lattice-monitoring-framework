/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class ProbeCatalogue {
    //Map <String, List<String>> probeCatalogue;
    String probesPackage;
    public JSONObject probeCatalogue;
    List <Class> probeClasses;
    
    

    public ProbeCatalogue(String probesPackage) {
        this.probesPackage = probesPackage;
        this.probeCatalogue = new JSONObject();
        this.probeClasses = new ArrayList <>();
        
    }
    
    
    public void generateCatalogueAsJSON() throws JSONException {
        for (Class cl : probeClasses) {   
            JSONObject probeInfo = new JSONObject();
            //System.out.println(cl.getName());
            Constructor [] cons = cl.getConstructors();
            int i=1;
            for (Constructor constructor : cons) {
                JSONObject constructorInfo = new JSONObject();
                //System.out.println(constructor.getName());
                Parameter [] params = constructor.getParameters();
                for (Parameter p : params) {
                    constructorInfo.append("parameterstype", p.getParameterizedType().getTypeName());
                    constructorInfo.append("parametersname", p.getName());
                    //System.out.println(p.getParameterizedType().getTypeName());
                    //System.out.println(p.getName());
                }
            probeInfo.put("classname", cl.getName());
            probeInfo.put("contructor" + i++, constructorInfo);
            }
        probeCatalogue.put(cl.getSimpleName(), probeInfo);         
        }  
    }
    
    
    private void generateCatalogue() throws ClassNotFoundException, IOException {
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
    
    public JSONObject getProbeCatalogue() {
        return probeCatalogue;
    }
   
    public static void main (String[] args) throws ClassNotFoundException, IOException, JSONException {
        ProbeCatalogue c = new ProbeCatalogue("eu.fivegex.demo.probes");
        
        c.generateCatalogue();
        c.generateCatalogueAsJSON();
        System.out.println(c.getProbeCatalogue().toString(5));
    }
}
