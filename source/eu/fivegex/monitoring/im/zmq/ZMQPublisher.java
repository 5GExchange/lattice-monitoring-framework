package eu.fivegex.monitoring.im.zmq;

import eu.fivegex.monitoring.appl.dataconsumers.DefaultControllableDataConsumer;
import eu.reservoir.monitoring.core.ControllableDataConsumer;
import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.ControllableReporter;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.DockerDataSource;
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
 * An ZMQPublisher is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQPublisher {
    String remoteHost;
    int remotePort = 0;
    
    ZMQ.Context context;
    ZMQ.Socket publisherSocket;
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQPublisher.class);

    /**
     * Construct an InfoFormatter, given a remote Consumer host
     * and a remote port.
     */
    public ZMQPublisher(String remHost, int remPort) {
	remoteHost = remHost;
	remotePort = remPort;
        
        context = ZMQ.context(1);
        publisherSocket = context.socket(ZMQ.PUB);
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connect() {
        String uri = "tcp://" + remoteHost + ":" + remotePort;
        publisherSocket.connect(uri);
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
        publisherSocket.setLinger(0);
        publisherSocket.close();
        return true;
    }

    public void destroyZMQContext() {
        context.term();
    }
    
    public String getRootHostname() {
        return this.remoteHost;
    }

    
    public ZMQ.Context getContext() {
        return context;
    }
    
    
    /**
     * Add data for a DataSource
     */
    public ZMQPublisher addDataSource(DataSource ds) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject dataSourceInfo = new JSONObject();
        
        try {
            dataSourceInfo.put("id", ds.getID().toString());
            dataSourceInfo.put("name", ds.getName());
            
            if (ds instanceof ControllableDataSource) {
                dataSourceInfo.put("pid", ((ControllableDataSource) ds).getMyPID());
                
                JSONObject controlEndPoint = new JSONObject();
                
                if (ds instanceof DockerDataSource && ((DockerDataSource) ds).getDataSourceConfigurator() != null) {
                    String externalHost = ((DockerDataSource) ds).getDataSourceConfigurator().getDockerHost();
                    int controlPort = ((DockerDataSource) ds).getDataSourceConfigurator().getControlForwardedPort();
                    
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
    public ZMQPublisher addProbe(Probe aProbe) throws IOException {
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
    public ZMQPublisher addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
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
    
    
    public ZMQPublisher addDataConsumer(ControllableDataConsumer dc) throws IOException {
        JSONObject infoObj = new JSONObject();
        JSONObject dataConsumerInfo = new JSONObject();
        
        try {
            dataConsumerInfo.put("id", dc.getID().toString());
            dataConsumerInfo.put("name", dc.getName());
            
            if (dc instanceof DefaultControllableDataConsumer) {
                dataConsumerInfo.put("pid",  ((DefaultControllableDataConsumer) dc).getMyPID());
                dataConsumerInfo.put("controlendpoint", dc.getControlPlane().getControlEndPoint());
            }
            
            infoObj.put("entity", "dataconsumer");
            infoObj.put("operation", "add"); // FIXME: could use an ENUM
            infoObj.put("info", dataConsumerInfo);
 
        } catch(JSONException e) {
            LOGGER.error("Error while formatting info" + e.getMessage());
        }
        
        sendInfo("info.dataconsumer", infoObj.toString());    
        
        Collection<ControllableReporter> reporters = dc.getReportersCollection();

	for (ControllableReporter aReporter : reporters) {
	    addReporter(aReporter);
	}
        
	return this;
    }
    
    
    
    public ZMQPublisher addReporter(ControllableReporter r) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject reporterInfo = new JSONObject();
            
            reporterInfo.put("id", r.getId().toString());
            reporterInfo.put("name", r.getName());
            reporterInfo.put("dataconsumer", r.getDcId().toString());
        
            infoObj.put("entity", "reporter");
            infoObj.put("operation", "add"); //FIXME
            infoObj.put("info", reporterInfo);
            
        } catch (JSONException e) {
            LOGGER.error("Error" + e.getMessage());
        }

        sendInfo("info.reporter", infoObj.toString());

	return this;
    }
    

    /*
     * Modify stuff
     */
//    public ZMQPublisher modifyDataSource(DataSource ds) throws IOException {
//	// remove then add
//	throw new IOException("Not implemented yet!!");
//    }
//
//    public ZMQPublisher modifyProbe(Probe p) throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//    public ZMQPublisher modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }


    /*
     * Remove stuff
     */
    public ZMQPublisher removeDataSource(DataSource ds) throws IOException {
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

    public ZMQPublisher removeProbe(Probe aProbe) throws IOException {
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
    

    public ZMQPublisher removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
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

    
    public ZMQPublisher removeDataConsumer(ControllableDataConsumer dc) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject dcInfo = new JSONObject();
            
            dcInfo.put("id", dc.getID().toString());
        
            infoObj.put("entity","dataconsumer");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", dcInfo);
            
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
        Collection<ControllableReporter> reporters = dc.getReportersCollection();

	for (ControllableReporter aReporter : reporters) {
	    removeReporter(aReporter);
	}
	  
        sendInfo("info.dataconsumer", infoObj.toString());
        
	return this;
    }
    
    
    public ZMQPublisher removeReporter(ControllableReporter r) throws IOException {
        JSONObject infoObj = new JSONObject();
        try {
            JSONObject reporterInfo = new JSONObject();
            
            reporterInfo.put("id", r.getId().toString());
        
            infoObj.put("entity","reporter");
            infoObj.put("operation", "remove"); // FIXME: could use an ENUM
            infoObj.put("info", reporterInfo);
            
            
        } catch (JSONException e) {
             LOGGER.error("Error" + e.getMessage());
        }
        
        sendInfo("info.reporter", infoObj.toString());
        
        return this;
    }
    

    /**
     * Send stuff to the Subscribers.
     */
    public boolean sendInfo(String aKey, String aValue) {
	LOGGER.debug("sending " + aKey + " => " + aValue);
        publisherSocket.sendMore(aKey);
        publisherSocket.send(aValue);
	return true;    
    }
    
}
