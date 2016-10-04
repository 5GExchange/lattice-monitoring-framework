/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AnnounceMessage.EntityType;
import eu.reservoir.monitoring.distribution.Receiving;

/**
 *
 * @author uceeftu
 */
interface ReceivingAnnounce extends Receiving {
    public void addNewAnnouncedEntity(ID entityID, EntityType type);
    
    public void removeNewDeannouncedEntity(ID entityID, EntityType type);
}
