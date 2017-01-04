package eu.fivegex.monitoring.im.zmq;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
import eu.reservoir.monitoring.core.plane.AnnounceEventListener;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.DeannounceMessage;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQInformationProducer is responsible for sending information about  DataSource, ControllableDataConsumer and Probe
 attributes on the InfoPlane using ZMQ.
**/

public class ZMQInformationSubscriber extends Thread {
    String remoteHost;
    int remotePort = 0;
    int localPort = 0;
    
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
    
    
    Map<ID, JSONObject> dataSources = new HashMap<>();
    Map<ID, JSONObject> probes = new HashMap<>();
    Map<ID, JSONObject> probeAttributes = new HashMap<>();
    
    Map<ID, JSONObject> dataConsumers = new HashMap<>();
    Map<ID, JSONObject> reporters = new HashMap<>();
    
    AnnounceEventListener listener;
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQInformationSubscriber.class);

    /**
     * Construct a ZMQInformationConsumer given a local port where binding to.
     */
    public ZMQInformationSubscriber(int localPort) {
	this.localPort = localPort;
    }
    
    /**
     * Construct a ZMQInformationConsumer given a remote host and remote 
     * port where connecting to.
     */
    public ZMQInformationSubscriber(String remHost, int remPort) {
	remoteHost = remHost;
	remotePort = remPort;
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connectAndListen() {
        String uri = "tcp://" + remoteHost + ":" + remotePort;
        subscriber.connect(uri);
        this.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {   
            }
        return true;
    }
    
    
    public boolean bindAndListen() {
        subscriber.bind("tcp://*:" + localPort);
        this.start();
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
        subscriber.close();
        context.term();
        return true;
    }

    public String getRootHostname() {
        return this.remoteHost;
    }
    
    
    public boolean containsDataSource(ID dataSourceID) {
        return dataSources.containsKey(dataSourceID);
    }
    

    public boolean containsDataConsumer(ID dataConsumerID) {
        return dataConsumers.containsKey(dataConsumerID);
    }
    
    public Object getDataSourceInfo(ID dataSourceID, String info) {
        try {
            return dataSources.get(dataSourceID).get(info);
        } catch (JSONException e) {
            LOGGER.error("Error while retrieving Data Source info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    public Object getProbeInfo(ID probeID, String info) {
        try {
            return probes.get(probeID).get(info);
        } catch (JSONException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public void run() {
        String filter = "info.";
        
        subscriber.subscribe(filter.getBytes());
        
        LOGGER.info("Listening for messages");
        
        while (!Thread.currentThread ().isInterrupted()) {
            String header = subscriber.recvStr ();
            String content = subscriber.recvStr ();
            LOGGER.info(header + " : " + content);
            messageHandler(content);
        }
    }
    
    
    
    private void messageHandler(String message) {
        JSONObject msgObj;
        try {
            msgObj = new JSONObject(message);
            ID entityID = null;
            Integer field = -1;
            
            String entityType = msgObj.getString("entity");
            String operation = msgObj.getString("operation");
            
            if (!entityType.equals("probeattribute")) {
                entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
            }
            else
                field = msgObj.getJSONObject("info").getInt("field");
            
            switch(entityType) {
                case "datasource":  
                    if (operation.equals("add")) {
                        dataSources.put(entityID, msgObj.getJSONObject("info"));
                        fireEvent(new AnnounceMessage(entityID, EntityType.DATASOURCE));
                    }
                    else if (operation.equals("remove")) {
                        dataSources.remove(entityID);
                        fireEvent(new DeannounceMessage(entityID, EntityType.DATASOURCE));
                    }
                    
                    LOGGER.info("datasource map:\n");
                    for (ID id: dataSources.keySet())
                        LOGGER.info(dataSources.get(id).toString(1));
                    
                    break;
                        
                case "probe":
                    if (operation.equals("add")) {
                        probes.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        probes.remove(entityID);
                    }
                    
                    
                    LOGGER.info("probe map:\n");
                    for (ID id: probes.keySet())
                        LOGGER.info(probes.get(id).toString(1));
                    
                    break;
                        
                case "probeattribute":
                    ID probeID = ID.fromString(msgObj.getJSONObject("info").getString("probe"));
                    JSONObject attributes;
                    
                    if (operation.equals("add")) {
                        
                        if (!probeAttributes.containsKey(probeID)) { 
                           attributes = new JSONObject();
                           attributes.put(field.toString(), msgObj.getJSONObject("info").getJSONObject("properties"));
                           probeAttributes.put(probeID, attributes);
                        }
                        else {
                            attributes = probeAttributes.get(probeID);
                            attributes.accumulate(field.toString(), msgObj.getJSONObject("info").getJSONObject("properties"));
                            probeAttributes.put(probeID, attributes);
                        }
                        
                    }
                    
                    else if (operation.equals("remove")) {
                        attributes = probeAttributes.get(probeID);
                        attributes.remove(field.toString());
                        if (!attributes.keys().hasNext())
                            probeAttributes.remove(probeID);
                    }
                    
                    LOGGER.info("probeattribute map:\n");
                    for (ID id: probeAttributes.keySet())
                        LOGGER.info(probeAttributes.get(id).toString(1));
                    
                    break;
                    
                        
                case "dataconsumer":  
                    if (operation.equals("add")) {
                        dataConsumers.put(entityID, msgObj.getJSONObject("info"));
                        // trigger announce event
                    }
                    else if (operation.equals("remove")) {
                        dataConsumers.remove(entityID);
                        // trigger deannounce event
                    }
                    break;
                        
                case "reporter":  
                    break;        
                        
            }
            
            
        } catch (JSONException e) {
            LOGGER.error("Error while deserializing received message" + e.getMessage());
        } 
    }
    
    
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
}
