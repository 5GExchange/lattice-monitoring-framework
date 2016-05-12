package eu.reservoir.monitoring.appl.vee.sap;

import eu.reservoir.monitoring.appl.vee.KPIProbe;
import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.*;
import java.util.List;
import java.util.ArrayList;

public class SAPUsersProbe extends KPIProbe implements Probe
{
    public SAPUsersProbe(String fqn)
    {
        // call super constructor
        super(fqn);

        // set name
        setName(fqn);

        // set data rate
        setDataRate(new EveryNSeconds(10));

        // add a KPI value
        // for com.sap.ci.totalUsers
        addKPI("com.sap.ci.totalUsers", ProbeAttributeType.INTEGER, "n");

        // activate probe
        activateProbe();
    }
	

    /**
     * Collect KPI values for measurement.
     */
    public List<Object> collectKPIValues() {
	try {
	    ArrayList<Object> list = new ArrayList<Object>(1);

            //extract KPI from SAP System
            int totalUsers = (int)(Math.random()*100);


	    // add queueLength to list
	    list.add(new Integer(totalUsers));

	    return list;
	} catch (Exception e) {
	    return null;
	}
    }


}
