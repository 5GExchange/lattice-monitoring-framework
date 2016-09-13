// ResponseTimeEmulatorUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.fivegex.monitoring.control.udp.UDPControlPlaneConsumer;
import eu.reservoir.monitoring.core.AbstractDataSource;
import eu.reservoir.monitoring.core.DataSourceInteracter;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneProducer;
import eu.reservoir.monitoring.im.dht.DHTInfoPlane;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a UDP Data Plane.
 */
public class ResponseTimeEmulatorUDP {
    // The DataSource, TODO: maybe we should modify the definition of the DataSource interface to include loadProbe
    AbstractDataSource ds;

    /*
     * Construct a ResponseTimeEmulator.
     */
    public ResponseTimeEmulatorUDP(String addr, int dataPort, String myHostname, int infoLocalPort, int controlLocalPort) {
	// set up data source
	ds = new BasicDataSource(myHostname);

        
        System.out.println(addr);
        System.out.println(dataPort);
        System.out.println(myHostname);
        System.out.println(infoLocalPort);
        System.out.println(controlLocalPort);
        
	// set up an IPaddress for data
	//InetAddress dataAddr = InetAddress.getByName(addr);
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

        InetSocketAddress ctrlAddress = new InetSocketAddress(addr, controlLocalPort);
        
        
	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducer(address));
        
        // set up control plane: a data source is a consumer of Control Messages
        ControlPlane controlPlane = new UDPControlPlaneConsumer(ctrlAddress);
        ((DataSourceInteracter)controlPlane).setDataSource(ds);
        
        ds.setControlPlane(controlPlane);
        
        //the root of the DHT is on port 6699
        ds.setInfoPlane(new DHTInfoPlane(addr, 6699, infoLocalPort));
        
	ds.connect();
        
    }

    private void turnOnProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.addProbe(p);
	    ds.turnOnProbe(p);
	}
    }

    private void turnOffProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.deactivateProbe(p);
	    ds.removeProbe(p);
	}
    }
    
   

    public static void main(String [] args) throws InterruptedException {
	try {
            String addr = "localhost";
            int port = 22997;
            int infoLocalPort = 9999;
            int controlLocalPort = infoLocalPort + 1;
            String ID="";
            
            if (args.length == 0) {
                // use existing settings
            } else if (args.length == 5) {
                addr = args[0];
                
                ID = args[1];
                
                Scanner sc = new Scanner(args[2]);
                port = sc.nextInt();
                
                sc= new Scanner(args[3]);
                infoLocalPort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                controlLocalPort = sc.nextInt();
                
            } else if (args.length == 4) {
                
                addr = args[0];
                
                //ID = args[2];
                
                Scanner sc = new Scanner(args[1]);
                port = sc.nextInt();
                
                sc= new Scanner(args[2]);
                infoLocalPort = sc.nextInt();
                
                sc= new Scanner(args[3]);
                controlLocalPort = sc.nextInt();
                
            } else {
                System.err.println("ResponseTimeEmulatorUDP ip-address [probeID] port infoLocalPort controlLocalPort");
                System.exit(1);
            }
            
            // try and get the real current hostname
            String currentHost ="localhost";
            
            try {
                currentHost = InetAddress.getLocalHost().getHostName();   
                System.out.println(currentHost);
            } catch (Exception e) {
            }
          
            // we got a hostname
            ResponseTimeEmulatorUDP hostMon = new ResponseTimeEmulatorUDP(addr, port, currentHost, infoLocalPort, controlLocalPort);
            
            String unique = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            
            //Probe random = new RandomProbe(currentHost + ".elapsedTime" + "." + unique, "elapsedTime", 15, "de25f5b5-c73e-4fb8-80dd-ca6d97675943");
            //Probe random2 = new RandomProbe(currentHost + ".elapsedTime" + "." + unique, "elapsedTime", 15, "593f0db0-a5a0-4ae3-a5bb-8dc80d6e6aa7");
            
            //hostMon.turnOnProbe(random);
            //hostMon.turnOnProbe(random2);

            /*
            Probe testProbe = new UserProcTableProbe("ttys000");
            hostMon.turnOnProbe(testProbe);
            
            Thread.sleep(2000);
            Probe testProbe2 = new UserProcTableProbe("ttys000");
            hostMon.turnOnProbe(testProbe2);
            */
        } catch (Exception ex) {
            System.out.println("Error while creating the Probe " + ex.getMessage());
	}
        
        
        
        
    }
}
