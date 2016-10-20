/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.ProbeNotFoundException;
import eu.fivegex.monitoring.control.ReporterNotFoundException;
import eu.fivegex.monitoring.control.DSNotFoundException;
import eu.fivegex.monitoring.control.DCNotFoundException;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class InformationManager {
    private final InfoPlane info;
    private final List<ID> dataSources;
    private final List<ID> dataConsumers;
    private final Map<ID, Object> pendingDataSources;
    private final Map<ID, Object> pendingDataConsumers;
    
    public InformationManager(InfoPlane info){
        this.info=info;
        dataSources = Collections.synchronizedList(new ArrayList());
        dataConsumers = Collections.synchronizedList(new ArrayList());
        pendingDataSources = new ConcurrentHashMap<>();
        pendingDataConsumers = new ConcurrentHashMap<>();
    }
    
    
    public void addNewAnnouncedEntity(ID id, EntityType type) {
        // we add the entity to the list only if that entity information is also in the info Plane
        if (type == EntityType.DATASOURCE && !containsDataSource(id) && info.containsDataSource(id, 1000)) { 
            System.out.println("InformationManager: adding Data Source " + id.toString());
            addDataSource(id);
            // we notify any pending deployment threads
            notifyDataSource(id);
        }
        else if (type == EntityType.DATACONSUMER && !containsDataConsumer(id) && info.containsDataConsumer(id, 1000)) {
            System.out.println("InformationManager: adding Data Consumer " + id.toString());
            addDataConsumer(id);
            // we notify any pending deployment threads
            notifyDataConsumer(id);
        }
    }
    
    public void removeNewDeannouncedEntity(ID id, EntityType type) {
        if (type == EntityType.DATASOURCE && containsDataSource(id)) {
            System.out.println("InformationManager: removing Data Source " + id.toString());
            deleteDataSource(id);
        }
        else if (type == EntityType.DATACONSUMER && containsDataConsumer(id)) {
            System.out.println("InformationManager: removing Data Consumer " + id.toString());
            deleteDataConsumer(id);
        }
    }
    
    public List<ID> getDataSourcesList() {
        synchronized(dataSources) {
            return dataSources;
        }
    }
    
    public List<ID> getDataConsumersList() {
        synchronized(dataConsumers) {
            return dataConsumers;
        }
    }
    
    public JSONObject getDataSourcesAsJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        for (ID id: getDataSourcesList()) {
            JSONObject dsAddr = new JSONObject();
            try {
                InetSocketAddress dsInfo = getDSAddressFromID(id);
                dsAddr.put("host", dsInfo.getAddress().getHostAddress());
                dsAddr.put("port", dsInfo.getPort());
                obj.put(id.toString(), dsAddr);
            } catch (DSNotFoundException ex) {
                System.out.println(ex.getMessage());
                deleteDataSource(id);
              }
        }
        return obj;
    }
    
    public JSONObject getDataConsumersAsJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        for (ID id: getDataConsumersList()) {
            JSONObject dcAddr = new JSONObject();
            try {
                InetSocketAddress dcInfo = getDCAddressFromID(id);
                dcAddr.put("host", dcInfo.getAddress().getHostAddress());
                dcAddr.put("port", dcInfo.getPort());
                obj.put(id.toString(), dcAddr);
            } catch (DCNotFoundException ex) {
                System.out.println(ex.getMessage());
                deleteDataConsumer(id);
              }
            }
        return obj;
    }
    
    
    void addDataSource(ID id) {
        dataSources.add(id);
    }
    
    void addDataConsumer(ID id) {
        dataConsumers.add(id);
    }
    
    void deleteDataSource(ID id) {    
        dataSources.remove(id);
    }
    
    void deleteDataConsumer(ID id) {
        dataConsumers.remove(id);
    }
    
    boolean containsDataSource(ID id) {
        return dataSources.contains(id);
    }
    
    boolean containsDataConsumer(ID id) {
        return dataConsumers.contains(id);
    }
    
    public void waitForDataSource(ID id, int timeout) throws InterruptedException, DSNotFoundException {
        Object monitor = new Object(); 
        synchronized(monitor) {
            System.out.println("Adding pending data source: " + id);
            pendingDataSources.put(id, monitor);
            monitor.wait(timeout);
        }
        if (pendingDataSources.containsKey(id)) //cleaning up
            pendingDataSources.remove(id);
        
        if (!containsDataSource(id)) {  
            if (!info.containsDataSource(id, 0)) //wait some more time
                throw new DSNotFoundException("Announce Message was not received by the Controller both on the control and info planes");
            else
                addDataSource(id); //we may have lost the message but the DS might be up and running
            
        }
    }
    
    
    public void waitForDataConsumer(ID id, int timeout) throws InterruptedException, DCNotFoundException {
        Object monitor = new Object(); 
        synchronized(monitor) {
            pendingDataConsumers.put(id, monitor);
            monitor.wait(timeout);
        }
        if (pendingDataConsumers.containsKey(id)) //cleaning up
            pendingDataConsumers.remove(id);
        
        if (!containsDataConsumer(id)) {  
            if (!info.containsDataConsumer(id, 0))
                throw new DCNotFoundException("Announce Message was not received by the Controller both on the control and info planes");
            else
                addDataConsumer(id); //we may have lost the message but the DC is up and running
            
        }
    }
    
    public void notifyDataSource(ID id) {
        // checking if there is a pending deployment for that Data Source ID
        if (pendingDataSources.containsKey(id)) {
            System.out.println("Notifying pending Data Source: " + id);
            Object monitor = pendingDataSources.remove(id);
            synchronized (monitor) {
                monitor.notify();
            }
        }
        // else do nothing
    }
    
    
    public void notifyDataConsumer(ID id) {
        // checking if there is a pending deployment for that Data Consumer ID
        if (pendingDataConsumers.containsKey(id)) {
            System.out.println("Notifying pending Data Consumer: " + id);
            Object monitor = pendingDataConsumers.remove(id);
            synchronized (monitor) {
                monitor.notify();
            }
        }
        // else do nothing
    }
    
    
    public InetSocketAddress getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException {
        String dsID = (String)info.lookupProbeInfo(probe, "datasource");
        
        if (dsID != null) {
            ID dataSourceID = ID.fromString(dsID);
            if (!containsDataSource(dataSourceID))
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " was de-announced");
            
            System.out.println("Found this data source ID: " + dataSourceID);
            InetSocketAddress dsAddress = getDSAddressFromID(dataSourceID);
            if (dsAddress != null)
                return dsAddress;
            else
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " not found in the infoplane");
        }
        else
            throw new ProbeNotFoundException("probe with ID " + probe.toString() + " not found in the infoplane");
    }
    
    public InetSocketAddress getDSAddressFromID(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        InetSocketAddress dsAddress = (InetSocketAddress)info.lookupDataSourceInfo(dataSource, "inetSocketAddress");
        if (dsAddress != null)
            return dsAddress;
        else 
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the infoplane");
    }

    public String getDSIDFromName(String dsName) throws DSNotFoundException {
        //using generic getInfo method for getting DS ID from DS name
        String dsID = (String)info.getInfo("/datasource/name/" + dsName);
        if (dsID != null)
            if (!containsDataSource(ID.fromString(dsID)))
                throw new DSNotFoundException("Data Source with ID " + dsID + " was de-announced");
            else
                return dsID;
        else 
            throw new DSNotFoundException("Data Source with name " + dsName + " not found in the infoplane");
        }  
    
    public InetSocketAddress getDCAddressFromID(ID dataConsumer) throws DCNotFoundException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was de-announced");
        
        InetSocketAddress dsAddress = (InetSocketAddress)info.lookupDataConsumerInfo(dataConsumer, "inetSocketAddress");
        if (dsAddress != null)
            return dsAddress;
        else 
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the infoplane");
    }
    
    public InetSocketAddress getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
            if (!containsDataConsumer(dataConsumerID))
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " was de-announced");
                
            System.out.println("Found this data consumer ID: " + dataConsumerID);
            InetSocketAddress dsAddress = getDCAddressFromID(dataConsumerID);
            if (dsAddress != null)
                return dsAddress;
            else
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " not found in the infoplane");
        }
        else
            throw new ReporterNotFoundException("probe with ID " + reporter.toString() + " not found in the infoplane");
    }
    
    public Integer getDSPIDFromID(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataSourceInfo(dataSource, "pid");
        if (pID != null)
            return pID;
        else 
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the infoplane or missing pid entry"); 
    }
    
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataConsumerInfo(dataConsumer, "pid");
        if (pID != null)
            return pID;
        else
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the infoplane or missing pid entry");
    }
}
