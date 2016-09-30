/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.ControlPlaneMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.Transmitting;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * An interface for distribution components that need
 * to do transmitting of ControlPlaneMessage objects.
 */
public interface TransmittingAndReceiving extends Transmitting {
    public Object transmit(ControlPlaneMessage dpm, MetaData metaData) throws Exception;
    
    /*
    A Class implementing this interface is basically a transmitter of control messages 
    that also needs to receive replies related to the control messages it sends 
    */
    public Object receivedReply(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;
}
