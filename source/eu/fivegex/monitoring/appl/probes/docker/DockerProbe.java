/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.docker;

import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.*;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author uceeftu
 */
public class DockerProbe extends AbstractProbe implements Probe{
    
    // the container ID
    String containerId;

    // the container name
    String resourceId;
    
    long previousContainerCPUTime;
    long previousSystemCPUTime;
    
    DockerDataCollector ddc;
    
    
    public DockerProbe(String dockerURI, String dockerPort, String probeName, String cId, String resourceId) throws UnknownHostException { // double check exception management        
        setName(probeName);
        setDataRate(new Rational(360, 1));
        //setDataRate(new EveryNSeconds(3));
        
        this.containerId=cId;
        this.resourceId = resourceId;
        
        ddc = new DockerDataCollector(dockerURI, Integer.valueOf(dockerPort), this.containerId);
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name")); // we need to double check what info is needed here
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu_percent", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "mem_used", ProbeAttributeType.LONG, "bytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "mem_percent", ProbeAttributeType.FLOAT, "percent"));
        
        addProbeAttribute(new DefaultProbeAttribute(4, "tx_bytes", ProbeAttributeType.LONG, "bytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "rx_bytes", ProbeAttributeType.LONG, "bytes"));
        
    }
    

    @Override
    public void beginThreadBody() {
        ddc.collectValues();
        previousContainerCPUTime = ddc.getContainerCpuTime();
        previousSystemCPUTime = ddc.getSystemCpuTime();
    }
    
    @Override
    public ProbeMeasurement collect() {
        try {
            //float scaleFactor = (float)rationalToMillis(getDataRate());
            
            ddc.collectValues();
            
            //System.out.println("Current CPU time: " + ddc.getContainerCpuTime());
            //System.out.println("Old CPU time: " + previousContainerCPUTime);
            
            //System.out.println("Current system time: " + ddc.getSystemCpuTime());
            //System.out.println("Old system time: " + previousSystemCPUTime);
            
            
            long containerCpuTimeDelta = ddc.getContainerCpuTime() - previousContainerCPUTime;
            long systemCpuTimeDelta = ddc.getSystemCpuTime() - previousSystemCPUTime;
            
            //System.out.println("CPUTime delta: " + containerCpuTimeDelta);
            //System.out.println("SystemTime delta: " + systemCpuTimeDelta);
            
            float cpuPercent = 0;
            if (systemCpuTimeDelta > 0)
                //this is according to 'docker stats' source code: TODO test formula on multiple cores
                cpuPercent = ((float)containerCpuTimeDelta / systemCpuTimeDelta) * ddc.getCoresNumber() * 100; 
            
            float memPercent = 0;
            float memMaxBytes = ddc.getMaxMemBytes();
            if (memMaxBytes > 0)
                memPercent = ((float)ddc.getUsedMemBytes() / memMaxBytes) * 100;
            
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(4);

            list.add(new DefaultProbeValue(0, this.resourceId));
            list.add(new DefaultProbeValue(1, cpuPercent));
            list.add(new DefaultProbeValue(2, ddc.getUsedMemBytes()));
            list.add(new DefaultProbeValue(3, memPercent));
            
            list.add(new DefaultProbeValue(4, ddc.getTxBytes()));
            list.add(new DefaultProbeValue(5, ddc.getRxBytes()));

            ProbeMeasurement m = new ProducerMeasurement(this, list, "Container");
            
            System.out.println(m.getValues());
            
            previousContainerCPUTime = ddc.getContainerCpuTime();
            previousSystemCPUTime = ddc.getSystemCpuTime();
            
            return m;
        }
        catch (Exception e)
            {
                System.out.println("Error in DockerProbe" + e.getMessage());
            }
    return null;
    }   
}
