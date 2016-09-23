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

import eu.fivegex.monitoring.control.udp.UDPDataConsumerControlPlaneConsumer;
import eu.reservoir.monitoring.core.DataConsumerInteracter;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumer;
import eu.reservoir.monitoring.im.dht.DHTDataConsumerInfoPlane;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class DataConsumerWithMeasurementRate {
    // The Basic consumer
    ControllableDataConsumer consumer;

    /*
     * Construct a controllable DataConsumerWithMeasurementRate
     */
    public DataConsumerWithMeasurementRate(String addr, int dataPort) throws UnknownHostException{
        String localControlEndPoint="localhost";
        int controlPlaneLocalPort=2222;
        
        String infoPlaneRootName = "localhost";
        int infoPlaneRootPort = 6699;
        int infoPlaneLocalPort = 10000;
        
        
	// set up a ControllableDataConsumer
	consumer = new ControllableDataConsumer("controllable-DC");
        System.out.println("Data Consumer ID: " + consumer.getID());

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);
        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);

	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumer(address));
        
        ControlPlane controlPlane = new UDPDataConsumerControlPlaneConsumer(ctrlAddress);
        ((DataConsumerInteracter)controlPlane).setDataConsumer(consumer);
        consumer.setControlPlane(controlPlane);
        
        InfoPlane infoPlane = new DHTDataConsumerInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort);
        ((DataConsumerInteracter)infoPlane).setDataConsumer(consumer);
        consumer.setInfoPlane(infoPlane);

	consumer.connect();
    }

    public static void main(String [] args) {
        String currentHost="localhost";
        int port = 22997;
        try {
            currentHost = InetAddress.getLocalHost().getHostName();
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
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        } 

    }
}


