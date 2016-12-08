/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.ID;



public class DeannounceMessage extends AbstractAnnounceMessage {
    public DeannounceMessage(ID id, EntityType e) {
        super(id, e, MessageType.DEANNOUNCE);
    }
}