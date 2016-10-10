/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control;

import eu.reservoir.monitoring.core.Probe;
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
    
    
    
    public ProbeLoader(String probeClassName, Object ... params) throws ProbeLoaderException {
        this.probeClassName = probeClassName;
        constructorParameters = params;
        
        initProbe();
    }
    
    
    public void initProbe() throws ProbeLoaderException {
        try {
            System.out.println("Loading Class: " + probeClassName);
            clazz = Class.forName(probeClassName);
            // check if the class implements the right interface
            probeClazz = clazz.asSubclass(Probe.class);
            
            // we build an array with the Class types of the provided Parameters
            Class [] paramsTypes = new Class[constructorParameters.length];
            
            for (int i=0; i<constructorParameters.length; i++)
                paramsTypes[i]=constructorParameters[i].getClass();
            
            cons0 = probeClazz.getConstructor(paramsTypes);
            
            // create an instance of the Probe
            probe = cons0.newInstance(constructorParameters);
            
            } catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException ex) {
                throw new ProbeLoaderException(ex);
            } catch (InvocationTargetException itex) {
                    throw new ProbeLoaderException(itex.getCause());
            }
    }

    public Probe getProbe() {
        return probe;
    }    
    
    
}
