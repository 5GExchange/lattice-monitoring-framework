/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.im.delegate;

import eu.reservoir.monitoring.core.ID;

/**
 *
 * @author uceeftu
 */
public class ZMQControlEndPointMetaData extends ControlEndPointMetaData {
    private ID id;

    
    public ZMQControlEndPointMetaData(String type, ID id) {
        super(type);
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public ID getId() {
        return id;
    }
}
