
//BasicConsumer.java
//Author: Stuart Clayman
//Email: sclayman@ee.ucl.ac.uk
//Date: Sept 2009

package eu.fivegex.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.appl.PrintReporter;

import java.io.IOException;

/**
* A BasicConsumer is an object that is used in application 
* level code.  It has the necessary functionality to act as a consumer
* and have plugins for each of the data plane, control plane, and
* info plane.
*/

public class MongodbConsumer extends AbstractDataConsumer implements MeasurementReceiver {

	public MongodbConsumer() {
		// The default way to report a measurement is to print it
		Reporter reporter =  new PrintReporter();
	        
		addReporter(reporter);
	}
 /**
  * Construct a MongoDBConsumer.
  */
	public MongodbConsumer(String addr, int dataPort) {
	
		Reporter reporter =  new MongodbReporter();
     
		addReporter(reporter);
	}

}