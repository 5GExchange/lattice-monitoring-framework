/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import eu.reservoir.monitoring.appl.PrintReporter;
import eu.reservoir.monitoring.core.AbstractDataConsumer;
import eu.reservoir.monitoring.core.MeasurementReceiver;
import eu.reservoir.monitoring.core.Reporter;

/**
 *
 * @author uceeftu
 */
public final class BasicConsumerWithRateReporter extends AbstractDataConsumer implements MeasurementReceiver {
    int mReportingInterval;
    MeasurementRateReporter mRateReporter;
    Reporter printReporter;
    
    public BasicConsumerWithRateReporter(int rate) {
        printReporter = new PrintReporter();
        mRateReporter = new MeasurementRateReporter();
        this.mReportingInterval = rate;
        init();
    }
    
    private void init() {
        addReporter(mRateReporter);
        addReporter(printReporter);
        Thread t = new Thread(new Runnable () {
                                    @Override
                                    public void run() { 
                                        long t1 = System.currentTimeMillis();
                                        System.out.println("Thread Started"); 
                                        while(true) {
                                            try {
                                                Thread.sleep(mReportingInterval*1000);
                                                long t2 = System.currentTimeMillis();
                                                long tDelta = t2 - t1;
                                                printRate(tDelta/1000); // convert in seconds
                                                t1 = t2;
                                                mRateReporter.resetMeasurementNumber();
                                            } catch (InterruptedException ex) { return; }
                                        }
                                    }
                                  }
                             );
        t.start();
    } 
    
   
    private void printRate(long interval) {
        System.out.println("Measurement Rate (measurements/sec): " + (float)mRateReporter.getMeasurementNumber()/interval);
    }
    
}
