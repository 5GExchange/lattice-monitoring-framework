// ControlPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package eu.reservoir.monitoring.core.plane;

import us.monoid.json.JSONObject;

/**
 * A ControlPlane.
 * This has the common methods for all 
 */
public interface ControlPlane extends Plane {
    public JSONObject getControlEndPoint();
}
