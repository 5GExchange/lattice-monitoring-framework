// UDPDataPlaneProducer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.net.InetSocketAddress;

/**
 * A UDPDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by UDP.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class UDPDataPlaneProducer extends UDPDataPlaneProducerWithNames implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    /**
     * Construct a UDPDataPlaneProducer.
     */
    public UDPDataPlaneProducer(InetSocketAddress addr) {
        super(addr);
    }
}