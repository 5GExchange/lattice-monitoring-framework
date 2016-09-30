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
import eu.reservoir.monitoring.core.plane.AnnounceMessage.EntityType;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    
    public InformationManager(InfoPlane info){
        this.info=info;
        dataSources = Collections.synchronizedList(new ArrayList());
        dataConsumers = Collections.synchronizedList(new ArrayList());
    }
    
    
    public void addNewAnnouncedEntity(ID id, EntityType type) {
        // we add to the list only if that entity information is also in the info Plane
        if (type == EntityType.DATASOURCE && info.lookupDataSourceInfo(id, "inetSocketAddress") != null) {
            System.out.println("InfoResolver: adding Data Source " + id.toString());
            addDataSource(id);
        }
        else if (type == EntityType.DATACONSUMER && info.lookupDataConsumerInfo(id, "inetSocketAddress") != null) {
            System.out.println("InfoResolver: adding Data Consumer " + id.toString());
            addDataConsumer(id);
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
    
    public JSONObject getDataSourceAsJSON() throws DSNotFoundException, JSONException {
        JSONObject obj = new JSONObject();
        for (ID id: getDataSourcesList()) {
            JSONObject dsAddr = new JSONObject();
            InetSocketAddress dsInfo = getDSAddressFromID(id);
            dsAddr.put("host", dsInfo.getAddress().getHostAddress());
            dsAddr.put("port", dsInfo.getPort());
            obj.put(id.toString(), dsAddr);
        }
        return obj;
    }
    
    
    void addDataSource(ID id) {
        dataSources.add(id);
    }
    
    void addDataConsumer(ID id) {
        dataConsumers.add(id);
    }
    
    
    public InetSocketAddress getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException {
        String dsID = (String)info.lookupProbeInfo(probe, "datasource");
        
        if (dsID != null) {
            ID dataSourceID = ID.fromString(dsID);
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
            return dsID;
        else 
            throw new DSNotFoundException("Data Source with name " + dsName + " not found in the infoplane");
        }  
    
    public InetSocketAddress getDCAddressFromID(ID dataConsumer) throws DCNotFoundException {
        InetSocketAddress dsAddress = (InetSocketAddress)info.lookupDataConsumerInfo(dataConsumer, "inetSocketAddress");
        if (dsAddress != null)
            return dsAddress;
        else 
            throw new DCNotFoundException("Data Source with ID " + dataConsumer.toString() + " not found in the infoplane");
    }
    
    public InetSocketAddress getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
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
    
    
    
}
