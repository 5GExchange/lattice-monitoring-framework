/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Reporter;

/**
 *
 * @author uceeftu
 */
public class MeasurementRateReporter implements Reporter {
    Long measurementNumber=0L;
    
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
