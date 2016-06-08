/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author uceeftu
 */
public final class ProbeLoader implements Serializable {
    
    String probeClassName;
    
    Class<?> clazz;
    Class<? extends Probe> probeClazz;
    Constructor<? extends Probe> cons0;
    Object [] constructorParameters;
    
    Probe probe;
    
    
    
    public ProbeLoader(String probeClassName, Object ... params) throws Exception {
        this.probeClassName = probeClassName;
        constructorParameters = params;
        
        initProbe();
    }
    
    
    public void initProbe() throws Exception {
        try {
            clazz = Class.forName(probeClassName);
            // check if the class implements the right interface
            Class<? extends Probe> appClazz = clazz.asSubclass(Probe.class);
            
            // we build an array with the Class types of the provided Parameters
            Class [] paramsTypes = new Class[constructorParameters.length];
            
            for (int i=0; i<constructorParameters.length; i++)
                paramsTypes[i]=constructorParameters[i].getClass();
            
            cons0 = appClazz.getConstructor(paramsTypes);
            
            // create an instance of the Probe
            probe = cons0.newInstance(constructorParameters);
            
            } catch (ClassCastException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
                // it is not a Probe, so we can't run it
                throw new Exception(ex);
            }   
    }

    public Probe getProbe() {
        return probe;
    }    
    
    
}
