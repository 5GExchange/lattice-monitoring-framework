package eu.fivegex.monitoring.im.zmq;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.DataSourceDelegate;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.plane.InfoPlane;

import java.io.IOException;
import eu.reservoir.monitoring.core.ControllableDataConsumer;

/**
 * A DHTDataSourceInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class ZMQDataSourceInfoPlane extends AbstractZMQInfoPlane implements InfoPlane, DataSourceDelegateInteracter {
    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;
    
    ZMQInformationPublisher zmqInformationPublisher;

    // The hostname of the DHT root.
    String remoteHost;

    // The port to connect to
    int remotePort;
    
    /**
     * Construct a ZMQDataSourceInfoPlane.
     * Connect to the Proxy Subscriber at hostname on port,
     * and start here on localPort.
     */
    
    public ZMQDataSourceInfoPlane(String remoteHostname, int remotePort) {
	remoteHost = remoteHostname;
	this.remotePort = remotePort;

	zmqInformationPublisher = new ZMQInformationPublisher(remoteHost, this.remotePort);
    }
     
     
    /**
     * Connect to a delivery mechanism.
     */
    public boolean connect() {
	return zmqInformationPublisher.connect();
    }

    /**
     * Disconnect from a delivery mechanism.
     */
    public boolean disconnect() {
	return zmqInformationPublisher.disconnect();
    }

    @Override
    public String getInfoRootHostname() {
        return zmqInformationPublisher.getRootHostname();
    }
    

    /**
     * Announce that the Data Source is up and running
     */
    public boolean announce() {
	try {
	    DataSource dataSource = dataSourceDelegate.getDataSource();
	    zmqInformationPublisher.addDataSource(dataSource);

	    LOGGER.info("just announced this Data Source " + dataSource.getID());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Un-announce that the Data Source is up and running
     */
    public boolean dennounce() {
        try {
            DataSource dataSource = dataSourceDelegate.getDataSource();
            zmqInformationPublisher.removeDataSource(dataSource);
            
            //infoFormatter.announce(new DeannounceMessage(dataSource.getID(), EntityType.DATASOURCE));
            LOGGER.info("just deannounced this Data Source " + dataSource.getID());
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }


    /**
     * Get the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    /**
     * Set the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	dataSourceDelegate = ds;
	return ds;
    }

    /**
     * Add a DataSource
     */
    public boolean addDataSourceInfo(DataSource ds) {
	return true;
    }

    /**
     * Add a Probe
     */
    public boolean addProbeInfo(Probe p) {
	try {
	    zmqInformationPublisher.addProbe(p);

	    LOGGER.info("just added Probe " + p.getClass());
            LOGGER.debug(p.toString());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }



    /**
     * Add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    zmqInformationPublisher.addProbeAttribute(p, pa);

	    LOGGER.debug("just added ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Modify a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
//	try {
//	    zmqInformationPublisher.modifyDataSource(ds);
//
//	    LOGGER.info("just modified DataSource " + ds);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Modify a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
//	try {
//	    zmqInformationPublisher.modifyProbe(p);
//
//	    LOGGER.info("just modified Probe " + p.getClass());
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Modify a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
//	try {
//	    zmqInformationPublisher.modifyProbeAttribute(p, pa);
//
//	    LOGGER.debug("just modified ProbeAttribute " + p + "." + pa);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }


    /**
     * Remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
//	try {
//	    zmqInformationPublisher.removeDataSource(ds);
//
//	    LOGGER.info("just removed Data Source " + ds);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
//	try {
//	    zmqInformationPublisher.removeProbe(p);
//
//	    LOGGER.info("just removed Probe " + p.getClass());
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
//	try {
//	    zmqInformationPublisher.removeProbeAttribute(p, pa);
//
//	    LOGGER.debug("just removed ProbeAttribute " + p + "." + pa);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        return false;
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        return false;
    }

    @Override
    public boolean containsDataSource(ID dataSourceID, int timeOut) {
        return false;
    }

    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut) {
        return false;
    }
    
    
    // TESTING

    @Override
    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupProbeInfo(Probe probe, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupProbeInfo(ID probeID, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupDataConsumerInfo(ID dataConsumerID, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object lookupReporterInfo(ID reporterID, String info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    
    
    
}