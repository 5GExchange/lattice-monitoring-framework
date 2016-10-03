/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.controller.InformationManager;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.ControllerControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.udp.UDPReceiver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayInputStream;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneProducer implements ControllerControlPlane, TransmittingAndReceiving, ReceivingAnnounce  {
    UDPReceiver AnnounceListener;
    UDPTransmitterPool controlTransmittersPool;
    int maxPoolSize;
    int announceListenerPort;
    
    InformationManager resolver;
    
    public AbstractUDPControlPlaneProducer(InformationManager resolver, int port, int maxPoolSize) {
        this.resolver = resolver;
        this.announceListenerPort = port;
        this.maxPoolSize = maxPoolSize;
    }
    

    @Override
    public boolean connect() {
	try {
	    // Creating listener for Announce Messages - only connect if we're not already connected
	    if (AnnounceListener == null) {
                AnnounceListener = new UDPReceiver(this, announceListenerPort);
                AnnounceListener.listen();
            }
            
            if (controlTransmittersPool == null) {
                // creating a pool for Control Messages transmission
                // 8 seems to match the max size of the threadPool created by the RestConsole
                controlTransmittersPool = new UDPTransmitterPool(this, maxPoolSize); 
            }       
            return true;
            
	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();
	    return false;
	}
    }

    @Override
    public boolean disconnect() {
        try {
	    AnnounceListener.end();
	    AnnounceListener = null;
            controlTransmittersPool.disconnect();
	    return true;
	} catch (IOException ieo) {
	    AnnounceListener = null;
	    return false;
	}
    }

    @Override
    public boolean announce() {
        // do nothing currenty
	return true;
    }

    @Override
    public boolean dennounce() {
        // do nothing currenty
	return true;
    }

    @Override
    public abstract Object transmit(ControlPlaneMessage dpm, MetaData metaData) throws Exception;

    
    // this overrides both receivedReply from TransmittingAndReceiving and ReceivingAnnounce
    @Override
    public abstract Object receivedReply(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;
    
    
    @Override
    public InetSocketAddress getControlEndPoint() {
        return null;
    }  
}
