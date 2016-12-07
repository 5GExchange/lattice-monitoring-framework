// InfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package eu.reservoir.monitoring.core.plane;


/**
 * A InfoPlane.
 * This has the common methods for all 
 */
public interface InfoPlane extends Plane, InfoService {
    public String getInfoRootHostname();
}
