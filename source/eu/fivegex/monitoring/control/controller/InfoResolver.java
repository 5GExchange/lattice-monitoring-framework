/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.net.InetSocketAddress;

/**
 *
 * @author uceeftu
 */
public class InfoResolver {
    InfoPlane info;
    
    public InfoResolver(InfoPlane info){
        this.info=info;
    }
    
    public InetSocketAddress getDSAddressFromProbeID(ID probe) throws ProbeIDNotFoundException, DSNotFoundException {
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
            throw new ProbeIDNotFoundException("probe with ID " + probe.toString() + " not found in the infoplane");
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
