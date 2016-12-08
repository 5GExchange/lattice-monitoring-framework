/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.ControlPlaneConsumerException;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegate;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegateInteracter;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.AnnounceEventListener;
import eu.reservoir.monitoring.core.plane.ControllerControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.Receiving;
import eu.reservoir.monitoring.distribution.udp.UDPReceiver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneProducer implements 
        ControllerControlPlane, SynchronousTransmitting, Receiving, InfoPlaneDelegateInteracter  {
    
    UDPReceiver AnnounceListener;
    UDPTransmitterPool controlTransmittersPool;
    int maxPoolSize;
    int announceListenerPort;
    
    InfoPlaneDelegate infoPlaneDelegate;
    AnnounceEventListener listener;
    
    static Logger LOGGER = LoggerFactory.getLogger("UDPControlPlaneProducer");
    
    public AbstractUDPControlPlaneProducer(int maxPoolSize) {
        this.announceListenerPort = -1;
        this.maxPoolSize = maxPoolSize;
    }
    
    
    public AbstractUDPControlPlaneProducer(int port, int maxPoolSize) {
        this.announceListenerPort = port;
        this.maxPoolSize = maxPoolSize;
    }
    

    @Override
    public boolean connect() {
	try {
	    // Creating listener for Announce Messages - only connect if we're not already connected
	    if (AnnounceListener == null && announceListenerPort != -1) {
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
	    LOGGER.error("Error while connecting " + ioe.getMessage());
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
    

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegateInteracter() {
        return infoPlaneDelegate;
    }

    @Override
    public void setInfoPlaneDelegateInteracter(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }
    
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}
