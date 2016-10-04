/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.core.*;
import eu.fivegex.monitoring.control.ProbeLoader;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.AnnounceMessage.EntityType;
import eu.reservoir.monitoring.core.plane.ControlOperation;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.core.plane.MessageType;
import eu.reservoir.monitoring.core.plane.DataSourceControlPlane;
import eu.reservoir.monitoring.core.plane.DeannounceMessage;
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




public class UDPDataSourceControlPlaneConsumer extends AbstractUDPControlPlaneConsumer implements DataSourceControlPlane, DataSourceDelegateInteracter, TransmittingAnnounce {
    DataSourceDelegate dataSourceDelegate;
    
    public UDPDataSourceControlPlaneConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
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
        //address and port to send to as parameters taken from metadata
        //System.out.println("TransmitReply - received metadata: " + metadata);
        
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

    @Override
    public boolean announce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        System.out.println("UDP Control Plane Consumer - Announcing Data Source");
        
        AnnounceMessage message = new AnnounceMessage(dataSource.getID(), EntityType.DATASOURCE);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            System.out.println("Error while announcing Data Source" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean dennounce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        System.out.println("UDP Control Plane Consumer - Deannouncing Data Source");
        
        DeannounceMessage message = new DeannounceMessage(dataSource.getID(), EntityType.DATASOURCE);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            System.out.println("Error while deannouncing Data Source" + e.getMessage());
            return false;
        }
    }
    
    
     @Override
    public void error(Exception e) {
        System.err.println("ControPlaneConsumer error: " + e.getMessage());
    }

    @Override
    public boolean transmitted(int id) {
        System.out.println("Just announced DS");
        return true;
    }
    
    
    @Override
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    @Override
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	System.err.println("DataSource Control Plane Consumer: setDataSource: " + ds);
	dataSourceDelegate = ds;
	return ds;
    }
    
    
    @Override
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws Exception {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        try {
            System.out.println("******* UDPControlPlaneConsumer -> loadProbe");
            ProbeLoader p = new ProbeLoader(probeClassName, probeArgs);
            if (dataSource instanceof ControllableDataSource)
                return ((ControllableDataSource)dataSource).addProbe(p);
            else
                throw new Exception("Probe cannot be loaded on the DS");
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public boolean unloadProbe(ID probeID) throws Exception {
        DataSource dataSource = dataSourceDelegate.getDataSource();
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
        DataSource dataSource = dataSourceDelegate.getDataSource();
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
        DataSource dataSource = dataSourceDelegate.getDataSource();
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
        DataSource dataSource = dataSourceDelegate.getDataSource();
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
        DataSource dataSource = dataSourceDelegate.getDataSource();
        System.out.println("******* UDPControlPlaneConsumer -> turnOnProbe");
        if (!dataSource.isProbeOn(probeID)) {
            dataSource.turnOnProbe(probeID);
            return true;
        }
        else 
            return true;
    }

    @Override
    public boolean turnOffProbe(ID probeID) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        System.out.println("******* UDPControlPlaneConsumer -> turnOffProbe");
        if (dataSource.isProbeOn(probeID)) {
            dataSource.turnOffProbe(probeID);
            return true;
        }
        else
            return false;
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
