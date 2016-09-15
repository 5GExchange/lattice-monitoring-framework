// MongodbConsumer.java
// 

package eu.fivegex.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.*;
//import eu.reservoir.monitoring.core.plane.*;
//import java.io.IOException;

/**
 * A BasicConsumer is an object that is used in application
 * level code.  It has the necessary functionality to act as a consumer
 * and have plugins for each of the data plane, control plane, and
 * info plane.
 */

public class MongodbConsumer extends AbstractDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public MongodbConsumer(String dbAddr, int dbPort, String dbName) {
        // The default way to report a measurement is to print it
        Reporter printReporter =  new PrintReporter();
        Reporter mongoReporter = new MongodbReporter(dbAddr, dbPort, dbName);
        
        addReporter(printReporter);
        addReporter(mongoReporter);
    }
}
