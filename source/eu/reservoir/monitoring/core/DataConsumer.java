/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core;

/**
 *
 * @author uceeftu
 */
public interface DataConsumer extends PlaneInteracter {
    /**
     * Get the ID of the DataConsumer.
     */
    public ID getID();


    /**
     * Set the DataConsumer ID
     */
    public DataConsumer setID(ID id);
    
    
    /**
     * Get the DataConsumer ID
     */
    public String getName();
    
    
    /**
     * Get the DataConsumer ID
     */
    public DataConsumer setName(String dcName);
    
    
    /**
     * Get the current rate of received measurements
     */
    public float getMeasurementsRate(); 
    
    
    /**
     * Add a reporter to this Data Consumer
     */
    public void addReporter(Reporter l);
    
    
    /**
     * Remove a reporter from this Data Consumer
     */
    public void removeReporter(Reporter l);
	

    /**
     * List all Reporters.
     */
    public Object[] getReporters();
    
}
