/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.reporters;

import eu.reservoir.monitoring.core.AbstractReporter;
import eu.reservoir.monitoring.core.Measurement;

/**
 *
 * @author uceeftu
 */
public class CassandraReporter extends AbstractReporter {
    String ServerAddress;
    int serverPort;
    
    public CassandraReporter(String address, String port) {
        super("cassandra-reporter");
        this.ServerAddress = address;
        this.serverPort = Integer.valueOf(port);
    }
    
    @Override
    public void report(Measurement m) {
        System.out.println("Writing to Cassandra...");
    }
    
}
