/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control;

/**
 *
 * @author uceeftu
 */
public interface ControlInterface<ReturnType, ExceptionType extends Throwable> {
    
    ReturnType startDS(String endPoint, String port, String userName, String args) throws ExceptionType;
    
    ReturnType stopDS(String dsID) throws ExceptionType;
    
    ReturnType getDataSourceInfo(String dsID) throws ExceptionType;
    
    ReturnType loadProbe(String id, String probeClassName, String probeArgs) throws ExceptionType;
    
    ReturnType unloadProbe(String id) throws ExceptionType;
    
    ReturnType turnOffProbe(String id) throws ExceptionType;
    
    ReturnType turnOnProbe(String id) throws ExceptionType;
     
    ReturnType setProbeServiceID(String probeID, String serviceID) throws ExceptionType;
    
    ReturnType setProbeGroupID(String probeID, String groupID) throws ExceptionType;
    
    ReturnType setProbeDataRate(String probeID, String dataRate) throws ExceptionType;
    
    ReturnType startDC(String endPoint, String port, String userName, String args) throws ExceptionType;
    
    ReturnType stopDC(String dcID) throws ExceptionType;
    
    ReturnType getDataConsumerMeasurementRate(String dcID) throws ExceptionType;
    
    ReturnType loadReporter(String id, String reporterClassName, String reporterArgs) throws ExceptionType;
    
    ReturnType unloadReporter(String id) throws ExceptionType;
    
    ReturnType getDataSources() throws ExceptionType;
    
    ReturnType getDataConsumers() throws ExceptionType;
    
    ReturnType getProbesCatalogue() throws ExceptionType;
}