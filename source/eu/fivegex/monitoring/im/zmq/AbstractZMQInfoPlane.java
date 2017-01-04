package eu.fivegex.monitoring.im.zmq;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DHTInfoPlane is an InfoPlane implementation
 * that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractZMQInfoPlane implements InfoPlane  {
    
    
    
    static Logger LOGGER = LoggerFactory.getLogger("AbstractZMQInfoPlane");
    
    public AbstractZMQInfoPlane() {
    }
    
    

    
    
    // lookup some info in the InfoPlane
//    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
//	return zmqInformationProducer.getDataSourceInfo(dataSource.getID(), info);
//    }
//
//    // lookup some info in the InfoPlane
//    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
//	return zmqInformationProducer.getDataSourceInfo(dataSourceID, info);
//    }
//
//    // lookup some info in the InfoPlane
//    public Object lookupProbeInfo(Probe probe, String info) {
//	return zmqInformationProducer.getProbeInfo(probe.getID(), info);
//    }
//
//    // lookup some info in the InfoPlane
//    public Object lookupProbeInfo(ID probeID, String info) {
//	return zmqInformationProducer.getProbeInfo(probeID, info);
//    }
//
//    // lookup some info in the InfoPlane
//    public Object lookupProbeAttributeInfo(Probe probe, int field, String info) {
//	return zmqInformationProducer.getProbeAttributeInfo(probe.getID(), field, info);
//    }
//
//    // lookup some info in the InfoPlane
//    public Object lookupProbeAttributeInfo(ID probeID, int field, String info) {
//	return zmqInformationProducer.getProbeAttributeInfo(probeID, field, info);
//    }
//    
//    @Override
//    public Object lookupDataConsumerInfo(ID dataConsumerID, String info) {
//        return zmqInformationProducer.getDataConsumerInfo(dataConsumerID, info);
//    }
//    
//    @Override
//    public Object lookupReporterInfo(ID reporterID, String info) {
//        return zmqInformationProducer.getReporterInfo(reporterID, info);
//    }
//    
//
//    /**
//     * Put a value in the InfoPlane.
//     */
//    public boolean putInfo(String key, Serializable value) {
//	return zmqInformationProducer.sendInfo(key, value.toString());
//    }
//
//    /**
//     * Get a value from the InfoPlane.
//     */
//    public Object getInfo(String key) {
//	return zmqInformationProducer.getInfo(key);
//    }
//
//    /**
//     * Remove a value from the InfoPlane.
//     */
//    public boolean removeInfo(String key) {
//	return zmqInformationProducer.remInfo(key);
//    }
//    
//    public String toString() {
//        return zmqInformationProducer.toString();
//    }
//    

    @Override
    public boolean putInfo(String key, Serializable value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getInfo(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeInfo(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
}