/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.controller.InformationManager;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
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
    
    public UDPControlPlaneProducer(InformationManager resolver, int port, int maxPoolSize) {
        super(resolver, port, maxPoolSize);
    }
    

    @Override
    public Object transmit(ControlPlaneMessage cpMessage, MetaData metadata) throws Exception {
        Object result=null;
        
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);


        // write type
        dataOutput.writeInt(cpMessage.getType().getValue());            

        // write object
        dataOutput.writeUTF(cpMessage.getControlOperation().getValue());            

        // writing message seqNo
        int seqNo = cpMessage.getSequenceNumber();
        dataOutput.writeInt(seqNo);

        // convert args to byte          
        dataOutput.write(cpMessage.getMethodArgsAsByte());
        
        System.out.println("\n--------- Sending Control Message with seqNo: " + seqNo + " ----------");

        // getting a Transmitter from the Pool
        UDPTransmitterSyncReply connection = controlTransmittersPool.getConnection();
        result = connection.transmitAndWaitReply(byteStream, (UDPControlMetaData)metadata, seqNo);
        
        // putting the Transmitter back to the Pool
        controlTransmittersPool.releaseConnection(connection);
        
        if (result instanceof Exception) 
            throw new Exception((Exception)result);        
    return result;    
    }

    // called when a control reply message is received
    @Override
    public Object receivedReply(ByteArrayInputStream bis, MetaData metaData, int seqNo) throws IOException, TypeException, ClassNotFoundException {
        Object result=null;
        
        DataInput dataIn = new XDRDataInputStream(bis);

        // check message type
        int type = dataIn.readInt();            
        MessageType mType = MessageType.lookup(type);

        if (mType == null) {
            throw new IOException("Message type is null");
        }

        else if (mType == MessageType.CONTROL_REPLY) {
                System.out.println("\n-------- Control Reply Message Received ---------");
                System.out.println("From: " + metaData);
                
                int replyMessageSeqNo = dataIn.readInt();

                String ctrlOperation = dataIn.readUTF();
                ControlOperation ctrlOperationName = ControlOperation.lookup(ctrlOperation);

                byte [] args = new byte[4096];
                dataIn.readFully(args);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(args));
                result = (Object) ois.readObject();
                ois.close();

                if (replyMessageSeqNo == seqNo)
                    System.out.println("Request seqNo: " + replyMessageSeqNo + "\nOperation: " + ctrlOperationName + "\nResult: " + result.toString());
                else
                    // we should not likely arrive here
                    throw new IOException("Message Sequence number mismatch! " + replyMessageSeqNo + " not equal to " + seqNo);
        }
        return result;
    }

    
    
    @Override
    // called when an Announce/Deannounce Message is received
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException {
        try {
	    DataInput dataIn = new XDRDataInputStream(bis);
            
	    // check message type
	    int type = dataIn.readInt();            
	    MessageType mType = MessageType.lookup(type);
            
	    if (mType == null) {
                throw new Exception("Message type is null");
	    }
            
            else if (mType == MessageType.ANNOUNCE || mType == MessageType.DEANNOUNCE) {
                    Integer e = dataIn.readInt();
                    EntityType entity = EntityType.lookup(e);

                    long entityIDMSB = dataIn.readLong();
                    long entityIDLSB = dataIn.readLong();
                    ID entityID = new ID(entityIDMSB, entityIDLSB);
                    if (mType == MessageType.ANNOUNCE) {
                        System.out.println("\n-------- Announce Message Received ---------");
                        addNewAnnouncedEntity(entityID, entity);
                    }
                    else {
                        System.out.println("\n-------- Dennounce Message Received ---------");
                        removeNewDeannouncedEntity(entityID, entity);
                    }
                        
                }         
        }
        catch (Exception exception) {
            System.out.println("Error while reading Announce/Deannounce message: " + exception.getMessage());
        }
        
    }

    @Override
    public void eof() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void error(Exception e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean transmitted(int id) {
        System.out.println("Transmitted Control Message with seqNo: " + id);
        return true;
    }
    
    @Override
    public void addNewAnnouncedEntity(ID entityID, EntityType type) {
        System.out.println("New " + type + " with ID " + entityID);
        try {
            Thread.sleep(1000); //needed to wait for the DHT to be updated
        } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
        }
        resolver.addNewAnnouncedEntity(entityID, type);
    }

    @Override
    public void removeNewDeannouncedEntity(ID entityID, EntityType type) {
        System.out.println(type + " with ID " + entityID + " is being shutdown");
        try {
            Thread.sleep(1000); //needed to wait for the DHT to be updated
        } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
        }
        resolver.removeNewDeannouncedEntity(entityID, type);
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            //we return the ID of the new created probe as result
            probeID = (ID) transmit(m, mData);
        } catch (Exception ex) {
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } catch (Exception ex) {
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } catch (Exception ex) {
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } catch (Exception ex) {
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } catch (Exception ex) {
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
            return result;
        } catch (Exception ex) {
            System.out.println("Error while sending turn off probe command " + ex.getMessage());
            throw ex;
          }
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
    
    
    /* DC Control Service methods */
    
    @Override
    public float getDCMeasurementsRate(ID dcId) throws Exception {
        List<Object> args = new ArrayList();
        args.add(dcId);
        
        Float rate;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_DC_RATE, args);
        
        try {
            InetSocketAddress dstAddr = resolver.getDCAddressFromID(dcId);
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());            
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
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            //we return the ID of the new created reporter as result
            reporterID = (ID) transmit(m, mData);
        } catch (Exception ex) {
            System.out.println("Error while sending loadReporter command " + ex.getMessage());
            throw ex;
          }
        return reporterID;
    }
    
    
    @Override
    public boolean unloadReporter(ID reporterID) throws Exception {
        List<Object> args = new ArrayList();
        args.add(reporterID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.UNLOAD_REPORTER, args);
        try {
            InetSocketAddress dstAddr = resolver.getDCAddressFromReporterID(reporterID);
            MetaData mData = new UDPControlMetaData(dstAddr.getAddress(), dstAddr.getPort());
            result = (Boolean) transmit(m, mData);
        } catch (Exception ex) {
            System.out.println("Error while sending unload reporter command " + ex.getMessage());
            throw ex;
          }
        return result;
    }
    
}
