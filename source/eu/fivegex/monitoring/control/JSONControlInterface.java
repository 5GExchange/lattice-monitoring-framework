/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public interface JSONControlInterface extends ControlInterface {
    
    JSONObject startDS(String endPoint, String userName, String args) throws JSONException;
    
    JSONObject stopDS(String endPoint, String userName) throws JSONException;
    
    JSONObject getDataSourceInfo(String dsID) throws JSONException;
    
    JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException;
    
    JSONObject unloadProbe(String id) throws JSONException;
    
    JSONObject turnOffProbe(String id) throws JSONException;
    
    JSONObject turnOnProbe(String id) throws JSONException;
     
    JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException;
    
    JSONObject setProbeGroupID(String probeID, String groupID) throws JSONException;
    
    JSONObject setProbeDataRate(String probeID, String dataRate) throws JSONException;
    
    JSONObject startDC(String endPoint, String userName, String args) throws JSONException;
    
    JSONObject stopDC(String endPoint, String userName) throws JSONException;
    
    JSONObject getDataConsumerMeasurementRate(String dcID) throws JSONException;
    
    JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException;
    
    JSONObject unloadReporter(String id) throws JSONException;
    
    JSONObject getDataSources() throws JSONException;
    
    JSONObject getDataConsumers() throws JSONException;
    
    JSONObject getProbesCatalogue() throws JSONException;
}
