/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.distribution.Transmitting;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author uceeftu
 */
public final class UDPTransmitterPool {
    Integer used = 0;
    int maxSize;
    LinkedBlockingQueue<UDPTransmitterSyncReply> socketQueue;
    Transmitting transmitting;

    public UDPTransmitterPool(Transmitting transmitting, int size) throws IOException {
        this.transmitting = transmitting;
        this.maxSize = size;
        this.socketQueue = new LinkedBlockingQueue(size);
        //initPool();   
    }
    
    public void disconnect() throws IOException {
        for (UDPTransmitterSyncReply t: socketQueue)
            t.end();
    }
    
    public UDPTransmitterSyncReply getConnection() throws IOException {
        try {
            synchronized (used) {
                if (socketQueue.isEmpty() && used < maxSize) {
                    used++;
                    return new UDPTransmitterSyncReply(transmitting);
                }
            }
            UDPTransmitterSyncReply t = socketQueue.take();
            synchronized (used) {
                used++;
            }
            return t;
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    public void releaseConnection(UDPTransmitterSyncReply conn) {
        try {
            socketQueue.put(conn);
            synchronized (used) {
                used--;
            }
        } catch (InterruptedException e) { }
    }   
}
