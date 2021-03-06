/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core.plane;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Rational;

/**
 *
 * @author uceeftu
 */
public interface DataConsumerControlService {
    public Rational getDCMeasurementsRate(ID dcID) throws ControlServiceException; 
    
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object ... reporterArgs) throws ControlServiceException;
    
    public boolean unloadReporter(ID reporterID) throws ControlServiceException;
}
