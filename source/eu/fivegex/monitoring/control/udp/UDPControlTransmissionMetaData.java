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
    private InetAddress address;
    private int port;

    public UDPControlTransmissionMetaData(InetAddress srcAddress, int port) {
        this.address = srcAddress;
        this.port = port;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(address,port);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return "UDPControlTransmissionMetaData{" + "Address=" + address + ", port=" + port + '}';
    }
    
    
    
}
