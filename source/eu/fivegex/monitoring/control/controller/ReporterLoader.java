/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.reservoir.monitoring.core.ControllableReporter;
import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 *
 * @author uceeftu
 */
public final class ReporterLoader implements Serializable { // to be refactored using an Interface
    
    String reporterClassName;
    
    Class<?> clazz;
    Class<? extends ControllableReporter> reporterClazz;
    Constructor<? extends ControllableReporter> cons0;
    Object [] constructorParameters;
    
    ControllableReporter reporter;
    
    
    
    public ReporterLoader(String reporterClassName, Object ... params) throws Exception {
        this.reporterClassName = reporterClassName;
        constructorParameters = params;
        
        initReporter();
    }
    
    
    public void initReporter() throws Exception {
        try {
            System.out.println("Loading Class: " + reporterClassName);
            clazz = Class.forName(reporterClassName);
            // check if the class implements the right interface
            reporterClazz = clazz.asSubclass(ControllableReporter.class);
            
            // we build an array with the Class types of the provided Parameters
            Class [] paramsTypes = new Class[constructorParameters.length];
            
            for (int i=0; i<constructorParameters.length; i++)
                paramsTypes[i]=constructorParameters[i].getClass();
            
            cons0 = reporterClazz.getConstructor(paramsTypes);
            
            // create an instance of the Probe
            reporter = cons0.newInstance(constructorParameters);
            
            }  catch (Exception ex) {
                System.out.println("Exception in ReporterLoader " + ex.getCause().getMessage());
                throw new Exception(ex);
            }   
    }

    public ControllableReporter getReporter() {
        return reporter;
    }    
    
    
}
