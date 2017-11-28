// RandomProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.fivegex.monitoring.appl.probes;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.datarate.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

/**
 * A probe that returns a random float.
 */
public class RandomProbe extends AbstractProbe implements Probe  {
    Random randomNo;
    int scaleFactor;

    
    public RandomProbe(String name, String fieldName, String scaleFactor) {
        this(name, fieldName, Integer.valueOf(scaleFactor));
    }
    
    /*
     * Construct a probe
     */
    public RandomProbe(String name, String fieldName, int scaleFactor) {
        setName(name);
        setDataRate(new EveryNSeconds(5));
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    
    public RandomProbe(String name, String fieldName, Integer scaleFactor) {
        setName(name);
        setDataRate(new EveryNSeconds(5));
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }
    
    


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(1);

	    float next = scaleFactor + (randomNo.nextFloat() * (scaleFactor / 5));

	    //System.err.println("rand = " + next);

            list.add(new DefaultProbeValue(0, InetAddress.getLocalHost().getHostName()));
	    list.add(new DefaultProbeValue(1, next));

	    return new ProducerMeasurement(this, list); // we could use the constructor that includes the Measurement type
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    return null;
	}
    }

}
