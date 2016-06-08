/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

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
        ID dataSourceID;
        try {
            dataSourceID = ID.fromString((String)info.lookupProbeInfo(probe, "datasource"));
        } catch (NullPointerException ex) {
            throw new ProbeIDNotFoundException("probe with ID " + probe.toString() + " not found in the infoplane");
        }
        System.out.println("Found this data source ID: " + dataSourceID);
        return getDSAddressFromID(dataSourceID);
    }
    
    public InetSocketAddress getDSAddressFromID(ID dataSource) throws DSNotFoundException {
        try {
            return (InetSocketAddress)info.lookupDataSourceInfo(dataSource, "inetSocketAddress");
            }
        catch (NullPointerException ex) {
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the infoplane");
        }
    }
    
}
