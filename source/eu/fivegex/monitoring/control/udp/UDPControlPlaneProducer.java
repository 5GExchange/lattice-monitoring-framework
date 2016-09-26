/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.controller.InfoResolver;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.ControlPlaneMessage;
import eu.reservoir.monitoring.core.plane.ControlOperation;
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


public class UDPControlPlaneProducer extends AbstractUDPControlPlaneProducer {

    /*
    public UDPControlPlaneProducer(InetSocketAddress address, InfoResolver resolver) {
        super(address, resolver);
    }
    */
    
    public UDPControlPlaneProducer(InfoResolver resolver) {
        super(resolver);
    }
    

    @Override
    public Object transmit(ControlPlaneMessage cpMessage, MetaData metadata) throws Exception {
        System.out.println("UDPControlPlaneProducer.transmit " + cpMessage.toString() + " to " + metadata);
        Object result=null;
        
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);


        // write type
        dataOutput.writeInt(cpMessage.getType().getValue());            

        // write object
        dataOutput.writeUTF(cpMessage.getControlOperation().getValue());            

        // writing message ID
        ID messageID = cpMessage.getMessageID();
        dataOutput.writeLong(messageID.getMostSignificantBits());
        dataOutput.writeLong(messageID.getLeastSignificantBits());


        // convert args to byte          
        dataOutput.write(cpMessage.getMethodArgsAsByte());

        switch (cpMessage.getType()) {
            case CONTROL:
                System.out.println("--------- Type Control ----------");
                System.out.println("Message ID: " + messageID);
                SyncUDPTransmitterAndReceiver udpTransmitterAndReceiver = new SyncUDPTransmitterAndReceiver(this);
                result = udpTransmitterAndReceiver.transmitAndWaitReply(byteStream, ((UDPControlTransmissionMetaData)metadata).getInetSocketAddress(), 0);
                //udpTransmitterAndReceiver.transmit(byteStream, ((UDPControlTransmissionMetaData)metadata).getInetSocketAddress(), 0);
                if (result instanceof Exception) 
                    throw new Exception((Exception)result); 
                
                break;
        }
    return result;    
    }

    @Override
    public Object receivedReply(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        Object result=null;
        
        try {
	    DataInput dataIn = new XDRDataInputStream(bis);
            
	    // check message type
	    int type = dataIn.readInt();            
	    MessageType mType = MessageType.lookup(type);

            //System.out.println("Received Type : ---------> " + type);
            
	    // delegate read to right object
	    if (mType == null) {
		//System.err.println("type = " + type);
		return null;
	    }
            
	    switch (mType) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;
       
            case CONTROL_REPLY:
                System.out.println("-------- Control Reply Message Received ---------");
                
                // get the Message id
                long messageIDMSB = dataIn.readLong();
                long messageIDLSB = dataIn.readLong();
                ID sourceMessageID = new ID(messageIDMSB, messageIDLSB);
                //System.out.println("This is a reply to Message: " + sourceMessageID);
                
                String ctrlOperation = dataIn.readUTF();
                ControlOperation ctrlOperationName = ControlOperation.lookup(ctrlOperation);

                //System.out.println("Operation code: " + ctrlOperationName);
                
                byte [] args = new byte[4096];
                dataIn.readFully(args);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(args));
                result = (Object) ois.readObject();
                ois.close();
                
                System.out.println("Received REPLY message for request ID " + sourceMessageID + " and Operation code " + ctrlOperationName + ": " + result.toString());
                
                
                /*System.out.println("FT: UDPControlPlaneConsumer.received - Received " + type + ". mType " + mType + 
                        " operation code " + ctrlOperation + " ctrl operation name " + ctrlOperationName);

                System.out.println("FT: UDPControlPlaneConsumer.received - Received args:");
                
                for (Object o : methodArgs) {
                    System.out.println(o.toString() + " ");
                }*/    
                
            }         
        }
        catch (Exception exception) {
            System.out.println("Error while reading result from reply message: " + exception.getMessage());
        }
        return result;
    }
    
    
    /* DS Control Service methods */

    @Override
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws Exception { //we also need the DS id on which the probe will be loaded
        List<Object> args = new ArrayList();
        args.add(dataSourceID);
        args.add(probeClassName);
        args.add(probeArgs);
        
        ID probeID = null;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.LOAD_PROBE, args);
        
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromID(dataSourceID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            //we return the ID of the new created probe as result
            probeID = (ID) transmit(m, mData);
        } //catch (DSNotFoundException idEx) {
          //  throw idEx; 
          //}
          catch (Exception ex) {
            System.out.println("Error while sending load probe command " + ex.getMessage());
            throw ex;
          }
        return probeID;
    }

    @Override
    public boolean unloadProbe(ID probeID) throws Exception {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.UNLOAD_PROBE, args);
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } //catch (ProbeIDNotFoundException idEx) {
          //  throw idEx; 
          //}
          catch (Exception ex) {
            System.out.println("Error while sending unload probe command " + ex.getMessage());
            throw ex;
          }
        
        return result;
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
    public boolean setProbeServiceID(ID probeID, ID id) throws Exception {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(id);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_SERVICE_ID, args);
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } //catch (ProbeIDNotFoundException idEx) {
          //  throw idEx; 
          //}
          catch (Exception ex) {
            System.out.println("Error while sending set probe service ID command " + ex.getMessage());
            throw ex;
          }
        
        return result;
        
    }

    @Override
    public ID getProbeGroupID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeGroupID(ID probeID, ID id) throws Exception {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(id);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_GROUP_ID, args);
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } //catch (ProbeIDNotFoundException idEx) {
          //  throw idEx; 
          //}
          catch (Exception ex) {
            System.out.println("Error while sending set probe group ID command " + ex.getMessage());
            throw ex;
          }
        
        return result;    
    }

    @Override
    public Rational getProbeDataRate(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeDataRate(ID probeID, Rational dataRate) throws Exception {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(dataRate);
        Boolean result = false;

        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_DATA_RATE, args);
        
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } 
          catch (Exception ex) {
            System.out.println("Error while sending turn on probe command " + ex.getMessage());
            throw ex;
          }
        
        return result;
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
    public boolean turnOnProbe(ID probeID) throws Exception{
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.TURN_ON_PROBE, args);
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } //catch (ProbeIDNotFoundException idEx) {
          //  throw idEx; 
          //}
          catch (Exception ex) {
            System.out.println("Error while sending turn on probe command " + ex.getMessage());
            throw ex;
          }
        
        return result;
        }

    @Override
    public boolean turnOffProbe(ID probeID) throws Exception {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.TURN_OFF_PROBE, args);
        try {
            InetSocketAddress dstAddr = resolver.getDSAddressFromProbeID(probeID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } //catch (ProbeIDNotFoundException idEx) {
           // throw idEx;
          //} 
          catch (Exception ex) {
            System.out.println("Error while sending turn off probe command " + ex.getMessage());
            throw ex;
          }
        
        return result;
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

    @Override
    public boolean transmitted(int id) {
        return true;
    }
    
    
    /* DC Control Service methods */
    
    @Override
    public float getDCMeasurementsRate(ID dcId) throws Exception {
        List<Object> args = new ArrayList();
        args.add(dcId);
        
        Float rate;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_DC_RATE, args);
        
        try {
            InetSocketAddress dstAddr = resolver.getDCAddressFromID(dcId);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());            
            rate = (Float) transmit(m, mData);
        } catch (Exception ex) {
            System.out.println("Error while sending getDCMeasurementsRate command " + ex.getMessage());
            throw ex;
          }
        return rate;
    }

    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws Exception {
        List<Object> args = new ArrayList();
        args.add(dataConsumerID);
        args.add(reporterClassName);
        args.add(reporterArgs);
        
        ID reporterID = null;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.LOAD_REPORTER, args);
        
        try {
            InetSocketAddress dstAddr = resolver.getDCAddressFromID(dataConsumerID);
            MetaData mData = new UDPControlTransmissionMetaData(dstAddr.getAddress(), dstAddr.getPort());
            //we return the ID of the new created reporter as result
            reporterID = (ID) transmit(m, mData);
        } catch (Exception ex) {
            System.out.println("Error while sending loadReporter command " + ex.getMessage());
            throw ex;
          }
        return reporterID;
    }
    
}
