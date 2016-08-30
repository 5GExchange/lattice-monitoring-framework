// ResponseTimeEmulatorUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.fivegex.demo;

import eu.fivegex.demo.probes.docker.DockerProbe;
import eu.reservoir.demo.RandomProbe;
import eu.reservoir.monitoring.control.udp.UDPControlPlaneConsumer;
import eu.reservoir.monitoring.core.ControllableDataSource;
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
public class SimpleDataSourceTest {
    ControllableDataSource ds;

    /*
     * Construct a SimpleDataSource with no loaded probes.
     */
    public SimpleDataSourceTest(String myDsName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           int infoPlaneLocalPort,
                           String localControlEndPoint,
                           int controlPlaneLocalPort) throws UnknownHostException {
        
        
	// set up data source
	ds = new ControllableBasicDataSource(myDsName);
        
        System.out.println("Sending data to: " + dataConsumerName + ":" + dataConsumerPort);
        
        System.out.println("Using local host name: " + myDsName);
        
        System.out.println("Connecting to InfoPlaneRoot using : " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to ControPlane using: " + controlPlaneLocalPort + ":" + myDsName);
        
        System.out.println("DataSource ID: " + ds.getID());
        
	// set up an IPaddress for data
	InetSocketAddress DataAddress = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);

        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);
        
	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducer(DataAddress));
        
        // set up control plane: a data source is a consumer of Control Messages
        ControlPlane controlPlane = new UDPControlPlaneConsumer(ctrlAddress);
        ((DataSourceInteracter)controlPlane).setDataSource(ds);
        
        ds.setControlPlane(controlPlane);
        
        //the root of the DHT is by default on infoPlaneRootName 6699
        ds.setInfoPlane(new DHTInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort));
        
	ds.connect();
        
        ds.getDataSourceDelegate().addDataSourceInfo(ds);
        
    }
    
    
    private void turnOnProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.addProbe(p);
	    ds.turnOnProbe(p);
	}
    }


    public static void main(String[] args) throws InterruptedException {
        //System.out.println(args.length);
	try {
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 9999;
            String controlEndPoint = null;
            int controlLocalPort = 1111;
            
            if (args.length == 0) {
                // use existing settings
                String loopBack = InetAddress.getLoopbackAddress().getHostName();
                System.out.println("No arguments provided - running on loopback: " + loopBack);
                dsName = dataConsumerAddr = infoHost = controlEndPoint = loopBack;
                
            } else if (args.length == 6) {
                //dsName = args[0];
                
                dataConsumerAddr = args[0];
                
                Scanner sc = new Scanner(args[1]);
                dataConsumerPort = sc.nextInt();
                
                infoHost = args[2];
                
                sc = new Scanner(args[3]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                infoLocalPort = sc.nextInt();
                
                // controlEndPoint=args[6];
                
                sc= new Scanner(args[5]);
                controlLocalPort = sc.nextInt();
                
                dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                //dsName = controlEnd.getHostName(); 
                //System.out.println(controlEnd.toString());
                System.out.println(dsName);
                
            } else {
                System.err.println("use: SimpleDataSource dcAddress dcPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }            
            
            /*
            dsName: is saved in the infoplane to be used as the control endpoint for the DS (and as DS name)
            dataConsumerAddr: address of the destination dataConsumer 
            dataConsumerPort: port of the destination dataConsumer
            infoHost: host where the infoplane root node is running
            infoRemotePort: port where the info plane root node is listening
            infoLocalPort: port to be used by this DS to connect to the info plane
            controlLocalPort: port to be used locally for the contro plane
            */

            SimpleDataSourceTest hostMon = new SimpleDataSourceTest(dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort, 
                                                            infoLocalPort, 
                                                            controlEndPoint, 
                                                            controlLocalPort);
            
            
            Probe docker = new DockerProbe("osboxes1", "4243", "dockerTestProbe", "fb8366a76b7a", "name");
            hostMon.turnOnProbe(docker);
            
            //Probe random = new RandomProbe(".elapsedTime" + ".", "elapsedTime", 15, "de25f5b5-c73e-4fb8-80dd-ca6d97675943");
            //hostMon.turnOnProbe(random);
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}
