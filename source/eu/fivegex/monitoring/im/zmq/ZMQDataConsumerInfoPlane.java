package eu.fivegex.monitoring.im.zmq;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.io.IOException;
import eu.reservoir.monitoring.core.ControllableDataConsumer;
import eu.reservoir.monitoring.core.ControllableReporter;
import eu.reservoir.monitoring.core.DataConsumerInteracter;

/**
 * A ZMQDataSourceInfoPlane is an InfoPlane implementation
 that sends the Information Model data for a Data Source.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class ZMQDataConsumerInfoPlane extends AbstractZMQInfoPlane implements InfoPlane, DataConsumerInteracter {
    ControllableDataConsumer dataConsumer;
    
    // The hostname of the Subscriber.
    String remoteHost;

    // The port of the Subscriber
    int remotePort;
    
    /**
     * Construct a ZMQDataConsumerInfoPlane.
     * Connect to the Proxy Subscriber at hostname on port,
     * and start here on localPort.
     */
    
    public ZMQDataConsumerInfoPlane(String remoteHostname, int remotePort) {
	remoteHost = remoteHostname;
	this.remotePort = remotePort;

	zmqPublisher = new ZMQPublisher(remoteHost, remotePort);
        zmqSubscriber = new ZMQSubscriber(remoteHost, remotePort + 1, "info.probe", zmqPublisher.getContext()); //reusing context
    }
     
     
    /**
     * Connect to a delivery mechanism.
     */
    public boolean connect() {
	return zmqPublisher.connect() && zmqSubscriber.connectAndListen();
    }

    /**
     * Disconnect from a delivery mechanism.
     */
    public boolean disconnect() {
        zmqPublisher.disconnect();
        zmqSubscriber.disconnect();
        return true;
    }

    @Override
    public String getInfoRootHostname() {
        return zmqPublisher.getRootHostname();
    }
    
    /**
     * Announce that the Data Consumer is up and running
     */
    public boolean announce() {
        LOGGER.info("Announcing this Data Consumer " + dataConsumer.getID());
        return addDataConsumerInfo(dataConsumer);
    }

    /**
     * Un-announce that the Data Consumer is up and running
     */
    public boolean dennounce() {
        LOGGER.info("Deannouncing Data Consumer " + dataConsumer.getID());
        return removeDataConsumerInfo(dataConsumer);
    }

    @Override
    public ControllableDataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public ControllableDataConsumer setDataConsumer(ControllableDataConsumer dc) {
        this.dataConsumer = dc;
        return dataConsumer;
    }
   
    
    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        try {
            zmqPublisher.addDataConsumer(dc);
            LOGGER.info("just added Data Consumer " + dc.getName());
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        try {
            zmqPublisher.addReporter((ControllableReporter)r);
            LOGGER.info("just added Reporter " + ((ControllableReporter)r).getName());
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        try {
            zmqPublisher.removeDataConsumer(dc);
            LOGGER.info("just removed Data Consumer " + dc.getName());
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        try {
            zmqPublisher.removeReporter((ControllableReporter)r);
            LOGGER.info("just removed Reporter " + ((ControllableReporter)r).getName());
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }
    
    
    /* these methods always return false here as they are meant to be used by 
     * a Data Source */
    
    @Override
    public boolean addDataSourceInfo(DataSource ds) {
        return false;
    }


    @Override
    public boolean addProbeInfo(Probe p) {
	return false;
    }

    
    @Override
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    
    @Override
    public boolean modifyDataSourceInfo(DataSource ds) {
	return false;
    }


    @Override
    public boolean modifyProbeInfo(Probe p) {
        return false;
    }

    
    @Override
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    
    @Override
    public boolean removeDataSourceInfo(DataSource ds) {
        return false;
    }

    
    @Override
    public boolean removeProbeInfo(Probe p) {
	return false;
    }

    
    @Override
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }
}