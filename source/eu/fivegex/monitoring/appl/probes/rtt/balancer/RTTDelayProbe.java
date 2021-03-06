/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.rtt.balancer;

import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.DefaultProbeAttribute;
import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.TypeException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class RTTDelayProbe extends AbstractProbe implements Probe {
    Dispatcher dispatcher;
    
    LinkedBlockingQueue<String> queue;
    
    private Logger LOGGER = LoggerFactory.getLogger(RTTDelayProbe.class);
    
    String containerId;
    String resourceId;
    

    public RTTDelayProbe(String probeName, String cId, String resId) {
        setName(probeName);
        setDataRate(new EveryNSeconds(5));
        
        containerId = cId;
        resourceId = resId;
    
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "RTT", ProbeAttributeType.FLOAT, "milliseconds"));
        
    }
    
    

    
    @Override
    public void beginThreadBody() {
        dispatcher = Dispatcher.getInstance();
        dispatcher.addNewQueue(containerId);
        queue = dispatcher.getContainerQueue(containerId);
        dispatcher.start();
    }
    
    
    @Override
    public void endThreadBody() {
        dispatcher.deleteQueue(containerId);
        dispatcher.stop();
    }


    
    @Override
    public ProbeMeasurement collect() {
        
        String receiverReport;
        try {
            receiverReport = queue.take();
            LOGGER.debug("retrieved: " + receiverReport);
            
            String[] elements = receiverReport.split(" ");

            String retrievedContainerId = elements[1].trim();
            String kpiName = elements[2].trim();
            Float RTTvalue  = Float.parseFloat(elements[3].trim());
            //String retrievedResourceId = elements[4].trim(); not sure if we need this
            
            if (retrievedContainerId.equals(containerId) && kpiName.equalsIgnoreCase("RTT")) {
                ArrayList<ProbeValue> list = new ArrayList<>(2);
                
                list.add(new DefaultProbeValue(0, resourceId));
                list.add(new DefaultProbeValue(1, RTTvalue));

                ProbeMeasurement m = new ProducerMeasurement(this, list, "Link");
                LOGGER.debug("Returning measurement: " + m.toString());
                return m; 
            }   
        } catch (InterruptedException ie) {
            LOGGER.error("Error thread interrupted: " + ie.getMessage());
            super.threadRunning = false;
            
        } catch (TypeException te) {
            LOGGER.error("Error while adding probe attribute: " + te.getMessage());
        } catch (Exception e) {
            for (int i=0; i< e.getStackTrace().length; i++)
                LOGGER.error(e.getStackTrace()[i].toString());
            LOGGER.error("Error " + e.getMessage());
        }
        return null;
    }
}



