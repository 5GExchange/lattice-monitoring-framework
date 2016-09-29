/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.controller.ReporterLoader;
import eu.reservoir.monitoring.core.DataConsumer;
import eu.reservoir.monitoring.core.DataConsumerInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.ControlOperation;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.core.plane.DataConsumerControlPlane;
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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;



public class UDPDataConsumerControlPlaneConsumer extends AbstractUDPControlPlaneConsumer implements DataConsumerControlPlane, DataConsumerInteracter, TransmittingAnnounce {
    DataConsumer dataConsumer;
    
    public UDPDataConsumerControlPlaneConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
    }

    @Override
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {

        ControlOperation ctrlOperationName=null;
        ID sourceMessageID=null;
                
	try {
	    DataInput dataIn = new XDRDataInputStream(bis);

	    //System.err.println("DC: datainputstream available = " + dataIn.available());

	    // check message type
	    int type = dataIn.readInt();            
	    MessageType mType = MessageType.lookup(type);

	    // delegate read to right object
	    if (mType == null) {
		//System.err.println("type = " + type);
		return;
	    }

	    // get seq no
	    //int seq = dataIn.readInt();
            
	    // Message meta data
	    //MessageMetaData msgMetaData = new MessageMetaData(ControlEntityID, seq, mType);

	    // read object and check it's type
	    switch (mType) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;
       
            case CONTROL:
                System.out.println("-------- Control Message Received ---------");
                
                String ctrlOperationMethod = dataIn.readUTF();
                ctrlOperationName = ControlOperation.lookup(ctrlOperationMethod);

                // get the Message ID
                long messageIDMSB = dataIn.readLong();
                long messageIDLSB = dataIn.readLong();
                sourceMessageID = new ID(messageIDMSB, messageIDLSB);
                
                System.out.println("Operation String: " + ctrlOperationName);
                System.out.println("Operation Method: " + ctrlOperationMethod);
                System.out.println("Source Message ID: " + sourceMessageID);
                
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
                ControlPlaneReplyMessage message = new ControlPlaneReplyMessage(result, ctrlOperationName, sourceMessageID);
                transmitReply(message, metaData);
	    }

        } catch (Exception ex) {
                ControlPlaneReplyMessage errorMessage = new ControlPlaneReplyMessage(ex.getCause(), ctrlOperationName, sourceMessageID);
                try {
                    transmitReply(errorMessage, metaData);
                } catch (Exception ex1) {
                    System.out.println("ControPlaneConsumer error - failed to transmit control error message: " + ex1.getMessage());
                }
                
                System.out.println("ControPlaneConsumer error - " + ex.getCause().getMessage());
                throw new IOException(ex.getCause().getMessage());
                
            
        }
    }
    
    @Override
    public int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws Exception {
        //address and port to send to as parameters taken from metadata
        //System.out.println("TransmitReply - received metadata: " + metadata);
        
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);
        
        //write the message type (i.e. ControlReply)
        dataOutput.writeInt(answer.getType().getValue());
        
        // writing message ID
        ID sourceMessageID = answer.getReplyToMessageID();
        dataOutput.writeLong(sourceMessageID.getMostSignificantBits());
	dataOutput.writeLong(sourceMessageID.getLeastSignificantBits());
        
        // write method operation this is an answer for
        dataOutput.writeUTF(answer.getControlOperation().getValue());

        // write result Object 
        dataOutput.write(answer.getPayloadAsByte());
        
        int sendReply = udpReceiver.sendMessage(byteStream);
        
        return sendReply;
    }

    @Override
    public boolean announce() {
        // creating a message to announce the ID and IP localAddress of the DC control endpoint
        System.out.println("UDP Control Plane Consumer - Announcing Data Consumer");
        
        AnnounceMessage message = new AnnounceMessage(dataConsumer.getID(), AnnounceMessage.EntityType.DATACONSUMER);
        
        try {
            // convert the object to a byte []
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutput dataOutput = new XDRDataOutputStream(byteStream);

            // write type
            dataOutput.writeInt(message.getMessageType().getValue());      

            
            System.out.println("Entity " + message.getEntity());
            // write entity type value as int
            dataOutput.writeInt(message.getEntity().getValue());
            
            // write entity ID 
            dataOutput.writeLong(dataConsumer.getID().getMostSignificantBits());
            dataOutput.writeLong(dataConsumer.getID().getLeastSignificantBits());
            
            UDPAnnounceTransmitter udpAt = new UDPAnnounceTransmitter(this, controllerAddress);
            udpAt.transmit(byteStream, 0);
            return true;
        
        } catch (IOException e) {
            System.out.println("Error while announcing " + e.getMessage());
            return false;
        }
        }
    
    
    @Override
    public void error(Exception e) {
        System.err.println("ControPlaneConsumer - invoked error method : failed to process control message: " + e.getMessage());
    }
    
    
    @Override
    public boolean transmitted(int id) {
        System.out.println("Just announced DC");
        return true;
    }
    
    

    @Override
    public DataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public DataConsumer setDataConsumer(DataConsumer dc) {
        this.dataConsumer = dc;
        return this.dataConsumer;
    }  
    
    @Override
    public float getDCMeasurementsRate(ID dcID) {
        System.out.println("******* UDPControlPlaneConsumer -> getDCMeasurementsRate");
        return dataConsumer.getMeasurementsRate();
    }

    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws Exception {
        System.out.println("******* UDPControlPlaneConsumer -> loadReporter");
        ReporterLoader r = new ReporterLoader(reporterClassName, reporterArgs);
        dataConsumer.addReporter(r.getReporter());
        return r.getReporter().getId();
    }

    @Override
    public boolean unloadReporter(ID reporterID) throws Exception {
        System.out.println("******* UDPControlPlaneConsumer -> loadReporter");
        dataConsumer.removeReporter(dataConsumer.getReporterById(reporterID));
        return true;
    }
}
