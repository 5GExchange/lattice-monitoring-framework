/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

//import java.util.ArrayList;
import eu.reservoir.monitoring.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;



public class ControlPlaneMessage extends DataPlaneMessage {
    private ControlOperation methodName;
    private List<Object> methodArgs;
    private ID messageID;
    
    public ControlPlaneMessage(ControlOperation m, List<Object> args) {
        type = MessageType.CONTROL;
        methodName = m;
        methodArgs = args;
        messageID = ID.generate();
    } 

    public ID getMessageID() {
        return messageID;
    }
    
    public ControlOperation getControlOperation() {
        return methodName;
    }
        
    public List<Object> getMethodArgs() {
            return methodArgs;
        }
    
    
    public byte[] getMethodArgsAsByte() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(methodArgs);
        byte[] bytes = bos.toByteArray();
        return bytes;
        }
    }