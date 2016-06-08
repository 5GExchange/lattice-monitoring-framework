/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.udp;


import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.DataSourceInteracter;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.ControlPlaneReplyMessage;
import eu.reservoir.monitoring.distribution.MetaData;
import eu.reservoir.monitoring.distribution.ReceivingAndReplying;
import eu.reservoir.monitoring.distribution.udp.UDPReceiver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneConsumer implements ControlPlane, DataSourceInteracter, ReceivingAndReplying {
    
    InetSocketAddress address;

    UDPReceiver udpReceiver;
    
    DataSource dataSource;

    public AbstractUDPControlPlaneConsumer(InetSocketAddress address) {
        this.address = address;
    }
    
    
    

    @Override
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (udpReceiver == null) {
                
                System.out.println("FT: -------- AbstractUDPDataPlaneConsumer.connect - Connecting to the Control Plane ----------");
                
		UDPReceiver rr = new UDPReceiver(this, address);

		rr.listen();
		
		udpReceiver = rr;

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
    public abstract boolean announce();
    

    @Override
    public boolean dennounce() {
        // do nothing currenty
	return true;
    }

    
    @Override
    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;

    
    @Override
    public abstract int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws Exception;
    
    
    @Override
    public void eof() {
        disconnect();
    }   
    
    
    @Override
    public void error(Exception e) {
    }
    
    
    @Override
    public DataSource getDataSource() {
        return dataSource;
        }

    
    @Override
    public DataSource setDataSource(DataSource ds) {
        dataSource = ds;
        return dataSource;
        }

    @Override
    public InetSocketAddress getControlEndPoint() {
        return this.address;
    }
    
    
}
