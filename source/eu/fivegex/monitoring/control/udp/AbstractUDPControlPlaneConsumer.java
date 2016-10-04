/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;


import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.ReceivingAndReplying;
import eu.reservoir.monitoring.distribution.XDRDataOutputStream;
import eu.reservoir.monitoring.distribution.udp.UDPReceiver;
import eu.reservoir.monitoring.distribution.udp.UDPTransmitter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneConsumer implements ControlPlane, ReceivingAndReplying, TransmittingAnnounce {
    
    InetSocketAddress localAddress;
    InetSocketAddress controllerAddress;

    UDPReceiver udpReceiver;
    UDPTransmitter udpAt; 

    public AbstractUDPControlPlaneConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        this.localAddress = localAddress;
        this.controllerAddress = controllerAddress;
    }
    
    
    

    @Override
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (udpReceiver == null) {
                
                //System.out.println("FT: -------- AbstractUDPControPlaneConsumer.connect - Connecting to the Control Plane ----------");
                
		UDPReceiver rr = new UDPReceiver(this, localAddress);

		rr.listen();
		
		udpReceiver = rr;

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

    protected void announceSerializer(AnnounceMessage message) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);

        // write type
        dataOutput.writeInt(message.getMessageType().getValue());      

        System.out.println("Entity " + message.getEntity());
        // write entity type value as int
        dataOutput.writeInt(message.getEntity().getValue());

        // write entity ID 
        dataOutput.writeLong(message.getEntityID().getMostSignificantBits());
        dataOutput.writeLong(message.getEntityID().getLeastSignificantBits());

        udpAt.transmit(byteStream, 0);
    }
    
    
    @Override
    public abstract boolean announce();
    

    @Override
    public abstract boolean dennounce();

    
    @Override
    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;

    
    @Override
    public abstract int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws Exception;
    
    
    @Override
    public void eof() {
        disconnect();
    }

    @Override
    public InetSocketAddress getControlEndPoint() {
        return this.localAddress;
    }
    
    
}
