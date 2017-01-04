package eu.fivegex.monitoring.im.zmq;

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

/**
 * A ZMQInfoPlaneConsumer is an InfoPlane implementation
 * that collects data from the Information Model data.
 */
public class ZMQInfoPlaneConsumer extends AbstractZMQInfoPlane implements InfoPlane, InfoPlaneDelegateInteracter, AnnounceEventListener  {
    private InfoPlaneDelegate infoPlaneDelegate;
    
    AnnounceEventListener listener;
    
    ZMQInformationSubscriber zmqInformationSubscriber;
    
    // The local port
    int port;

    /**
     * Constructor for subclasses.
     */
    private ZMQInfoPlaneConsumer() {
        setInfoPlaneDelegateInteracter(new ControlInformationManager(this));
        // setting the announce listener to the InfoPlaneDelegate
        listener = infoPlaneDelegate;
    }


    /**
     * Construct a ZMQInfoPlaneConsumer.
     */
    public ZMQInfoPlaneConsumer(int localPort) {
        this();
	port = localPort;
        zmqInformationSubscriber = new ZMQInformationSubscriber(port);
        zmqInformationSubscriber.addAnnounceEventListener(listener);
    }
    

    /**
     * Connect to a delivery mechanism.
     */
    @Override
    public boolean connect() {
	return zmqInformationSubscriber.bindAndListen();
    }

    
    /**
     * Disconnect from a delivery mechanism.
     */
    @Override
    public boolean disconnect() {
	return zmqInformationSubscriber.disconnect();
    }

    
    @Override
    public String getInfoRootHostname() {
        return zmqInformationSubscriber.getRootHostname();
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
    
    // Lookup methods
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeout) {
        return zmqInformationSubscriber.containsDataSource(dataSourceID); 
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
        return zmqInformationSubscriber.containsDataConsumer(dataConsumerID);
    }
    
    
    @Override
    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
        return zmqInformationSubscriber.getDataSourceInfo(dataSource.getID(), info);
    }

    @Override
    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
        return zmqInformationSubscriber.getDataSourceInfo(dataSourceID, info);
    }

    @Override
    public Object lookupProbeInfo(Probe probe, String info) {
        return zmqInformationSubscriber.getProbeInfo(probe.getID(), info);
    }

    @Override
    public Object lookupProbeInfo(ID probeID, String info) {
        return zmqInformationSubscriber.getProbeInfo(probeID, info);
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
    
    
    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        infoPlaneDelegate.receivedAnnounceEvent(m);
    }
    
    @Override
    public void setInfoPlaneDelegateInteracter(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegateInteracter() {
        return this.infoPlaneDelegate;
    }
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        this.listener=l;
    }
    
    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}