// ControlPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package eu.reservoir.monitoring.core.plane;

import java.net.InetSocketAddress;

/**
 * A ControlPlane.
 * This has the common methods for all 
 */
public interface ControlPlane extends Plane, DataSourceControlService, DataConsumerControlService {
    public InetSocketAddress getControlEndPoint();
}
