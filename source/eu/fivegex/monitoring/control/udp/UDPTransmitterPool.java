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
    volatile Integer usedObjects;
    int maxSize;
    LinkedBlockingQueue<UDPTransmitterSyncReply> socketQueue;
    Transmitting transmitting;

    public UDPTransmitterPool(Transmitting transmitting, int size) throws IOException {
        this.transmitting = transmitting;
        this.maxSize = size;
        this.socketQueue = new LinkedBlockingQueue(size);
        this.usedObjects = 0;
    }
    
    public void disconnect() throws IOException {
        for (UDPTransmitterSyncReply t: socketQueue)
            t.end();
    }
    
    public UDPTransmitterSyncReply getConnection() throws IOException, InterruptedException {
        synchronized (usedObjects) {
            if (socketQueue.isEmpty() && usedObjects < maxSize) {
                usedObjects++;
                return new UDPTransmitterSyncReply(transmitting);
            }
        }
        UDPTransmitterSyncReply t = socketQueue.take();
        synchronized (usedObjects) {
            usedObjects++;
        }
        return t;
    }
    
    public void releaseConnection(UDPTransmitterSyncReply conn) throws InterruptedException {
        socketQueue.put(conn);
        synchronized (usedObjects) {
            usedObjects--;
        }
    }   
}
