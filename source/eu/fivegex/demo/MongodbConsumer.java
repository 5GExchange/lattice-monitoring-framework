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
    public MongodbConsumer() {
        // The default way to report a measurement is to print it
        Reporter reporter =  new PrintReporter();

        addReporter(reporter);
    }

    public MongodbConsumer(String addr, int dataPort) {
        Reporter reporter =     new MongodbReporter();

        addReporter(reporter);

    }

}
