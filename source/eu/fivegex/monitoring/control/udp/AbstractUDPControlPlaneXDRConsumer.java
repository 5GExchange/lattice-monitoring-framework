/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;


import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.ControlOperation;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.core.plane.MessageType;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.ReceivingAndReplying;
import eu.reservoir.monitoring.distribution.XDRDataInputStream;
import eu.reservoir.monitoring.distribution.XDRDataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneXDRConsumer extends AbstractUDPControlPlaneConsumer implements ControlPlane, ReceivingAndReplying, TransmittingAnnounce {

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

        udpAt.transmit(byteStream, 0);
    }

    
    @Override
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        ControlOperation ctrlOperationName=null;
        int seqNo = -1;
        
	try {
	    DataInput dataIn = new XDRDataInputStream(bis);

	    // check message type
	    int type = dataIn.readInt();            
	    MessageType mType = MessageType.lookup(type);

	    // delegate read to right object
	    if (mType == null) {
		throw new IOException("Message type is null");
	    }

            else if (mType == MessageType.CONTROL) {
                System.out.println("-------- Control Message Received ---------");
                
                String ctrlOperationMethod = dataIn.readUTF();
                ctrlOperationName = ControlOperation.lookup(ctrlOperationMethod);
                
                // get source message sequence number
                seqNo = dataIn.readInt();
                
                System.out.println("Operation String: " + ctrlOperationName);
                System.out.println("Operation Method: " + ctrlOperationMethod);
                System.out.println("Source Message ID: " + seqNo);
                
                byte [] args = new byte[4096];
                dataIn.readFully(args);

                List<Object> methodArgs;

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(args));
                methodArgs = (ArrayList<Object>) ois.readObject();
                ois.close();
                
                /* possible implementation based on reflections */
                
                Method methodToInvoke = null;
                for (Method method: this.getClass().getMethods()) {
                    //System.out.println(method.getName());
                    if (method.getName().equals(ctrlOperationMethod)) {
                        methodToInvoke = method;
                        break;
                    }
                }
                     
                /* Some Debug output
                System.out.println(methodToInvoke.getName());
                
                for (Class c : methodToInvoke.getParameterTypes())
                    System.out.println(c);
                
                for (Object o: methodArgs) System.out.println(methodArgs);
                */
                
                Object result = methodToInvoke.invoke(this, methodArgs.toArray());
                ControlPlaneReplyMessage message = new ControlPlaneReplyMessage(result, ctrlOperationName, seqNo);
                transmitReply(message, metaData);
	    }

        } catch (Exception ex) {
                ControlPlaneReplyMessage errorMessage = new ControlPlaneReplyMessage(ex.getCause(), ctrlOperationName, seqNo);
                try {
                    transmitReply(errorMessage, metaData);
                } catch (Exception ex1) {
                    System.out.println("ControPlaneConsumer error - failed to transmit control error message: " + ex1.getMessage());
                }
                
                System.out.println("ControPlaneConsumer error: " + ex.getCause().getMessage());
                throw new IOException(ex.getCause().getMessage());
                
            
        }
    }

    
    @Override
    public int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws Exception {
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);
        
        //write the message type (i.e. ControlReply)
        dataOutput.writeInt(answer.getType().getValue());
        
        // writing message ID
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
