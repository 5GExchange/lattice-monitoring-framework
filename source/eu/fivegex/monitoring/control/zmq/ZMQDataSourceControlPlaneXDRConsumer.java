/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.zmq;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.reservoir.monitoring.core.*;
import eu.fivegex.monitoring.control.ProbeLoader;
import eu.fivegex.monitoring.control.ProbeLoaderException;
import eu.reservoir.monitoring.core.plane.DataSourceControlPlane;
import java.io.IOException;
import java.net.InetSocketAddress;



public class ZMQDataSourceControlPlaneXDRConsumer extends AbstractZMQControlPlaneXDRConsumer implements DataSourceControlPlane, DataSourceDelegateInteracter {
    DataSourceDelegate dataSourceDelegate;
    
    
    public ZMQDataSourceControlPlaneXDRConsumer(InetSocketAddress router) {
        super(router);
    }
    
    
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (zmqReceiver == null) {
		zmqReceiver  = new ZMQReceiver(this, routerAddress, routerPort);
                zmqReceiver.setIdentity(dataSourceDelegate.getDataSource().getID().toString());
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
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    @Override
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	//System.out.println("DataSource Control Plane Consumer: setDataSource: " + ds);
	dataSourceDelegate = ds;
	return ds;
    }
    
    
    @Override
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws ControlServiceException {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        try {
            LOGGER.info("** invoking loadProbe **");
            ProbeLoader p = new ProbeLoader(probeClassName, probeArgs);
            if (dataSource instanceof ControllableDataSource)
                return ((ControllableDataSource)dataSource).addProbe(p);
            else
                throw new ControlServiceException("Probe cannot be loaded on the DS");
        } catch (ProbeLoaderException ex) {
            throw new ControlServiceException(ex);
        }
    }

    @Override
    public boolean unloadProbe(ID probeID) throws ControlServiceException {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking unloadProbe **");
        Probe p = dataSource.getProbeByID(probeID);
        dataSource.removeProbe(p);
        return true;
    }
    

    @Override
    public String getProbeName(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeName(ID probeID, String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ID getProbeServiceID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeServiceID(ID probeID, ID id) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking setProbeServiceID **");
        dataSource.setProbeServiceID(probeID, id);
        return true;
        }

    @Override
    public ID getProbeGroupID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeGroupID(ID probeID, ID id) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking setProbeGroupID **");
        dataSource.setProbeGroupID(probeID, id);
        return true;
    }

    @Override
    public Rational getProbeDataRate(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeDataRate(ID probeID, Rational dataRate) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking setProbeDataRate **");
        dataSource.setProbeDataRate(probeID, dataRate);
        return true;
    }

    @Override
    public Measurement getProbeLastMeasurement(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Timestamp getProbeLastMeasurementCollection(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean turnOnProbe(ID probeID) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking turnOnProbe **");
        if (!dataSource.isProbeOn(probeID))
            dataSource.turnOnProbe(probeID);
        return true;
    }

    @Override
    public boolean turnOffProbe(ID probeID) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking turnOffProbe **");
        if (dataSource.isProbeOn(probeID))
            dataSource.turnOffProbe(probeID);
        return true;
    }

    @Override
    public boolean isProbeOn(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean activateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deactivateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isProbeActive(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDataSourceInfo(ID id) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking getDataSourceInfo **");
        return dataSource.getName();
    }

    @Override
    public boolean setDataSourceName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}