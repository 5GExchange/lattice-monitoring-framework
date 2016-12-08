/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.docker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 *
 * @author uceeftu
 */

//to be refoctored with an interface

public class DockerDataCollector {
    long containerCpuTime;
    long systemCpuTime;
    int coresNumber;
    long usedMemBytes;
    long maxMemBytes;
    
    String containerId;
    
    String dockerURI;
    int dockerPort;
    
    Resty rest;
    
    
    public DockerDataCollector(String dockerURI, int dockerPort, String cId) throws UnknownHostException {
        this.dockerURI = dockerURI;
        this.dockerPort = dockerPort;
        this.containerId = cId;
        initialize(InetAddress.getByName(dockerURI), dockerPort);
    }
    
    private synchronized void initialize(InetAddress addr, int port) {
        this.dockerPort = port;
        dockerURI = "http://" + addr.getHostName() + ":" + Integer.toString(port);
        rest = new Resty();
    }
    
    
    void collectValues() {
        try {
            String uri = dockerURI + "/containers/" + containerId + "/stats?stream=0";
            
            JSONObject jsobj = rest.json(uri).toObject();
            
            this.containerCpuTime=jsobj.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getLong("total_usage");
            //System.out.println(this.containerCpuTime);
            
            this.systemCpuTime=jsobj.getJSONObject("cpu_stats").getLong("system_cpu_usage");
            //System.out.println(this.systemCpuTime);
            
            this.usedMemBytes=jsobj.getJSONObject("memory_stats").getLong("usage");
            //System.out.println(this.usedMemBytes);
            
            this.maxMemBytes=jsobj.getJSONObject("memory_stats").getLong("limit");
            //System.out.println(this.maxMemBytes);
            
            this.coresNumber=jsobj.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getJSONArray("percpu_usage").length();
            //System.out.println(this.coresNumber);
            
            
        } catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }
    }
    
    
    public long getContainerCpuTime() {
       return containerCpuTime;
    }
     
    public long getSystemCpuTime() {
       return systemCpuTime;
    } 

    public int getCoresNumber() {
       return coresNumber;
    }
    
    public long getUsedMemBytes() {
       return usedMemBytes;
    }
    
    public long getMaxMemBytes() {
       return maxMemBytes;
    }
    
    
}
