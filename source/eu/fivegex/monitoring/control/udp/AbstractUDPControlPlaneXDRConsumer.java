/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;


import eu.fivegex.monitoring.control.ControlPlaneConsumerException;
import eu.fivegex.monitoring.control.ControlServiceException;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.ControlOperation;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.core.plane.MessageType;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.XDRDataInputStream;
import eu.reservoir.monitoring.distribution.XDRDataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneXDRConsumer extends AbstractUDPControlPlaneConsumer {

    
    public AbstractUDPControlPlaneXDRConsumer(InetSocketAddress localAddress) {
        super(localAddress);
    }
    
    
    public AbstractUDPControlPlaneXDRConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
    }
   
    @Override
    public abstract boolean announce();
    

    @Override
    public abstract boolean dennounce();
    

    protected void announceSerializer(AbstractAnnounceMessage message) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);

        // write type
        dataOutput.writeInt(message.getMessageType().getValue());      

        // write entity type value as int
        dataOutput.writeInt(message.getEntity().getValue());

        // write entity ID 
        dataOutput.writeLong(message.getEntityID().getMostSignificantBits());
        dataOutput.writeLong(message.getEntityID().getLeastSignificantBits());

        if (udpAt != null) {
            LOGGER.info("Sending " + message.getMessageType() + " message for " + message.getEntity() + " " + message.getEntityID());
            udpAt.transmit(byteStream, 0);
        }
    }

    
    @Override
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, ReflectiveOperationException {
        ControlOperation ctrlOperationName=null;
        ControlPlaneReplyMessage replyMessage = null;
        
        int seqNo = -1;
        
	try {
	    DataInput dataIn = new XDRDataInputStream(bis);

	    // check replyMessage type
	    int type = dataIn.readInt();            
	    MessageType mType = MessageType.lookup(type);

	    // delegate read to right object
	    if (mType == null) {
		throw new IOException("Message type is null");
	    }

            else if (mType == MessageType.CONTROL) {
                LOGGER.debug("-------- Control Message Received ---------");
                
                String ctrlOperationMethod = dataIn.readUTF();
                ctrlOperationName = ControlOperation.lookup(ctrlOperationMethod);
                
                // get source replyMessage sequence number
                seqNo = dataIn.readInt();
                
                LOGGER.debug("Operation String: " + ctrlOperationName);
                LOGGER.debug("Operation Method: " + ctrlOperationMethod);
                LOGGER.debug("Source Message ID: " + seqNo);
                
                byte [] args = new byte[8192];
                dataIn.readFully(args);

                List<Object> methodArgs;

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(args));
                methodArgs = (ArrayList<Object>) ois.readObject();
                ois.close();
                
                Method methodToInvoke = null;
                for (Method method: this.getClass().getMethods()) {
                    if (method.getName().equals(ctrlOperationMethod)) {
                        methodToInvoke = method;
                        break;
                    }
                }
                
                if (methodToInvoke != null) {
                    Object result = methodToInvoke.invoke(this, methodArgs.toArray());
                    replyMessage = new ControlPlaneReplyMessage(result, ctrlOperationName, seqNo);
                    //transmitReply(replyMessage, metaData);
                }
                else
                    throw new NoSuchMethodException("A suitable Control Service method to invoke has not been found");
	    }
            

        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException ex) {
                Exception exceptionToSend = new ControlPlaneConsumerException(ex);
                Throwable wrappedException = ex.getCause();
                
                // a ControlServiceException may be wrapped within InvocationTargetException coming from the 
                // reflection invocation 
                if (wrappedException instanceof ControlServiceException) 
                    // we further unwrap the cause from the ControlServiceException and put it in a ControlPlaneConsumerException
                    exceptionToSend = new ControlPlaneConsumerException(wrappedException.getCause());
                
                replyMessage = new ControlPlaneReplyMessage(exceptionToSend, ctrlOperationName, seqNo);
                throw new ReflectiveOperationException(exceptionToSend.getCause()); // we also throw the exception locally unwrapping it from ControlPlaneException 
        } finally {
            if (replyMessage != null)
                transmitReply(replyMessage, metaData);
            else
                LOGGER.warn("the received message was not a Control Message"); 
          }
    }

    
    @Override
    public int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws IOException {
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);
        
        //write the replyMessage type (i.e. ControlReply)
        dataOutput.writeInt(answer.getType().getValue());
        
        // writing replyMessage ID
        int sourceMessageSeqNo = answer.getReplyToMessageID();
        dataOutput.writeInt(sourceMessageSeqNo);
        
        // write method operation this is an answer for
        dataOutput.writeUTF(answer.getControlOperation().getValue());

        // write result Object 
        dataOutput.write(answer.getPayloadAsByte());
        
        int sendReply = udpReceiver.sendMessage(byteStream);
        
        return sendReply;
    }
}
