/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo.probes.docker;

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

    // the container name == FQN
    String containerName;
    
    long previousContainerCPUTime;
    long previousSystemCPUTime;
    
    DockerDataCollector ddc;
    
    
    public DockerProbe(String dockerURI, String dockerPort, String probeName, String cId, String cName) throws UnknownHostException { // double check exception management        
        setName(probeName);
        setDataRate(new Rational(360, 1));
        //setDataRate(new EveryNSeconds(3));
        
        this.containerId=cId;
        this.containerName = cName;
        
        ddc = new DockerDataCollector(dockerURI, Integer.valueOf(dockerPort), this.containerId);
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu.percent", ProbeAttributeType.FLOAT, "percent"));
        //addProbeAttribute(new DefaultProbeAttribute(1, "cpu.percent", ProbeAttributeType.LONG, "percent")); //for test
        addProbeAttribute(new DefaultProbeAttribute(2, "mem.used", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "mem.percent", ProbeAttributeType.FLOAT, "percent"));
        
    }
    

    @Override
    public void beginThreadBody() {
	System.err.println("DockerProbe: beginThread " + this.containerName + " with ID " + this.containerId);
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
            
            System.out.println("CPUTime delta: " + containerCpuTimeDelta);
            System.out.println("SystemTime delta: " + systemCpuTimeDelta);
            
            float cpuPercent = 0;
            if (systemCpuTimeDelta > 0)
                cpuPercent = ((float)containerCpuTimeDelta / systemCpuTimeDelta) * ddc.getCoresNumber() * 100;
            
            float memPercent = 0;
            float memMaxBytes = ddc.getMaxMemBytes();
            if (memMaxBytes > 0)
                memPercent = ((float)ddc.getUsedMemBytes() / memMaxBytes) * 100;
            
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(4);

            list.add(new DefaultProbeValue(0, this.containerName));
            list.add(new DefaultProbeValue(1, cpuPercent));
            list.add(new DefaultProbeValue(2, ddc.getUsedMemBytes()));
            list.add(new DefaultProbeValue(3, memPercent));

            ProbeMeasurement m = new ProducerMeasurement(this, list, "Container");
            
            System.out.println(m.getValues());
            
            previousContainerCPUTime = ddc.getContainerCpuTime();
            previousSystemCPUTime = ddc.getSystemCpuTime();
            
            return m;
        }
        catch (TypeException e)
            {
                System.out.println("Error in DockerProbe");
            }
    return null;
    }
    
    
}