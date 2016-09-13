/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.distribution.MetaData;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 *
 * @author uceeftu
 */
public class UDPControlTransmissionMetaData implements MetaData, Serializable{
    InetAddress srcAddress;
    int port;
    InetSocketAddress address;

    public UDPControlTransmissionMetaData(InetAddress srcAddress, int port) {
        this.srcAddress = srcAddress;
        this.port = port;
    }

    public InetSocketAddress getInetSocketAddress() {
        address = new InetSocketAddress(srcAddress,port);
        return address;
    }
    
    @Override
    public String toString() {
        return "UDPControlTransmissionMetaData{" + "srcAddress=" + srcAddress + ", port=" + port + '}';
    }
    
    
    
}
