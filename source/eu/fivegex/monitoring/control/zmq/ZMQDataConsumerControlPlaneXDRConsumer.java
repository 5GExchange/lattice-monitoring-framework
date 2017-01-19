/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.zmq;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.fivegex.monitoring.control.ReporterLoader;
import eu.fivegex.monitoring.control.ReporterLoaderException;
import eu.reservoir.monitoring.core.DataConsumerInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.DataConsumerControlPlane;
import java.net.InetSocketAddress;
import eu.reservoir.monitoring.core.ControllableDataConsumer;
import java.io.IOException;



public class ZMQDataConsumerControlPlaneXDRConsumer extends AbstractZMQControlPlaneXDRConsumer implements DataConsumerControlPlane, DataConsumerInteracter {
    ControllableDataConsumer dataConsumer;
    
    public ZMQDataConsumerControlPlaneXDRConsumer(InetSocketAddress router) {
        super(router);
    }

    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (zmqReceiver == null) {
		zmqReceiver  = new ZMQReceiver(this, routerAddress, routerPort);
                zmqReceiver.setIdentity(dataConsumer.getID().toString());
                zmqReceiver.connect();
		zmqReceiver.listen();
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
    public boolean announce() {
        return true;
    }

    @Override
    public boolean dennounce() {
        return true;
    }

    @Override
    public ControllableDataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public ControllableDataConsumer setDataConsumer(ControllableDataConsumer dc) {
        this.dataConsumer = dc;
        return this.dataConsumer;
    }  
    
    @Override
    public float getDCMeasurementsRate(ID dcID) {
        LOGGER.info("** invoking getDCMeasurementsRate **");
        return dataConsumer.getMeasurementsRate();
    }
    
    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws ControlServiceException {
        try {
            LOGGER.info("** invoking loadReporter **");            
            ReporterLoader r = new ReporterLoader(reporterClassName, reporterArgs);
            dataConsumer.addReporter(r.getReporter());
            return r.getReporter().getId();
        } catch (ReporterLoaderException e) {
            throw new ControlServiceException(e);
        }
    }

    @Override
    public boolean unloadReporter(ID reporterID) throws ControlServiceException {
        LOGGER.info("** invoking unloadReporter **");
        dataConsumer.removeReporter(dataConsumer.getReporterById(reporterID));
        return true;
    }
}
