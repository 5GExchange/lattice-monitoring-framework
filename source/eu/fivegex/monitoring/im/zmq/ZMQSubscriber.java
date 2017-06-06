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
import org.zeromq.ZMQException;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 * DataSources, DataConsumers, Probes and probes attributes on the InfoPlane 
 * using ZMQ.
**/

public class ZMQSubscriber extends Thread {
    String remoteHost;
    int remotePort = 0;
    int localPort = 0;
    
    ZMQ.Context context;
    ZMQ.Socket subscriberSocket;
    
    String internalURI;
    String messageFilter;
    
    boolean threadRunning = false;
    
    Map<ID, JSONObject> dataSources = new HashMap<>();
    Map<ID, JSONObject> probes = new HashMap<>();
    Map<ID, JSONObject> probeAttributes = new HashMap<>();
    
    Map<ID, JSONObject> dataConsumers = new HashMap<>();
    Map<ID, JSONObject> reporters = new HashMap<>();
    
    AnnounceEventListener listener;
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQSubscriber.class);

    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQSubscriber(String remHost, int remPort, String filter) {
        this(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	remoteHost = remHost;
	remotePort = remPort;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQSubscriber(String internalURI, String filter, ZMQ.Context context) {
	this.internalURI = internalURI;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
        subscriberSocket.setLinger(0);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public ZMQSubscriber(int port, String filter) {
        this(port, filter, ZMQ.context(1));
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZMQ.Context.
     */
    
    public ZMQSubscriber(int port, String filter, ZMQ.Context context) {
	localPort = port;
        messageFilter = filter; 
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
        subscriberSocket.setLinger(0);
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connectAndListen() {
        String uri;
        if (remoteHost != null && remotePort != 0)
            uri = "tcp://" + remoteHost + ":" + remotePort;
        else {
            uri = internalURI;
            // sleeping before connecting to the inproc socket
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
        
        subscriberSocket.connect(uri);
        this.start();
        return true;
    }
    
    
    public boolean bindAndListen() {
        subscriberSocket.bind("tcp://*:" + localPort);
        this.start();
        return true;
    }

    
    public ZMQ.Socket getSubscriberSocket() {
        return subscriberSocket;
    }
    

    /**
     * Disconnect from the DHT peers.
     */
    public boolean disconnect() {
        threadRunning = false;
        subscriberSocket.close();
        context.close();
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
    
    
    
    public Object getProbeAttributeInfo(ID probeID, Integer field, String info) {
        try {
            return probeAttributes.get(probeID).getJSONObject(field.toString()).get(info);
        } catch (JSONException e) {
            LOGGER.error("Error while retrieving Attribute info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    public Object getDataConsumerInfo(ID dataConsumerID, String info) {
        try {
            return dataConsumers.get(dataConsumerID).get(info);
        } catch (JSONException e) {
            LOGGER.error("Error while retrieving Data Consumer info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    public Object getReporterInfo(ID reporterID, String info) {
        try {
            return reporters.get(reporterID).get(info);
        } catch (JSONException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public void run() {
        this.setName("zmq-info-subscriber");
        subscriberSocket.subscribe(messageFilter.getBytes());
        
        LOGGER.info("Listening for messages");
        
        threadRunning = true;
        try {
            while (threadRunning) {
                String header = subscriberSocket.recvStr();
                String content = subscriberSocket.recvStr();
                LOGGER.debug(header + " : " + content);
                messageHandler(content);
            }
            } catch (ZMQException e) {
                // we won't do anything except logging the exception
                LOGGER.debug(e.getMessage());
            }
    }
    
    
    
    private void messageHandler(String message) {
        JSONObject msgObj;
        try {
            msgObj = new JSONObject(message);
            ID entityID = null;
            String field = null;
            
            String entityType = msgObj.getString("entity");
            String operation = msgObj.getString("operation");
            
            if (!entityType.equals("probeattribute")) {
                entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
            }
            else
                field = msgObj.getJSONObject("info").getString("field");
            
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
                    
                    LOGGER.trace("datasource map:\n");
                    for (ID id: dataSources.keySet())
                        LOGGER.trace(dataSources.get(id).toString(1));
                    
                    break;
                        
                case "probe":
                    if (operation.equals("add")) {
                        probes.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        probes.remove(entityID);
                    }
                    
                    
                    LOGGER.trace("probe map:\n");
                    for (ID id: probes.keySet())
                        LOGGER.trace(probes.get(id).toString(1));
                    
                    break;
                        
                case "probeattribute":
                    ID probeID = ID.fromString(msgObj.getJSONObject("info").getString("probe"));
                    JSONObject attributes;
                    
                    if (operation.equals("add")) {
                        
                        if (!probeAttributes.containsKey(probeID)) { 
                           attributes = new JSONObject();
                           attributes.put(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                           probeAttributes.put(probeID, attributes);
                        }
                        else {
                            attributes = probeAttributes.get(probeID);
                            attributes.accumulate(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                            probeAttributes.put(probeID, attributes);
                        }
                        
                    }
                    
                    else if (operation.equals("remove")) {
                        attributes = probeAttributes.get(probeID);
                        if (attributes == null)
                            break;
                        if (attributes.has(field))
                            attributes.remove(field);
                        if (!attributes.keys().hasNext())
                            probeAttributes.remove(probeID);
                    }
                    
                    LOGGER.trace("probeattribute map:\n");
                    for (ID id: probeAttributes.keySet())
                        LOGGER.trace(probeAttributes.get(id).toString(1));
                    
                    break;
                    
                        
                case "dataconsumer":  
                    if (operation.equals("add")) {
                        dataConsumers.put(entityID, msgObj.getJSONObject("info"));
                        fireEvent(new AnnounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    else if (operation.equals("remove")) {
                        dataConsumers.remove(entityID);
                        fireEvent(new DeannounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    break;
                        
                case "reporter":  
                    if (operation.equals("add")) {
                        reporters.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        reporters.remove(entityID);
                    }
                    
                    
                    LOGGER.debug("reporters map:\n");
                    for (ID id: reporters.keySet())
                        LOGGER.debug(reporters.get(id).toString(1));
                    
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
