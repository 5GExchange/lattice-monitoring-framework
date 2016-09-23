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
import eu.reservoir.monitoring.core.DataConsumer;
import eu.reservoir.monitoring.core.ID;

/**
 *
 * @author uceeftu
 */
public final class ControllableDataConsumer extends AbstractDataConsumer implements MeasurementReceiver, DataConsumer {
    /*
     * The ID
     */
    ID myID;
    
    String dataConsumerName;
    
    int mReportingInterval;
    float lastMeasurementRate = 0f;
    MeasurementRateReporter mRateReporter;
    Reporter printReporter;
    
    
    public ControllableDataConsumer(String name) {
        this(name, 10); //default interval every 10 sec
    }
    
    public ControllableDataConsumer(String name, int rate) {
        dataConsumerName = name;
        myID = ID.generate();
        printReporter = new PrintReporter();
        mRateReporter = new MeasurementRateReporter();
        this.mReportingInterval = rate;
        init();
    }
    
    private void init() {
        addReporter(mRateReporter);
        addReporter(printReporter);
        
        startMeasurementsRateThread();
    } 
    
    
    private void startMeasurementsRateThread() {
        Thread t = new Thread(new Runnable () {
                                    @Override
                                    public void run() { 
                                        long t1 = System.currentTimeMillis();
                                        while(true) {
                                            try {
                                                Thread.sleep(mReportingInterval*1000);
                                                long t2 = System.currentTimeMillis();
                                                long tDelta = t2 - t1;
                                                computeMeasurementsRate(tDelta/1000); // convert in seconds
                                                t1 = t2;
                                                mRateReporter.resetMeasurementNumber();
                                            } catch (InterruptedException ex) { return; }
                                        }
                                    }
                                  }
                             );
        t.start();
    }
    
   
    private void computeMeasurementsRate(long interval) {
        this.lastMeasurementRate = (float)mRateReporter.getMeasurementNumber()/interval;
        System.out.println("Measurement Rate (measurements/sec): " + lastMeasurementRate);
    }

    @Override
    public float getMeasurementsRate() {
        return this.lastMeasurementRate;
    }

    @Override
    public ID getID() {
        return myID;
    }

    @Override
    public DataConsumer setID(ID id) {
        this.myID = id;
        return this;
    }

    @Override
    public String getName() {
        return this.dataConsumerName;
    }

    @Override
    public DataConsumer setName(String dcName) {
        this.dataConsumerName = dcName;
        return this;
    }
    
}
