/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

//import java.util.ArrayList;
import eu.reservoir.monitoring.core.ID;
import java.util.EnumSet;
import java.util.HashMap;



public class AnnounceMessage {
    private final ID entityID;
    private final MessageType messageType;
    private EntityType entity;
    //time?
    
    public AnnounceMessage(ID id, EntityType e) {
        this.messageType=MessageType.ANNOUNCE;
        this.entity=e;
        this.entityID = id;
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