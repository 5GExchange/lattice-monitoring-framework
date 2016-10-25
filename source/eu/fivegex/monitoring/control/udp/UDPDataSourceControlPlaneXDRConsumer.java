/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.reservoir.monitoring.core.*;
import eu.fivegex.monitoring.control.ProbeLoader;
import eu.fivegex.monitoring.control.ProbeLoaderException;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.DataSourceControlPlane;
import eu.reservoir.monitoring.core.plane.DeannounceMessage;
import java.io.IOException;
import java.net.InetSocketAddress;



public class UDPDataSourceControlPlaneXDRConsumer extends AbstractUDPControlPlaneXDRConsumer implements DataSourceControlPlane, DataSourceDelegateInteracter {
    DataSourceDelegate dataSourceDelegate;
    
    
    public UDPDataSourceControlPlaneXDRConsumer(InetSocketAddress localAddress) {
        super(localAddress);
    }
    
    public UDPDataSourceControlPlaneXDRConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
    }
    

    @Override
    public boolean announce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.debug("invoking announce for Data Source " + dataSource.getID());
        
        AbstractAnnounceMessage message = new AnnounceMessage(dataSource.getID(), AbstractAnnounceMessage.EntityType.DATASOURCE);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while announcing Data Source" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean dennounce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.debug("invoking deannounce for Data Source " + dataSource.getID());
        
        AbstractAnnounceMessage message = new DeannounceMessage(dataSource.getID(), AbstractAnnounceMessage.EntityType.DATASOURCE);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while deannouncing Data Source" + e.getMessage());
            return false;
        }
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
        if (!dataSource.isProbeOn(probeID)) {
            dataSource.turnOnProbe(probeID);
            return true;
        }
        else 
            return true;
    }

    @Override
    public boolean turnOffProbe(ID probeID) {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("** invoking turnOffProbe **");
        if (dataSource.isProbeOn(probeID)) {
            dataSource.turnOffProbe(probeID);
            return true;
        }
        else
            return false;
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