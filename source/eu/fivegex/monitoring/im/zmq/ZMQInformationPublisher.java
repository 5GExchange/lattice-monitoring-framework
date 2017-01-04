package eu.fivegex.monitoring.im.zmq;

import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import java.io.IOException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQInformationPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQInformationPublisher {
    String remoteHost;
    int remotePort = 0;
    
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket publisher = context.socket(ZMQ.PUB);
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQInformationPublisher.class);

    /**
     * Construct an InfoFormatter, given a remote Consumer host
     * and a remote port.
     */
    public ZMQInformationPublisher(String remHost, int remPort) {
	remoteHost = remHost;
	remotePort = remPort;
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connect() {
        String uri = "tcp://" + remoteHost + ":" + remotePort;
        publisher.connect(uri);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {   
            }
        return true;
    }

    /**
     * Disconnect from the DHT peers.
     */
    public boolean disconnect() {
        publisher.close();
        context.term();
        return true;
    }

    public String getRootHostname() {
        return this.remoteHost;
    }
    
//    public ZMQInformationPublisher addDataConsumer(ControllableDataConsumer dc) throws IOException {
//        sendInfo("/dataconsumer/" + dc.getID() + "/name", dc.getName());        
//        sendInfo("/dataconsumer/" + dc.getID() + "/inetSocketAddress", dc.getControlPlane().getControlEndPoint());
//        
//        for (ControllableReporter r: dc.getReportersCollection()) {
//            if (r instanceof ControllableReporter)
//                addReporter((ControllableReporter)r);
//        }
//        
//        return this;
//    }
    
    
//    public ZMQInformationPublisher addDataConsumerInfo(ControllableDataConsumer dc) throws IOException {
//        // this maps the name to the ID
//	sendInfo("/dataconsumer/name/" + dc.getName(), dc.getID().toString()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            sendInfo("/dataconsumer/" + dc.getID() + "/pid", ((DefaultControllableDataConsumer) dc).getMyPID());       
//	return this;
//    }
//    
//    public ZMQInformationPublisher addReporter(ControllableReporter r) throws IOException {
//        sendInfo("/reporter/" + r.getId() + "/name", r.getName());
//        sendInfo("/reporter/" + r.getId() + "/dataconsumer", r.getDcId().toString());
//        return this;
//    }
    
    
    /**
     * Add data for a DataSource
     */
    public ZMQInformationPublisher addDataSource(DataSource ds) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject dataSourceInfo = new JSONObject();
        
        try {
            dataSourceInfo.put("id", ds.getID().toString());
            dataSourceInfo.put("name", ds.getName());
            
            if (ds instanceof ControllableDataSource) {
                dataSourceInfo.put("pid", ((ControllableDataSource) ds).getMyPID());
                
                JSONObject controlEndPoint = new JSONObject();
                
                if (((ControllableDataSource) ds).getDataSourceConfigurator() != null) {
                    String externalHost = ((ControllableDataSource) ds).getDataSourceConfigurator().getDockerHost();
                    int controlPort = ((ControllableDataSource) ds).getDataSourceConfigurator().getControlForwardedPort();
                    
                    try {
                        controlEndPoint.put("address", externalHost);
                        controlEndPoint.put("port", controlPort);
                        controlEndPoint.put("type", "socket/NAT");
                    } catch (JSONException e) {
                        throw new IOException(e.getMessage());
                    }
                    
                    dataSourceInfo.put("controlendpoint", controlEndPoint);           
                }
                
                else {
                    dataSourceInfo.put("controlendpoint", ds.getControlPlane().getControlEndPoint());
                }
            }
            
            infoObj.put("entity", "datasource");
            infoObj.put("operation", "add"); // FIXME: could use an ENUM
            infoObj.put("info", dataSourceInfo);
            
 
        } catch(JSONException e) {
            LOGGER.error("Error while formatting info" + e.getMessage());
        }
        
        sendInfo("info.datasource", infoObj.toString());    
        
	Collection<Probe> probes = ds.getProbes();

	for (Probe aProbe : probes) {
	    addProbe(aProbe);
	}
        
	return this;
    }
    
    
    /**
     * Add data for a Probe.
     */
    public ZMQInformationPublisher addProbe(Probe aProbe) throws IOException {
        DataSource ds = (DataSource)aProbe.getProbeManager();
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeInfo = new JSONObject();
            
            probeInfo.put("id", aProbe.getID().toString());
            probeInfo.put("name", aProbe.getName());
            probeInfo.put("datarate", aProbe.getDataRate().toString());
            probeInfo.put("on", aProbe.isOn());
            probeInfo.put("active", aProbe.isActive());
            probeInfo.put("datasource", ds.getID().toString());
        
            infoObj.put("entity", "probe");
            infoObj.put("operation", "add"); //FIXME
            infoObj.put("info", probeInfo);
            
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }

        sendInfo("info.probe", infoObj.toString());
        
        Collection<ProbeAttribute> attrs = aProbe.getAttributes();
        for (ProbeAttribute attr : attrs) {
	    addProbeAttribute(aProbe, attr);
	}
        
	return this;
    }

    /**
     * Add data for a ProbeAttribute.
     */
    public ZMQInformationPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject attrInfo = new JSONObject();
        
        try {
            attrInfo.put("field", attr.getField());
            
            JSONObject attrProperties = new JSONObject();
            attrProperties.put("name", attr.getName());
            attrProperties.put("type", attr.getType().getCode());
            attrProperties.put("units", attr.getUnits());
            
            attrInfo.put("properties", attrProperties);
            attrInfo.put("probe", aProbe.getID().toString());
            
            infoObj.put("entity", "probeattribute");
            infoObj.put("operation", "add");
            infoObj.put("info", attrInfo);
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
        sendInfo("info.probeattribute", infoObj.toString());
        
	return this;
    }

    /*
     * Modify stuff
     */
//    public ZMQInformationPublisher modifyDataSource(DataSource ds) throws IOException {
//	// remove then add
//	throw new IOException("Not implemented yet!!");
//    }
//
//    public ZMQInformationPublisher modifyProbe(Probe p) throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//    public ZMQInformationPublisher modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }


    /*
     * Remove stuff
     */
    public ZMQInformationPublisher removeDataSource(DataSource ds) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject dsInfo = new JSONObject();
            
            dsInfo.put("id", ds.getID().toString());
        
            infoObj.put("entity","datasource");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", dsInfo);
            
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
	Collection<Probe> probes = ds.getProbes();
        
	for (Probe aProbe : probes) {
	    removeProbe(aProbe);
	}
	  
        sendInfo("info.datasource", infoObj.toString());
        
	return this;
    }

    public ZMQInformationPublisher removeProbe(Probe aProbe) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeInfo = new JSONObject();
            
            probeInfo.put("id", aProbe.getID().toString());
        
            infoObj.put("entity","probe");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", probeInfo);
            
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
	Collection<ProbeAttribute> attrs = aProbe.getAttributes();
        for (ProbeAttribute attr : attrs) {
	    removeProbeAttribute(aProbe, attr);
	}
	  
        sendInfo("info.probe", infoObj.toString());
        
	return this;
    }
    

    public ZMQInformationPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
	JSONObject infoObj = new JSONObject();
        try {
            JSONObject probeAttrsInfo = new JSONObject();
            
            probeAttrsInfo.put("probe", aProbe.getID().toString());
            probeAttrsInfo.put("field", attr.getField());
        
            infoObj.put("entity","probeattribute");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", probeAttrsInfo);
            
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
        sendInfo("info.probeattribute", infoObj.toString());
        
	return this;
    }

    
//    public ZMQInformationPublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException {
//	remDHT("/dataconsumer/" + dc.getID() + "/name");
//        remDHT("/dataconsumer/" + dc.getID() + "/inetSocketAddress"); //we also need to remove the control end point
//        remDHT("/dataconsumer/name/" + dc.getName()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            remDHT("/dataconsumer/" + dc.getID() + "/pid");
//
//	// skip through all reporters
//	for (ControllableReporter r : dc.getReportersCollection()) {
//	    removeReporter((ControllableReporter)r);
//	}        
//	return this;
//    }
//    
//    
//    public ZMQInformationPublisher removeReporter(ControllableReporter r) throws IOException {
//        remDHT("/reporter/" + r.getId() + "/name");
//        remDHT("/reporter/" + r.getId() + "/dataconsumer");
//        return this;
//    }
    

    /**
     * Lookup DataSource info
     */
//    public Object getDataSourceInfo(ID dsID, String info) {
//	return getDHT("/datasource/" + dsID + "/" + info);
//    }
//
//    /**
//     * Lookup probe details.
//     */
//    public Object getProbeInfo(ID probeID, String info) {
//	return getDHT("/probe/" + probeID + "/" + info);
//    }
//
//    /**
//     * Lookup probe attribute details.
//     */
//    public Object getProbeAttributeInfo(ID probeID, int field, String info) {
//	return getDHT("/probeattribute/" + probeID + "/" + field + "/" + info);
//    }
//
//    /**
//     * Lookup ControllableDataConsumer info
//     */
//    public Object getDataConsumerInfo(ID dcID, String info) {
//	return getDHT("/dataconsumer/" + dcID + "/" + info);
//    }
//    
//    
//    /**
//     * Lookup Reporter info
//     */
//    public Object getReporterInfo(ID reporterID, String info) {
//	return getDHT("/reporter/" + reporterID + "/" + info);
//    }
//    
//    
//    public boolean containsDataSource(ID dataSourceID, int timeout) {
//        try {
//            String newKey = "/datasource/" + dataSourceID + "/name";
//            return dht.contains(newKey, timeout);
//        } 
//        catch (IOException ioe) {
//            LOGGER.error("ContainsDataSource failed for DS " + dataSourceID + ioe.getMessage());
//            return false;
//        }
//    }
//    
//    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
//        try {
//            String newKey = "/dataconsumer/" + dataConsumerID + "/name";
//            return dht.contains(newKey, timeout);
//        } 
//        catch (IOException ioe) {
//            LOGGER.error("ContainsDataConsumer failed for DS " + dataConsumerID + ioe.getMessage());
//            return false;
//        }
//    }
    

    /**
     * Send stuff to the Subscribers.
     */
    public boolean sendInfo(String aKey, String aValue) {
	LOGGER.info("sending " + aKey + " => " + aValue);
        publisher.sendMore(aKey);
        publisher.send(aValue);
	return true;    
    }
    
}
