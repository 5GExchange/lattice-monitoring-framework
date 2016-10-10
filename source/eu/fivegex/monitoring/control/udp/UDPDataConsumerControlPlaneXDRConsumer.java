/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.fivegex.monitoring.control.ReporterLoader;
import eu.fivegex.monitoring.control.ReporterLoaderException;
import eu.reservoir.monitoring.core.DataConsumer;
import eu.reservoir.monitoring.core.DataConsumerInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage;
import eu.reservoir.monitoring.core.plane.AnnounceMessage;
import eu.reservoir.monitoring.core.plane.DataConsumerControlPlane;
import eu.reservoir.monitoring.core.plane.DeannounceMessage;
import java.io.IOException;
import java.net.InetSocketAddress;



public class UDPDataConsumerControlPlaneXDRConsumer extends AbstractUDPControlPlaneXDRConsumer implements DataConsumerControlPlane, DataConsumerInteracter {
    DataConsumer dataConsumer;
    
    public UDPDataConsumerControlPlaneXDRConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
    }


    @Override
    public boolean announce() {
        System.out.println("UDP Control Plane Consumer: announcing Data Consumer");
        AbstractAnnounceMessage message = new AnnounceMessage(dataConsumer.getID(), AbstractAnnounceMessage.EntityType.DATACONSUMER);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            System.out.println("Error while announcing Data Consumer" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean dennounce() {
        System.out.println("UDP Control Plane Consumer: deannouncing Data Consumer");
        AbstractAnnounceMessage message = new DeannounceMessage(dataConsumer.getID(), AbstractAnnounceMessage.EntityType.DATACONSUMER);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            System.out.println("Error while deannouncing Data Consumer" + e.getMessage());
            return false;
        }
    }
    
    

    @Override
    public DataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public DataConsumer setDataConsumer(DataConsumer dc) {
        this.dataConsumer = dc;
        return this.dataConsumer;
    }  
    
    @Override
    public float getDCMeasurementsRate(ID dcID) {
        System.out.println("******* UDPControlPlaneConsumer -> getDCMeasurementsRate");
        return dataConsumer.getMeasurementsRate();
    }
    
    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws ControlServiceException {
        try {
            System.out.println("******* UDPControlPlaneConsumer -> loadReporter");
            ReporterLoader r = new ReporterLoader(reporterClassName, reporterArgs);
            dataConsumer.addReporter(r.getReporter());
            return r.getReporter().getId();
        } catch (ReporterLoaderException e) {
            throw new ControlServiceException(e);
        }
    }

    @Override
    public boolean unloadReporter(ID reporterID) throws ControlServiceException {
        System.out.println("******* UDPControlPlaneConsumer -> unloadReporter");
        dataConsumer.removeReporter(dataConsumer.getReporterById(reporterID));
        return true;
    }
}
