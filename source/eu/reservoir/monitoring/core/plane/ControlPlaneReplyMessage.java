/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * @author uceeftu
 */
public class ControlPlaneReplyMessage {
    private final ControlOperation methodName;
    private final Object Payload;
    private final ID replyToMessageID;
    private final MessageType type;

    public ID getReplyToMessageID() {
        return replyToMessageID;
    }
    
    public ControlPlaneReplyMessage(Object Payload, ControlOperation m, ID replyToMessageID) {
        type = MessageType.CONTROL_REPLY;
        methodName = m;
        this.Payload = Payload; 
        this.replyToMessageID =replyToMessageID;
    }
    
     public byte[] getPayloadAsByte() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(Payload);
        byte[] bytes = bos.toByteArray();
        return bytes;
        }
    
     public ControlOperation getControlOperation() {
        return methodName;
    }

    public MessageType getType() {
        return type;
    }
}
