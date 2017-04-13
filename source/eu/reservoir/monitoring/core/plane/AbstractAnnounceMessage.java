/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.ID;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractAnnounceMessage implements Serializable {
    
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
    
    public static String toString(AbstractAnnounceMessage o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
    
    public static AbstractAnnounceMessage fromString(String s) throws IOException ,
                                                       ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return (AbstractAnnounceMessage) o;
   }
    
    
    
}
