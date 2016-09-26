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
public interface ControllableReporter extends Reporter {
    public ID getId();
       
    public void setId(ID id);

    public String getName();
    
    public void setName(String name);
    
    public void setDcId(ID dcId);
    
    public ID getDcId();
}
