// MongodbConsumer.java
// 

package eu.fivegex.monitoring.appl.dataconsumers;

import eu.fivegex.monitoring.appl.reporters.MongoDBReporter;
import eu.fivegex.monitoring.appl.reporters.ReporterException;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.*;

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
    public MongodbConsumer(String dbAddr, int dbPort, String dbName, String collectionName) throws ReporterException {
        Reporter printReporter =  new PrintReporter();
        Reporter mongoReporter = new MongoDBReporter(dbAddr, dbPort, dbName, collectionName);
        
        addReporter(printReporter);
        addReporter(mongoReporter);
    }
}
