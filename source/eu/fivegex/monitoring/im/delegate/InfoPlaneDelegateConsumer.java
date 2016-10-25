/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.im.delegate;

import eu.reservoir.monitoring.core.ID;
import java.net.InetSocketAddress;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateConsumer {
    
    public JSONObject getDataSources() throws JSONException;
    
    public JSONObject getDataConsumers() throws JSONException;
    
    public InetSocketAddress getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException;
    
    public InetSocketAddress getDSAddressFromID(ID dataSource) throws DSNotFoundException;
    
    public String getDSIDFromName(String dsName) throws DSNotFoundException;
    
    public InetSocketAddress getDCAddressFromID(ID dataConsumer) throws DCNotFoundException;
    
    public InetSocketAddress getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException;
    
    public Integer getDSPIDFromID(ID dataSource) throws DSNotFoundException;
    
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException;  
}
