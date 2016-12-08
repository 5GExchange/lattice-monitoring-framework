/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;


public class DataSourceInfo extends LatticeEntityInfo {

    public DataSourceInfo(String className, String args) {
        super(EntityType.DATASOURCE, className, args);
        this.id = ID.generate();
    }
    
}
