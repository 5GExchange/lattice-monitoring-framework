/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.controller.InfoResolver;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayInputStream;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneProducer implements ControlPlane, TransmittingControl {
    //InetSocketAddress address;
    //UDPTransmitterAndReceiver udpTransmitterAndReceiver;
    
    InfoResolver resolver;
   
    /*
    public AbstractUDPControlPlaneProducer(InetSocketAddress address, InfoResolver resolver) {
        this.address = address;
        this.resolver = resolver;
    }
    */
    
    public AbstractUDPControlPlaneProducer(InfoResolver resolver) {
        //this.address = null; // we are not connecting to a specific endpoint
        this.resolver = resolver;
    }
    

    @Override
    public boolean connect() {
        // this method does nothing as we do not create a Datagramsocket 
        // connected to a specific DS. We will create a different udpTransmitterAndReceiver
        // for each received request in the transmit method implementation
        
        //System.out.println("FT: AbstractUDPControlPLaneProducer.connect");
        
        /*
	try {
	    // only connect if we're not already connected
	    if (udpTransmitterAndReceiver == null) {
                
                udpTransmitterAndReceiver = new UDPTransmitterAndReceiver(this);
                
		return true;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}
    */
    return true;
    }

    @Override
    public boolean disconnect() {
        /*
        try {
	    udpTransmitterAndReceiver.end();
	    udpTransmitterAndReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    udpTransmitterAndReceiver = null;
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
