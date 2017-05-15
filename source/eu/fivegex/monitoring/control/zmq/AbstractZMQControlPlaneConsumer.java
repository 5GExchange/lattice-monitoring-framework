/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.zmq;


import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.ReceivingAndReplying;
import eu.reservoir.monitoring.distribution.Transmitting;
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
public abstract class AbstractZMQControlPlaneConsumer implements ControlPlane, ReceivingAndReplying, Transmitting {
    ZMQReceiver zmqReceiver;
    String routerAddress;
    int routerPort;
    
    static Logger LOGGER = LoggerFactory.getLogger("ControlPlaneConsumer");

    public AbstractZMQControlPlaneConsumer(InetSocketAddress router) {
        this.routerAddress = router.getAddress().getHostName();
        this.routerPort = router.getPort();
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
    public abstract boolean connect(); 
    

    @Override
    public boolean disconnect() {
        try {
	    zmqReceiver.end();
	    zmqReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    zmqReceiver = null;
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
        controlEndPoint.put("type", "zmq");
        } catch (JSONException e) {
            return null;
        }
        return controlEndPoint;
    }
}
