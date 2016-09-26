/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.ID;
import eu.fivegex.monitoring.control.controller.ProbeLoader;

/**
 *
 * @author uceeftu
 */
public class ControllableBasicDataSource extends BasicDataSource implements ControllableDataSource {
    
   public ControllableBasicDataSource (String dsName) {
       super(dsName);
   }        
            
   @Override
   public ID addProbe(ProbeLoader p) {  
        addProbe(p.getProbe());
        return p.getProbe().getID();
    } 
}
