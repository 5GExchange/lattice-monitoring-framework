/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control;

import eu.reservoir.monitoring.distribution.Transmitting;
import java.io.IOException;

/**
 *
 * @author uceeftu
 */
public interface Transmitter {
    public void setTransmitting(Transmitting transmitting);
     public void connect() throws IOException;
    public void end() throws IOException;
    
}
