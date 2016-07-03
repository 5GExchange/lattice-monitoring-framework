// ResponseTimeEmulatorUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.fivegex.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.control.udp.UDPControlPlaneConsumer;
import eu.reservoir.monitoring.core.AbstractDataSource;
import eu.reservoir.monitoring.core.DataSourceInteracter;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneProducer;
import eu.reservoir.monitoring.im.dht.DHTInfoPlane;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane
 */
public class SimpleDataSource {
    // The DataSource, TODO: maybe we should modify the definition of the DataSource interface to include loadProbe
    // or creating a ControllableDataSource interface
    AbstractDataSource ds;

    /*
     * Construct a ResponseTimeEmulator.
     */
    public SimpleDataSource(String myHostName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           int infoPlaneLocalPort,
                           int controlPlaneLocalPort) throws UnknownHostException {
	// set up data source
	ds = new BasicDataSource(myHostName);

        
        System.out.println("Sending data to: " + dataConsumerName + ":" + dataConsumerPort);
        
        System.out.println("Using local host name: " + myHostName);
        
        System.out.println("Connecting to InfoPlaneRoot using : " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to ControPlane using: " + controlPlaneLocalPort + ":" + myHostName);
        
	// set up an IPaddress for data
	InetSocketAddress DataAddress = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);

        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(myHostName), controlPlaneLocalPort);
        
	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducer(DataAddress));
        
        // set up control plane: a data source is a consumer of Control Messages
        ControlPlane controlPlane = new UDPControlPlaneConsumer(ctrlAddress);
        ((DataSourceInteracter)controlPlane).setDataSource(ds);
        
        ds.setControlPlane(controlPlane);
        
        //the root of the DHT is by default on dataConsumerPort 6699
        ds.setInfoPlane(new DHTInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort));
        
	ds.connect();
        
    }


    public static void main(String [] args) throws InterruptedException {
        System.out.println(args.length);
	try {
            String currentHost = "localhost";
            String dataConsumerAddr = "localhost";
            int dataConsumerPort = 22997;
            String infoHost="localhost";
            int infoRemotePort= 6699;
            int infoLocalPort = 9999;
            int controlLocalPort = 1111;
            
            
            if (args.length == 0) {
                // use existing settings
            } else if (args.length == 7) {
                currentHost = args[0];
                
                dataConsumerAddr = args[1];
                
                Scanner sc = new Scanner(args[2]);
                dataConsumerPort = sc.nextInt();
                
                infoHost = args[3];
                
                sc = new Scanner(args[4]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[5]);
                infoLocalPort = sc.nextInt();
                
                sc= new Scanner(args[6]);
                controlLocalPort = sc.nextInt();
                
            } else {
                System.err.println("use: SimpleDataSource localAddress dcAddress dcPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }
            
            // try and get the real current hostname
            //String currentHost ="localhost";
            
            //try {
            //    currentHost = InetAddress.getLocalHost().getHostName();   
            //    System.out.println(currentHost);
            //} catch (Exception e) {
            //}
          
            // we got a hostname
            SimpleDataSource hostMon = new SimpleDataSource(currentHost, dataConsumerAddr, dataConsumerPort, infoHost, infoRemotePort, infoLocalPort, controlLocalPort);
           
        } catch (Exception ex) {
            System.out.println("Error while starting the Data Source " + ex.getMessage());
	}
        
        
        
        
    }
}
