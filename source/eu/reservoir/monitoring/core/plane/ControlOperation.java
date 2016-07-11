/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import java.util.EnumSet;
import java.util.HashMap;

/**
 *
 * @author uceeftu
 */
public enum ControlOperation {
    
    TURN_OFF_PROBE("turnOffProbe"),
    TURN_ON_PROBE("turnOnProbe"),
    LOAD_PROBE("loadProbe"),
    UNLOAD_PROBE("unloadProbe"),
    SET_PROBE_SERVICE_ID("setProbeServiceID");
    
    private static final HashMap<String, ControlOperation> lookup = new HashMap<>();

    // fill in the map
    static {
	for(ControlOperation t : EnumSet.allOf(ControlOperation.class))     
	    lookup.put(t.getValue(), t);
    }

    /**
     * Lookup an MessageType enum 
     */
    public static final ControlOperation lookup(String mt) {
	return lookup.get(mt);
    }
     
    /*
     * The value for each MessageType.
     */
    String MethodName;

    /*
     * Define the message types
     */
    private ControlOperation(String OperationName) {
	MethodName = OperationName;
    }

    /**
     * Get the value of a MessageType
     */
    public String getValue() {
	return MethodName;   
    }
}
