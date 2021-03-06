/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.im.delegate;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author uceeftu
 */
public class ReporterInformationManager {
    private final InfoPlane info;
    private final Map<ID,String> probesCache;
    private final Map<ID,String> probeAttributesCache;
    
    public ReporterInformationManager(InfoPlane info) {
        this.info = info;
        probesCache = new HashMap<>();
        probeAttributesCache = new HashMap<>();
    }
    
    public String getProbeName(ID probeID) throws ProbeNotFoundException {
        String probeName = null;

        if (!probesCache.containsKey(probeID)) {
            probesCache.put(probeID, (String)info.lookupProbeInfo(probeID, "name"));
        }
        else
            probeName = probesCache.get(probeID);

        if (probeName == null)
            throw new ProbeNotFoundException("Error while getting information for probe: " + probeID);
        
        return probeName;
    }
    
    public Object getProbeAttributeName(ID probeID, int field) throws ProbeAttributeNotFoundException{
       String probeAttributeName = null;
       if (!probeAttributesCache.containsKey(probeID)) {
           probeAttributesCache.put(probeID, (String)info.lookupProbeAttributeInfo(probeID, field, "name"));
       }
       else
           probeAttributeName = probeAttributesCache.get(probeID);
       
       if (probeAttributeName == null)
           throw new ProbeAttributeNotFoundException("Error while getting information for probe attribute: " + field + ", probe: " + probeID);
       
       return probeAttributeName;
    }
    
}
