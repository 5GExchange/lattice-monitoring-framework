/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.dataconsumers;

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
public class SimpleControllableDataConsumer {
    // The Basic consumer
    ControllableDataConsumer consumer;

    /*
     * Construct a controllable SimpleControllableDataConsumer
     */
    public SimpleControllableDataConsumer(String dataAddr, 
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          int infoPlaneLocalPort,
                                          String controlAddr,
                                          int controlPort) throws UnknownHostException {
        
	// set up a ControllableDataConsumer
	consumer = new ControllableDataConsumer("controllable-DC");
        System.out.println("Data Consumer ID: " + consumer.getID());

	// set up an IP address for data and control
	InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataAddr), dataPort);
        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(controlAddr), controlPort);

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
        try {
            String dcAddr = null;
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 10000;
            String controlEndPoint = null;
            int controlLocalPort = 2222;
            
            if (args.length == 0) {
                String loopBack = InetAddress.getLoopbackAddress().getHostName();
                System.out.println("No arguments provided - running on loopback: " + loopBack);
                dcAddr = infoHost = controlEndPoint = loopBack;
                new SimpleControllableDataConsumer(dcAddr, dataPort, infoHost, infoRemotePort, infoLocalPort, controlEndPoint, controlLocalPort);
                System.err.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                System.err.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
            } else if (args.length == 5) {  
                Scanner sc = new Scanner(args[0]);
                dataPort = sc.nextInt();
                
                infoHost = args[1];
                
                sc = new Scanner(args[2]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[3]);
                infoLocalPort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                controlLocalPort = sc.nextInt();
                
                dcAddr = controlEndPoint = InetAddress.getLocalHost().getHostName();

                new SimpleControllableDataConsumer(dcAddr, dataPort, infoHost, infoRemotePort, infoLocalPort, controlEndPoint, controlLocalPort);

                System.err.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                System.err.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
            } else {
                System.err.println("usage: SimpleControllableDataConsumer localdataPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        } 

    }
}


