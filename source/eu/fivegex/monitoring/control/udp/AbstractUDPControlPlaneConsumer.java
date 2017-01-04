/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;


import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.ReceivingAndReplying;
import eu.reservoir.monitoring.distribution.Transmitting;
import eu.reservoir.monitoring.distribution.udp.UDPReceiver;
import eu.reservoir.monitoring.distribution.udp.UDPTransmitter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneConsumer implements ControlPlane, ReceivingAndReplying, Transmitting {
    
    InetSocketAddress localAddress;
    InetSocketAddress controllerAddress;

    UDPReceiver udpReceiver;
    UDPTransmitter udpAt; 
    
    static Logger LOGGER = LoggerFactory.getLogger("ControlPlaneConsumer");

    public AbstractUDPControlPlaneConsumer(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
        this.controllerAddress = null;
    }
    
    
    public AbstractUDPControlPlaneConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        this.localAddress = localAddress;
        this.controllerAddress = controllerAddress;
    }
   
    @Override
    public abstract boolean announce();
    

    @Override
    public abstract boolean dennounce();

    
    @Override
    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, ReflectiveOperationException;

    
    @Override
    public abstract int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws IOException;
    
    
    @Override
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (udpReceiver == null) {
		UDPReceiver rr = new UDPReceiver(this, localAddress);
		rr.listen();
		udpReceiver = rr;

                if (controllerAddress != null)
                    udpAt = new UDPTransmitter(this, controllerAddress);
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
        try {
	    udpReceiver.end();
	    udpReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    udpReceiver = null;
	    return false;
	}
    }
    
    @Override
    public void eof() {
        disconnect();
    }
    
    @Override
    public void error(Exception e) {
        LOGGER.error("Error: " + e.getMessage());
    }

    @Override
    public boolean transmitted(int id) {
        LOGGER.info("Just sent Announce/Deannounce message");
        return true;
    }
    
    @Override
    public JSONObject getControlEndPoint() {
        JSONObject controlEndPoint = new JSONObject();
        try {
        controlEndPoint.put("address", localAddress.getAddress().getHostAddress());
        controlEndPoint.put("port", localAddress.getPort());
        controlEndPoint.put("type", "socket");
        } catch (JSONException e) {
            return null;
        }
        return controlEndPoint;
    }
}
