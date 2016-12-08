// UDPDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.core.MeasurementReporting;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.net.InetSocketAddress;

public class UDPDataPlaneConsumer extends UDPDataPlaneConsumerWithNames implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a UDPDataPlaneConsumer.
     */
    public UDPDataPlaneConsumer(InetSocketAddress addr) {
        super(addr);
    }
    
    /**
     * Construct a UDPDataPlaneConsumer.
     */
    public UDPDataPlaneConsumer(int port) {
        super(port);
    }

}