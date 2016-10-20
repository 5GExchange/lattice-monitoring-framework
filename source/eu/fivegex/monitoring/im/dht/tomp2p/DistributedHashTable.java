package eu.fivegex.monitoring.im.dht.tomp2p;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Random;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
 * A Distributed Hash Table implementation.
 * Values can be accessed and added from any one
 * of the distributed nodes.
 */

public class DistributedHashTable {
    Peer peer; 

    int localPort = 0;

    /**
     * Constructor a Peer on a specified localPort
     */
    
    public DistributedHashTable(int port)  throws IOException {
	this.localPort = port;

        peer = new PeerMaker(new Number160(new Random())).setPorts(port)
                                                         .makeAndListen();
    }


    /**
     * Start the bootstrap process to a well known peer (address and port)
     */
    
    public void connect(String remAddress, int remPort) throws IOException {
        InetAddress remoteAddress = InetAddress.getByName(remAddress);
        
	FutureBootstrap bootstrap = peer.bootstrap().setInetAddress(remoteAddress)
                                                    .setPorts(remPort)
                                                    .start();
        bootstrap.awaitUninterruptibly();
        if (bootstrap.getBootstrapTo() != null) {
            peer.discover().setPeerAddress(bootstrap.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    /**
     * Close the peer connection
     */
    public void close() throws IOException {
	peer.shutdown();
    }

    /**
     * Get an object out of the DHT.
     */
    public Object get(String aKey) throws IOException, ClassNotFoundException {
	Number160 keyHash = Number160.createHash(aKey);

	FutureDHT futureDHT = peer.get(keyHash).start();
        futureDHT.awaitUninterruptibly();
        
        if (futureDHT.isSuccess()) {
            return futureDHT.getData().getObject();
        }
        else
            throw new IOException("Object not found");
    }

    
    /**
     * Put an object into the DHT.
     */
    
    public DistributedHashTable put(String aKey, Serializable aValue) throws IOException {
        Number160 keyHash = Number160.createHash(aKey);
        peer.put(keyHash).setData(new Data(aValue)).start().awaitUninterruptibly();
	return this;
    }
    

    /**
     * Does the DHT contain a particular Identifier.
     * Returns true if the map contains the specified key and false otherwise.
     */
    public boolean contains(String aKey) throws IOException {
	Number160 keyHash = Number160.createHash(aKey);

	FutureDHT futureDHT = peer.get(keyHash).start();
        futureDHT.awaitUninterruptibly();
        
        if (futureDHT.isSuccess()) {
            return true;
        }
        else
            return false;
    }

    /**
     * Removes the mapping with the specified key.
     */
    public DistributedHashTable remove(String aKey) throws IOException {
	Number160 keyHash = Number160.createHash(aKey);
        peer.remove(keyHash).start();
	return this;
    }
    
  
    public String toString() {
            return peer.toString();
        }


}
