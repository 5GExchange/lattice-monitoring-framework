/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.ID;
import java.util.EnumSet;
import java.util.HashMap;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractAnnounceMessage {
    
    protected ID entityID;
    protected MessageType messageType; // subclass will set this either to Announce or Deannounce
    protected EntityType entity;

    public AbstractAnnounceMessage(ID id, EntityType entity, MessageType messageType) {
        this.entityID = id;
        this.entity = entity;
        this.messageType = messageType;
    }

    public ID getEntityID() {
        return entityID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public EntityType getEntity() {
        return entity;
    }
    
    public enum EntityType {
        DATASOURCE(1),
        DATACONSUMER(2);
        
        private static final HashMap<Integer, EntityType> lookup = new HashMap<>();
        private Integer entityValue;
        
        public static final EntityType lookup(Integer mt) {
            return lookup.get(mt);
        }
        
        static {
	for(EntityType t : EnumSet.allOf(EntityType.class)) { 
            lookup.put(t.getValue(), t);
            }
        }
        
        private EntityType(Integer value) {
            entityValue = value;
        }
        public Integer getValue() {
            return entityValue;   
        }
        
    }
    
}
