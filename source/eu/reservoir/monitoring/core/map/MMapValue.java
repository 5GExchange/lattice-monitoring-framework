// MMapValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2012

package eu.reservoir.monitoring.core.map;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import java.io.Serializable;

/**
 * An interface for a value in a MMap.
 */
public interface MMapValue extends Serializable {
    /**
     * Get the actual value.
     */
    public Object getValue();

    /**
     * Get the type for a MMapValue
     * e.g. INTEGER
     */
    public ProbeAttributeType getType();
}
