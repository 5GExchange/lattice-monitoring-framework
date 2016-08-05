/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

/**
 *
 * @author uceeftu
 */

import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class DataConsumerWithMeasurementRate {
    // The Basic consumer
    BasicConsumerWithRateReporter consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public DataConsumerWithMeasurementRate(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumerWithRateReporter(5);

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumer(address));

	consumer.connect();

    }

    public static void main(String [] args) {
        String currentHost="localhost";
        int port = 22997;
        try {
            currentHost = InetAddress.getLocalHost().getHostName();   
        } catch (Exception e) {
        } 
        
	if (args.length == 0) {
	    new DataConsumerWithMeasurementRate(currentHost, port);
	    System.err.println("DataConsumerWithMeasurementRate listening on " + currentHost + "/" + port);
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	    new DataConsumerWithMeasurementRate(addr, port);

	    System.err.println("DataConsumerWithMeasurementRate listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: DataConsumerWithMeasurementRate localhost port");
	    System.exit(1);
	}
    }

}


