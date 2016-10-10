/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.ControlPlaneConsumerException;
import eu.fivegex.monitoring.control.controller.InformationManager;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
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
                AnnounceListener = new UDPReceiver(this, announceListenerPort, "AnnounceListener");
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
    public void addNewAnnouncedEntity(ID entityID, AbstractAnnounceMessage.EntityType type) {
        System.out.println("New " + type + " with ID " + entityID);
        try {
            Thread.sleep(1000); //needed to wait for the DHT to be updated
        } catch (InterruptedException ex) {
                System.out.println("Thread interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
        }
        resolver.addNewAnnouncedEntity(entityID, type);
    }

    @Override
    public void removeNewDeannouncedEntity(ID entityID, AbstractAnnounceMessage.EntityType type) {
        System.out.println(type + " with ID " + entityID + " is being shutdown");
        try {
            Thread.sleep(1000); //needed to wait for the DHT to be updated
        } catch (InterruptedException ex) {
                System.out.println("Thread interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
        }
        resolver.removeNewDeannouncedEntity(entityID, type);
    }
    
    
    @Override
    public boolean announce() {
        // sending announce messages is not expected for a Control Plane Producer
	return false;
    }

    @Override
    public boolean dennounce() {
        // sending deannounce messages is not expected for a Control Plane Producer
	return false;
    }

    @Override
    public abstract Object synchronousTransmit(ControlPlaneMessage dpm, MetaData metaData) throws IOException, ControlPlaneConsumerException;

    
    @Override
    public abstract Object receivedReply(ByteArrayInputStream bis, MetaData metaData, int seqNo) throws IOException;
    
    
    @Override
    public InetSocketAddress getControlEndPoint() {
        throw new UnsupportedOperationException("Abstract UDP Control Plane Producer: getting control endpoint is not supported");
    }  
}
