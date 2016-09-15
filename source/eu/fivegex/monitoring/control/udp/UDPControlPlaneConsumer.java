/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeLoader;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.core.TypeException;
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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;



public class UDPControlPlaneConsumer extends AbstractUDPControlPlaneConsumer {
    
    public UDPControlPlaneConsumer(InetSocketAddress address) {
        super(address);
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
                
                byte [] args = new byte[2048];
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
                ControlPlaneReplyMessage errorMessage = new ControlPlaneReplyMessage((Object)ex, ctrlOperationName, sourceMessageID);
                try {
                    transmitReply(errorMessage, metaData);
                } catch (Exception ex1) {
                    System.err.println("ControPlaneConsumer (exception detected): failed to transmit control error message: " + ex1.getMessage());
                }
                
                System.err.println("ControPlaneConsumer (exception detected): failed to process control message: " + ex.getMessage());
                throw new IOException(ex.getMessage());
                
            
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
        
        int sendReply = udpReceiver.sendReply(byteStream);
        
        return sendReply;
    }

    @Override
    public boolean announce() {
        // creating a message to announce the IP address of the DS control endpoint
        return true;
        }
    
    
     @Override
    public void error(Exception e) {
        System.err.println("ControPlaneConsumer - invoked error method : failed to process control message: " + e.getMessage());
    }

    
    @Override
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws Exception {
        try {
            System.out.println("******* UDPControlPlaneConsumer -> loadProbe");
            ProbeLoader p = new ProbeLoader(probeClassName, probeArgs);
            if (dataSource instanceof ControllableDataSource)
                return ((ControllableDataSource)dataSource).addProbe(p);
            else
                throw new Exception("Probe cannot be loaded on that DS");
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public boolean unloadProbe(ID probeID) throws Exception {
        System.out.println("******* UDPControlPlaneConsumer -> unloadProbe");
        Probe p = dataSource.getProbeByID(probeID);
        dataSource.removeProbe(p);
        return true;
    }
    

    @Override
    public String getProbeName(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeName(ID probeID, String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ID getProbeServiceID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeServiceID(ID probeID, ID id) {
        System.out.println("******* UDPControlPlaneConsumer -> setProbeServiceID");
        dataSource.setProbeServiceID(probeID, id);
        return true;
        }

    @Override
    public ID getProbeGroupID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeGroupID(ID probeID, ID id) {
        System.out.println("******* UDPControlPlaneConsumer -> setProbeGroupID");
        dataSource.setProbeGroupID(probeID, id);
        return true;
    }

    @Override
    public Rational getProbeDataRate(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeDataRate(ID probeID, Rational dataRate) {
        System.out.println("******* UDPControlPlaneConsumer -> setProbeDataRate");
        dataSource.setProbeDataRate(probeID, dataRate);
        return true;
    }

    @Override
    public Measurement getProbeLastMeasurement(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Timestamp getProbeLastMeasurementCollection(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean turnOnProbe(ID probeID) {
        System.out.println("******* UDPControlPlaneConsumer -> turnOnProbe");
        dataSource.turnOnProbe(probeID);
        return true;
    }

    @Override
    public boolean turnOffProbe(ID probeID) {
        System.out.println("******* UDPControlPlaneConsumer -> turnOffProbe");
        dataSource.turnOffProbe(probeID);
        return true;
    }

    @Override
    public boolean isProbeOn(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean activateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deactivateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isProbeActive(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDataSourceName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setDataSourceName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
