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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayInputStream;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneProducer implements ControllerControlPlane, TransmittingControl, ReceivingAnnounce  {
    //InetSocketAddress address;
    UDPAnnounceReceiver AnnounceListener;
    int announceListenerPort;
    
    InformationManager resolver;
    
    public AbstractUDPControlPlaneProducer(InformationManager resolver, int port) {
        //this.address = null; // we are not connecting to a specific endpoint
        this.resolver = resolver;
        this.announceListenerPort = port;
    }
    

    @Override
    public boolean connect() {
        // this method does nothing as we do not create a Datagramsocket 
        // connected to a specific DS. We will create a different AnnounceListener
        // for each received request in the transmit method implementation
        
        //System.out.println("FT: AbstractUDPControlPLaneProducer.connect");

	try {
	    // only connect if we're not already connected
	    if (AnnounceListener == null) {
                AnnounceListener = new UDPAnnounceReceiver(this, announceListenerPort);
                AnnounceListener.listen();
                return true;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();
	    return false;
	}
    }

    @Override
    public boolean disconnect() {
        /*
        try {
	    AnnounceListener.end();
	    AnnounceListener = null;
	    return true;
	} catch (IOException ieo) {
	    AnnounceListener = null;
	    return false;
	}
        */
    return true;
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

    
    @Override
    public abstract Object receivedReply(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;
    
    
    @Override
    public InetSocketAddress getControlEndPoint() {
        return null;
    }  
}
