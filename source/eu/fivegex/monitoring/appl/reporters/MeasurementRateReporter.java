/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.reporters;

import eu.reservoir.monitoring.core.AbstractReporter;
import eu.reservoir.monitoring.core.Measurement;

/**
 *
 * @author uceeftu
 */
public class MeasurementRateReporter extends AbstractReporter {
    Long measurementNumber=0L;

    public MeasurementRateReporter() {
        super("measurement-rate-reporter");
    }
    
    @Override
    public void report(Measurement m) {
        synchronized(measurementNumber) {
            measurementNumber++;
        }
    }
    
    public Long getMeasurementNumber() {
        //maybe could be synchronized
        return this.measurementNumber;
    }
    
    public void resetMeasurementNumber() {
        synchronized(measurementNumber) {
            measurementNumber=0L;
        }
    }
    
}
