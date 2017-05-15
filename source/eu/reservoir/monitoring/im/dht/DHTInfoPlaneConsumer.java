// DHTInfoPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.im.dht;

import eu.fivegex.monitoring.im.delegate.ControlInformationManager;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegate;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.ControllableDataConsumer;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.AnnounceEventListener;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegateInteracter;
import eu.fivegex.monitoring.im.dht.tomp2p.IMNode;

/**
 * A DHTInfoPlaneConsumer is an InfoPlane implementation
 * that collects data from the Information Model data.
 */
public class DHTInfoPlaneConsumer extends AbstractDHTInfoPlane implements InfoPlane, InfoPlaneDelegateInteracter, AnnounceEventListener  {
    private InfoPlaneDelegate infoPlaneDelegate;
    
    AnnounceEventListener listener;
    
    // The hostname of the DHT root.
    String rootHost;

    // The port to connect to
    int rootPort;

    // The local port
    int port;

    /**
     * Constructor for subclasses.
     */
    DHTInfoPlaneConsumer() {
        setInfoPlaneDelegate(new ControlInformationManager(this));
    }


    /**
     * Construct a DHTInfoPlaneConsumer.
     * Connect to the DHT root at hostname on port,
     * and start here on localPort.
     */
    public DHTInfoPlaneConsumer(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new IMNode(localPort, remoteHostname, remotePort);
        imNode.addAnnounceEventListener(this);
    }



   /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	return true;
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	return true;
    }

    /**
     * Consumer can never add a DataSource.
     * Return false
     */
    public boolean addDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never add a Probe.
     * Return false
     */
    public boolean addProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	    return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
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
    public boolean containsDataSource(ID dataSourceID, int timeout) {
        return imNode.containsDataSource(dataSourceID, timeout); 
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
        return imNode.containsDataConsumer(dataConsumerID, timeout);
    }
    
    
    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        infoPlaneDelegate.receivedAnnounceEvent(m);
    }
    
    @Override
    public void setInfoPlaneDelegate(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return this.infoPlaneDelegate;
    }
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        this.listener=l;
    }
    
    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}