/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.im.delegate;

import eu.reservoir.monitoring.core.ID;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateProducer {

    void addDataConsumer(ID id, int timeout) throws InterruptedException, DCNotFoundException;

    void addDataSource(ID id, int timeout) throws InterruptedException, DSNotFoundException;
    
}
