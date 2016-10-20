// DHTInfoPlaneRoot.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.im.dht;

import eu.reservoir.monitoring.core.plane.InfoPlane;


/**
 * A DHTInfoPlaneRoot is an InfoPlane implementation
 * that acts as a ROOT for the Information Model data.
 * There needs to be one root for a DHT.
 * The other nodes connect to it.
 */
public class DHTInfoPlaneRoot extends DHTInfoPlaneConsumer implements InfoPlane  {
    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     */
    public DHTInfoPlaneRoot(String localHostname, int localPort) {
	rootHost = localHostname;
	rootPort = localPort;

	imNode = new eu.fivegex.monitoring.im.dht.tomp2p.IMNode(localPort, localHostname, localPort);
    }
}