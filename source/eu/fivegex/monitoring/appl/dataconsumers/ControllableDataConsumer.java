/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.dataconsumers;

import eu.fivegex.monitoring.appl.reporters.PrintReporter;
import eu.reservoir.monitoring.core.AbstractDataConsumer;
import eu.reservoir.monitoring.core.MeasurementReceiver;
import eu.reservoir.monitoring.core.ControllableReporter;
import eu.reservoir.monitoring.core.DataConsumer;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Measurement;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends AbstractDataConsumer functionalities adding remote control
 * it also provides measurements rate reporting as specified in the DataConsumer Interface
 * it comes with a default PrintReporter
 * @author uceeftu
 */
public final class ControllableDataConsumer extends AbstractDataConsumer implements MeasurementReceiver, DataConsumer {
    /**
     * The DC ID
     */
    ID myID;
    
    /**
     * The DC name
     */
    String dataConsumerName;
    
    /**
     * An attribute to count the number of measurements taken off the queue
     */
    Long measurementsCounter = 0L;
    
    /**
     * Specifies the period of the measurement reporting
     */
    int mReportingInterval;
    
    /**
     *The last computed measurement rate 
     */
    float lastMeasurementRate = 0f;
    
    
    /**
    * a Map Reporter id to ControllableReporter
    */
    Map<ID, ControllableReporter> reporters = new HashMap<>();
    
    
    /**
     * A print reporter to show measurement on the stdout
     */
    ControllableReporter printReporter;
    
    
    public ControllableDataConsumer(String name) {
        this(name, 20); //default interval every 10 sec
    }
    
    public ControllableDataConsumer(String name, int rate) {
        dataConsumerName = name;
        myID = ID.generate();
        printReporter = new PrintReporter();
        this.mReportingInterval = rate;
        init();
    }
    
    private void init() {
        // we set the DC id to the reporter 
        // (needed to link information in the info plane)
        printReporter.setDcId(myID);
        addReporter(printReporter);
        
        startMeasurementsRateThread();
    } 

    public Long getMeasurementsCounter() {
        return measurementsCounter;
    }

    public void setMeasurementsCounter(Long measurementCounter) {
        this.measurementsCounter = measurementCounter;
    }
    
    // creates a thread to measure the number of meaurement taken off th queue
    // in the mReportingInterval time interval
    private void startMeasurementsRateThread() {
        Thread t = new Thread(new Runnable () {
                                    @Override
                                    public void run() { 
                                        long t1 = System.nanoTime();
                                        while(true) {
                                            try {
                                                Thread.sleep(mReportingInterval*1000);
                                                long t2 = System.nanoTime();
                                                long tDelta = t2 - t1;
                                                computeMeasurementsRate(tDelta/(1000*1000*1000)); // convert in seconds
                                                t1 = t2;
                                                setMeasurementsCounter(0L);
                                            } catch (InterruptedException ex) { }
                                        }
                                    }
                                  }
                             );
        t.start();
    }
    
   
    private void computeMeasurementsRate(long interval) {
        this.lastMeasurementRate = (float)this.getMeasurementsCounter()/interval;
        //System.out.println("Current Measurement Rate (measurements/sec): " + lastMeasurementRate);
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

    @Override
    public void removeReporter(ControllableReporter l) {
        super.removeReporter(l);
        reporters.remove(l.getId());
        if (this.getInfoPlane() != null)
            this.getInfoPlane().removeReporterInfo(l);
        
    }

    @Override
    public void addReporter(ControllableReporter l) {
        l.setDcId(myID);
        super.addReporter(l);
        reporters.put(l.getId(), l);
        if (this.getInfoPlane() != null)
            this.getInfoPlane().addReporterInfo(l);
    }

    @Override
    public ControllableReporter getReporterById(ID reporterID) {
        return reporters.get(reporterID);
    }
    
    
    
    // this overriden method from the super class only adds a measurementsCounter
    @Override
    public void run() {
	beginThreadBody();
	while (threadRunning) {
	    Measurement m = null;
	    try {
		m = (Measurement)measurementQueue.take();
	    } catch (InterruptedException ie) {
		continue;
	    }
	    fireEvent(m);
            // counts the number of measurements taken off the queue
            measurementsCounter++;
	}
	endThreadBody();
   }  
}
