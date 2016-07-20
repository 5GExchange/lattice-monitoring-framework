/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public final class JSONProbeCatalogue extends AbstractProbeCatalogue {
    JSONObject probeCatalogue;
    
    public JSONProbeCatalogue(String probesPackage) {
        super(probesPackage);
        this.probeCatalogue = new JSONObject();
        
    }
    
    
    public void generateProbesCatalogue() throws JSONException {
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
    
    
    public JSONObject getProbeCatalogue() {
        return probeCatalogue;
    }
    
    
    public static void main (String[] args) throws ClassNotFoundException, IOException, JSONException {
        JSONProbeCatalogue c = new JSONProbeCatalogue("eu.fivegex.demo.probes");
    
        c.searchForProbes();
        c.generateProbesCatalogue();
        System.out.println(c.getProbeCatalogue().toString(5));
    }
    
    
}
